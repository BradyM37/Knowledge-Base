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
        // Extract books, chapters, and verses
        Pattern bookPattern = Pattern.compile("(Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|1 Samuel|2 Samuel|1 Kings|2 Kings|1 Chronicles|2 Chronicles|Ezra|Nehemiah|Esther|Job|Psalms|Proverbs|Ecclesiastes|Song of Solomon|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|1 Corinthians|2 Corinthians|Galatians|Ephesians|Philippians|Colossians|1 Thessalonians|2 Thessalonians|1 Timothy|2 Timothy|Titus|Philemon|Hebrews|James|1 Peter|2 Peter|1 John|2 John|3 John|Jude|Revelation)\\s+Chapter\\s+(\\d+)");
        
        Matcher bookMatcher = bookPattern.matcher(fullBibleText);
        
        while (bookMatcher.find()) {
            String book = bookMatcher.group(1);
            String chapter = bookMatcher.group(2);
            String bookChapter = book + " " + chapter;
            
            // Find the start of this chapter
            int chapterStart = bookMatcher.end();
            
            // Find the end of this chapter (start of next chapter or end of text)
            int chapterEnd = fullBibleText.length();
            bookMatcher.find();
            if (bookMatcher.find()) {
                chapterEnd = bookMatcher.start();
                // Reset the matcher to continue from where we were
                bookMatcher = bookPattern.matcher(fullBibleText);
                bookMatcher.region(chapterStart, fullBibleText.length());
            }
            
            // Extract the chapter text
            String chapterText = fullBibleText.substring(chapterStart, chapterEnd).trim();
            bibleChapters.put(bookChapter, chapterText);
            
            // Store each chapter as a fact
            factService.storeFact(book + " Chapter " + chapter + ": " + chapterText);
            
            // Extract verses
            Pattern versePattern = Pattern.compile("(\\d+)\\s+(.+?)(?=\\d+\\s+|$)");
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
    }
    
    private String extractKeywords(String text) {
        // Simple keyword extraction - remove common words and punctuation
        return text.replaceAll("\\b(the|and|of|to|in|that|is|was|for|on|with|as|by|at|from)\\b", "")
                  .replaceAll("[^a-zA-Z0-9\\s]", "")
                  .trim();
    }
    
    public String findBiblePassage(String query) {
        // Check if query is a direct reference (e.g., "John 3:16")
        Pattern referencePattern = Pattern.compile("(Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|1 Samuel|2 Samuel|1 Kings|2 Kings|1 Chronicles|2 Chronicles|Ezra|Nehemiah|Esther|Job|Psalms|Proverbs|Ecclesiastes|Song of Solomon|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|1 Corinthians|2 Corinthians|Galatians|Ephesians|Philippians|Colossians|1 Thessalonians|2 Thessalonians|1 Timothy|2 Timothy|Titus|Philemon|Hebrews|James|1 Peter|2 Peter|1 John|2 John|3 John|Jude|Revelation)\\s+(\\d+)(?::(\\d+))?");
        
        Matcher referenceMatcher = referencePattern.matcher(query);
        if (referenceMatcher.find()) {
            String book = referenceMatcher.group(1);
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
                return bibleChapters.getOrDefault(chapterRef, "Chapter not found");
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