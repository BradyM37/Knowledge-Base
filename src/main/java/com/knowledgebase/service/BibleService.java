package com.knowledgebase.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BibleService {

    private Map<String, List<String>> bibleVerses = new HashMap<>();
    private Map<String, String> bibleChapters = new HashMap<>();
    private String fullBibleText = "";
    
    @Autowired
    private FactService factService;

    @PostConstruct
    public void init() {
        try {
            loadBibleFromPdf();
            indexBibleContent();
        } catch (Exception e) {
            System.err.println("Failed to load Bible content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBibleFromPdf() {
        try {
            // Load the PDF from resources folder
            ClassPathResource resource = new ClassPathResource("bible/CSB_Pew_Bible.pdf");
            InputStream inputStream = resource.getInputStream();
            
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            
            // Extract text from the PDF
            fullBibleText = stripper.getText(document);
            document.close();
            
            // Store the full Bible text as a fact for general queries
            factService.storeFact("The Bible is a collection of religious texts or scriptures sacred in Christianity.");
            
            System.out.println("Bible PDF loaded successfully. Text length: " + fullBibleText.length());
        } catch (IOException e) {
            System.err.println("Error loading Bible PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

private void indexBibleContent() {
    // First, let's print a sample of the text to see its format
    System.out.println("Sample Bible text (first 1000 chars): " + fullBibleText.substring(0, Math.min(1000, fullBibleText.length())));
    
    // Try a more flexible pattern for book and chapter detection
    Pattern bookPattern = Pattern.compile("(Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|1\\s*Samuel|2\\s*Samuel|1\\s*Kings|2\\s*Kings|1\\s*Chronicles|2\\s*Chronicles|Ezra|Nehemiah|Esther|Job|Psalms?|Proverbs|Ecclesiastes|Song\\s*of\\s*Solomon|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|1\\s*Corinthians|2\\s*Corinthians|Galatians|Ephesians|Philippians|Colossians|1\\s*Thessalonians|2\\s*Thessalonians|1\\s*Timothy|2\\s*Timothy|Titus|Philemon|Hebrews|James|1\\s*Peter|2\\s*Peter|1\\s*John|2\\s*John|3\\s*John|Jude|Revelation)\\s*(?:Chapter\\s*)?(\\d+)", Pattern.CASE_INSENSITIVE);
    
    Matcher bookMatcher = bookPattern.matcher(fullBibleText);
    
    int matchCount = 0;
    while (bookMatcher.find()) {
        matchCount++;
        String book = bookMatcher.group(1);
        String chapter = bookMatcher.group(2);
        
        // Normalize book name
        book = book.trim();
        if (book.toLowerCase().equals("psalm")) {
            book = "Psalms";
        }
        
        String bookChapter = book + " " + chapter;
        
        // Find the start of this chapter
        int chapterStart = bookMatcher.end();
        
        // Find the end of this chapter (start of next chapter or end of text)
        int chapterEnd = fullBibleText.length();
        
        // Save current position
        int currentPos = bookMatcher.end();
        
        // Look for the next chapter
        if (bookMatcher.find()) {
            chapterEnd = bookMatcher.start();
            // Reset the matcher to continue from where we were
            bookMatcher = bookPattern.matcher(fullBibleText);
            bookMatcher.region(currentPos, fullBibleText.length());
        }
        
        // Extract the chapter text
        String chapterText = fullBibleText.substring(chapterStart, chapterEnd).trim();
        
        // Debug output for first few matches
        if (matchCount <= 3) {
            System.out.println("Found: " + book + " " + chapter);
            System.out.println("Chapter text sample: " + chapterText.substring(0, Math.min(100, chapterText.length())));
        }
        
        bibleChapters.put(bookChapter, chapterText);
        
        // Store each chapter as a fact
        factService.storeFact(book + " Chapter " + chapter + ": " + chapterText);
        
        // Extract verses - more flexible pattern
        Pattern versePattern = Pattern.compile("(\\d+)[\\s\\.\\:]\\s*(.+?)(?=\\d+[\\s\\.\\:]|$)", Pattern.DOTALL);
        Matcher verseMatcher = versePattern.matcher(chapterText);
        
        while (verseMatcher.find()) {
            String verseNum = verseMatcher.group(1);
            String verseText = verseMatcher.group(2).trim();
            String verseRef = book + " " + chapter + ":" + verseNum;
            
            // Store the verse
            if (!bibleVerses.containsKey(book)) {
                bibleVerses.put(book, new ArrayList<>());
            }
            bibleVerses.get(book).add(verseRef + " - " + verseText);
            
            // Store each verse as a fact
            factService.storeFact(verseRef + ": " + verseText);
            
            // Store common questions about this verse
            factService.storeQuestionAnswer(
                "What does " + verseRef + " say?", 
                verseText
            );
            
            factService.storeQuestionAnswer(
                "Where in the Bible does it talk about " + extractKeywords(verseText) + "?",
                "You can find this in " + verseRef + ": " + verseText
            );
        }
    }
    
    System.out.println("Bible indexed successfully. Books: " + bibleVerses.size() + ", Chapters: " + bibleChapters.size());
    System.out.println("Total matches found: " + matchCount);
}
    
    private String extractKeywords(String text) {
        // Simple keyword extraction - remove common words and punctuation
        return text.replaceAll("\\b(the|and|of|to|in|that|is|was|for|on|with|as|by|at|from)\\b", "")
                  .replaceAll("[^a-zA-Z0-9\\s]", "")
                  .trim();
    }
    
public String findBiblePassage(String query) {
    // Normalize the query - convert to title case for book names
    query = query.trim();
    
    // Handle common variations
    query = query.replaceAll("(?i)\\bpsalm\\b", "Psalms");
    query = query.replaceAll("(?i)\\bpsalms\\b", "Psalms");
    
    // Check if query is a direct reference (e.g., "John 3:16")
    Pattern referencePattern = Pattern.compile("(Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|1 Samuel|2 Samuel|1 Kings|2 Kings|1 Chronicles|2 Chronicles|Ezra|Nehemiah|Esther|Job|Psalms|Proverbs|Ecclesiastes|Song of Solomon|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|1 Corinthians|2 Corinthians|Galatians|Ephesians|Philippians|Colossians|1 Thessalonians|2 Thessalonians|1 Timothy|2 Timothy|Titus|Philemon|Hebrews|James|1 Peter|2 Peter|1 John|2 John|3 John|Jude|Revelation)\\s+(\\d+)(?::(\\d+))?", Pattern.CASE_INSENSITIVE);
    
    Matcher referenceMatcher = referencePattern.matcher(query);
    if (referenceMatcher.find()) {
        String book = referenceMatcher.group(1);
        // Ensure proper capitalization for book name
        book = book.substring(0, 1).toUpperCase() + book.substring(1).toLowerCase();
        if (book.startsWith("1 ") || book.startsWith("2 ") || book.startsWith("3 ")) {
            book = book.substring(0, 2) + book.substring(2, 3).toUpperCase() + book.substring(3);
        }
        
        String chapter = referenceMatcher.group(2);
        String verse = referenceMatcher.group(3);
        
        if (verse != null) {
            // Looking for a specific verse
            String verseRef = book + " " + chapter + ":" + verse;
            for (String verseText : bibleVerses.getOrDefault(book, new ArrayList<>())) {
                if (verseText.startsWith(verseRef)) {
                    return verseText;
                }
            }
        } else {
            // Looking for a whole chapter
            String chapterRef = book + " " + chapter;
            return bibleChapters.getOrDefault(chapterRef, "Chapter not found: " + chapterRef);
        }
    }
        
        // If not a direct reference, search for keywords
        String[] keywords = query.toLowerCase().split("\\s+");
        List<String> matchingVerses = new ArrayList<>();
        
        for (List<String> verses : bibleVerses.values()) {
            for (String verse : verses) {
                String lowerVerse = verse.toLowerCase();
                boolean allKeywordsMatch = true;
                
                for (String keyword : keywords) {
                    if (!lowerVerse.contains(keyword)) {
                        allKeywordsMatch = false;
                        break;
                    }
                }
                
                if (allKeywordsMatch) {
                    matchingVerses.add(verse);
                }
            }
        }
        
        if (!matchingVerses.isEmpty()) {
            return String.join("\n\n", matchingVerses.subList(0, Math.min(3, matchingVerses.size())));
        }
        
        return "No specific Bible passage found for your query. Please try a different question.";
    }
}