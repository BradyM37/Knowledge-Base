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
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BibleService {

    private Map<String, List<String>> bibleVerses = new HashMap<>();
    private Map<String, String> bibleChapters = new HashMap<>();
    private Map<String, String> bibleVersesByReference = new HashMap<>(); // Add this line
    private String fullBibleText = "";
    
    // Map for common misspellings and variations of book names
    private Map<String, String> bookNameVariations = new HashMap<>();
    
    @Autowired
    private FactService factService;
    @PostConstruct
    public void init() {
        try {
            initializeBookNameVariations();
            loadBibleFromPdf();
            indexBibleContent();
        } catch (Exception e) {
            System.err.println("Failed to load Bible content: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize map of common book name variations and misspellings
     */
    private void initializeBookNameVariations() {
        // Genesis variations
        bookNameVariations.put("genisis", "Genesis");
        bookNameVariations.put("geneses", "Genesis");
        bookNameVariations.put("gen", "Genesis");
        
        // Exodus variations
        bookNameVariations.put("exodous", "Exodus");
        bookNameVariations.put("exo", "Exodus");
        
        // Leviticus variations
        bookNameVariations.put("leviticus", "Leviticus");
        bookNameVariations.put("levitcus", "Leviticus");
        bookNameVariations.put("lev", "Leviticus");
        
        // Numbers variations
        bookNameVariations.put("num", "Numbers");
        
        // Deuteronomy variations
        bookNameVariations.put("deuteronmy", "Deuteronomy");
        bookNameVariations.put("dueteronomy", "Deuteronomy");
        bookNameVariations.put("deut", "Deuteronomy");
        
        // Joshua variations
        bookNameVariations.put("josh", "Joshua");
        
        // Judges variations
        bookNameVariations.put("judg", "Judges");
        
        // Ruth variations
        // (No common misspellings)
        
        // Samuel variations
        bookNameVariations.put("1 sam", "1 Samuel");
        bookNameVariations.put("first samuel", "1 Samuel");
        bookNameVariations.put("first sam", "1 Samuel");
        bookNameVariations.put("1st samuel", "1 Samuel");
        bookNameVariations.put("1st sam", "1 Samuel");
        bookNameVariations.put("i samuel", "1 Samuel");
        bookNameVariations.put("i sam", "1 Samuel");
        
        bookNameVariations.put("2 sam", "2 Samuel");
        bookNameVariations.put("second samuel", "2 Samuel");
        bookNameVariations.put("second sam", "2 Samuel");
        bookNameVariations.put("2nd samuel", "2 Samuel");
        bookNameVariations.put("2nd sam", "2 Samuel");
        bookNameVariations.put("ii samuel", "2 Samuel");
        bookNameVariations.put("ii sam", "2 Samuel");
        
        // Kings variations
        bookNameVariations.put("1 kings", "1 Kings");
        bookNameVariations.put("first kings", "1 Kings");
        bookNameVariations.put("1st kings", "1 Kings");
        bookNameVariations.put("i kings", "1 Kings");
        
        bookNameVariations.put("2 kings", "2 Kings");
        bookNameVariations.put("second kings", "2 Kings");
        bookNameVariations.put("2nd kings", "2 Kings");
        bookNameVariations.put("ii kings", "2 Kings");
        
        // Chronicles variations
        bookNameVariations.put("1 chron", "1 Chronicles");
        bookNameVariations.put("first chronicles", "1 Chronicles");
        bookNameVariations.put("1st chronicles", "1 Chronicles");
        bookNameVariations.put("i chronicles", "1 Chronicles");
        bookNameVariations.put("i chron", "1 Chronicles");
        
        bookNameVariations.put("2 chron", "2 Chronicles");
        bookNameVariations.put("second chronicles", "2 Chronicles");
        bookNameVariations.put("2nd chronicles", "2 Chronicles");
        bookNameVariations.put("ii chronicles", "2 Chronicles");
        bookNameVariations.put("ii chron", "2 Chronicles");
        
        // Ezra variations
        // (No common misspellings)
        
        // Nehemiah variations
        bookNameVariations.put("nehamiah", "Nehemiah");
        bookNameVariations.put("neh", "Nehemiah");
        
        // Esther variations
        bookNameVariations.put("est", "Esther");
        
        // Job variations
        // (No common misspellings)
        
        // Psalms variations
        bookNameVariations.put("psalm", "Psalms");
        bookNameVariations.put("pslams", "Psalms");
        bookNameVariations.put("pslam", "Psalms");
        bookNameVariations.put("ps", "Psalms");
        
        // Proverbs variations
        bookNameVariations.put("proverb", "Proverbs");
        bookNameVariations.put("prov", "Proverbs");
        
        // Ecclesiastes variations
        bookNameVariations.put("ecclesiates", "Ecclesiastes");
        bookNameVariations.put("eccl", "Ecclesiastes");
        bookNameVariations.put("ecc", "Ecclesiastes");
        
        // Song of Solomon variations
        bookNameVariations.put("song of songs", "Song of Solomon");
        bookNameVariations.put("songs", "Song of Solomon");
        bookNameVariations.put("song", "Song of Solomon");
        bookNameVariations.put("sos", "Song of Solomon");
        
        // Isaiah variations
        bookNameVariations.put("isiah", "Isaiah");
        bookNameVariations.put("isaiah", "Isaiah");
        bookNameVariations.put("isa", "Isaiah");
        
        // Jeremiah variations
        bookNameVariations.put("jerimiah", "Jeremiah");
        bookNameVariations.put("jer", "Jeremiah");
        
        // Lamentations variations
        bookNameVariations.put("lam", "Lamentations");
        
        // Ezekiel variations
        bookNameVariations.put("ezekial", "Ezekiel");
        bookNameVariations.put("ezek", "Ezekiel");
        
        // Daniel variations
        bookNameVariations.put("dan", "Daniel");
        
        // Hosea variations
        bookNameVariations.put("hos", "Hosea");
        
        // Joel variations
        // (No common misspellings)
        
        // Amos variations
        // (No common misspellings)
        
        // Obadiah variations
        bookNameVariations.put("obad", "Obadiah");
        
        // Jonah variations
        // (No common misspellings)
        
        // Micah variations
        bookNameVariations.put("mic", "Micah");
        
        // Nahum variations
        bookNameVariations.put("nah", "Nahum");
        
        // Habakkuk variations
        bookNameVariations.put("habakuk", "Habakkuk");
        bookNameVariations.put("hab", "Habakkuk");
        
        // Zephaniah variations
        bookNameVariations.put("zeph", "Zephaniah");
        
        // Haggai variations
        bookNameVariations.put("hag", "Haggai");
        
        // Zechariah variations
        bookNameVariations.put("zech", "Zechariah");
        
        // Malachi variations
        bookNameVariations.put("mal", "Malachi");
        
        // Matthew variations
        bookNameVariations.put("mathew", "Matthew");
        bookNameVariations.put("matt", "Matthew");
        bookNameVariations.put("mat", "Matthew");
        
        // Mark variations
        bookNameVariations.put("mrk", "Mark");
        
        // Luke variations
        bookNameVariations.put("luk", "Luke");
        
        // John variations
        bookNameVariations.put("jhn", "John");
        
        // Acts variations
        bookNameVariations.put("act", "Acts");
        
        // Romans variations
        bookNameVariations.put("rom", "Romans");
        
        // Corinthians variations
        bookNameVariations.put("1 cor", "1 Corinthians");
        bookNameVariations.put("first corinthians", "1 Corinthians");
        bookNameVariations.put("1st corinthians", "1 Corinthians");
        bookNameVariations.put("i corinthians", "1 Corinthians");
        bookNameVariations.put("i cor", "1 Corinthians");
        
        bookNameVariations.put("2 cor", "2 Corinthians");
        bookNameVariations.put("second corinthians", "2 Corinthians");
        bookNameVariations.put("2nd corinthians", "2 Corinthians");
        bookNameVariations.put("ii corinthians", "2 Corinthians");
        bookNameVariations.put("ii cor", "2 Corinthians");
        
        // Galatians variations
        bookNameVariations.put("galatians", "Galatians");
        bookNameVariations.put("gal", "Galatians");
        
        // Ephesians variations
        bookNameVariations.put("ephesians", "Ephesians");
        bookNameVariations.put("eph", "Ephesians");
        
        // Philippians variations
        bookNameVariations.put("phillipians", "Philippians");
        bookNameVariations.put("phil", "Philippians");
        
        // Colossians variations
        bookNameVariations.put("collosians", "Colossians");
        bookNameVariations.put("col", "Colossians");
        
        // Thessalonians variations
        bookNameVariations.put("1 thess", "1 Thessalonians");
        bookNameVariations.put("first thessalonians", "1 Thessalonians");
        bookNameVariations.put("1st thessalonians", "1 Thessalonians");
        bookNameVariations.put("i thessalonians", "1 Thessalonians");
        bookNameVariations.put("i thess", "1 Thessalonians");
        
        bookNameVariations.put("2 thess", "2 Thessalonians");
        bookNameVariations.put("second thessalonians", "2 Thessalonians");
        bookNameVariations.put("2nd thessalonians", "2 Thessalonians");
        bookNameVariations.put("ii thessalonians", "2 Thessalonians");
        bookNameVariations.put("ii thess", "2 Thessalonians");
        
        // Timothy variations
        bookNameVariations.put("1 tim", "1 Timothy");
        bookNameVariations.put("first timothy", "1 Timothy");
        bookNameVariations.put("1st timothy", "1 Timothy");
        bookNameVariations.put("i timothy", "1 Timothy");
        bookNameVariations.put("i tim", "1 Timothy");
        
        bookNameVariations.put("2 tim", "2 Timothy");
        bookNameVariations.put("second timothy", "2 Timothy");
        bookNameVariations.put("2nd timothy", "2 Timothy");
        bookNameVariations.put("ii timothy", "2 Timothy");
        bookNameVariations.put("ii tim", "2 Timothy");
        
        // Titus variations
        bookNameVariations.put("tit", "Titus");
        
        // Philemon variations
        bookNameVariations.put("philem", "Philemon");
        bookNameVariations.put("phlm", "Philemon");
        
        // Hebrews variations
        bookNameVariations.put("heb", "Hebrews");
        
        // James variations
        bookNameVariations.put("jam", "James");
        bookNameVariations.put("jas", "James");
        
        // Peter variations
        bookNameVariations.put("1 pet", "1 Peter");
        bookNameVariations.put("first peter", "1 Peter");
        bookNameVariations.put("1st peter", "1 Peter");
        bookNameVariations.put("i peter", "1 Peter");
        bookNameVariations.put("i pet", "1 Peter");
        
        bookNameVariations.put("2 pet", "2 Peter");
        bookNameVariations.put("second peter", "2 Peter");
        bookNameVariations.put("2nd peter", "2 Peter");
        bookNameVariations.put("ii peter", "2 Peter");
        bookNameVariations.put("ii pet", "2 Peter");
        
        // John variations
        bookNameVariations.put("1 jn", "1 John");
        bookNameVariations.put("first john", "1 John");
        bookNameVariations.put("1st john", "1 John");
        bookNameVariations.put("i john", "1 John");
        bookNameVariations.put("i jn", "1 John");
        
        bookNameVariations.put("2 jn", "2 John");
        bookNameVariations.put("second john", "2 John");
        bookNameVariations.put("2nd john", "2 John");
        bookNameVariations.put("ii john", "2 John");
        bookNameVariations.put("ii jn", "2 John");
        
        bookNameVariations.put("3 jn", "3 John");
        bookNameVariations.put("third john", "3 John");
        bookNameVariations.put("3rd john", "3 John");
        bookNameVariations.put("iii john", "3 John");
        bookNameVariations.put("iii jn", "3 John");
        
        // Jude variations
        // (No common misspellings)
        
        // Revelation variations
        bookNameVariations.put("revelations", "Revelation");
        bookNameVariations.put("rev", "Revelation");
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
        // Based on the sample text, we need to look for patterns like "PSALM 12" or similar
        Pattern bookPattern = Pattern.compile("(GENESIS|EXODUS|LEVITICUS|NUMBERS|DEUTERONOMY|JOSHUA|JUDGES|RUTH|1\\s*SAMUEL|2\\s*SAMUEL|1\\s*KINGS|2\\s*KINGS|1\\s*CHRONICLES|2\\s*CHRONICLES|EZRA|NEHEMIAH|ESTHER|JOB|PSALMS?|PROVERBS|ECCLESIASTES|SONG\\s*OF\\s*SOLOMON|ISAIAH|JEREMIAH|LAMENTATIONS|EZEKIEL|DANIEL|HOSEA|JOEL|AMOS|OBADIAH|JONAH|MICAH|NAHUM|HABAKKUK|ZEPHANIAH|HAGGAI|ZECHARIAH|MALACHI|MATTHEW|MARK|LUKE|JOHN|ACTS|ROMANS|1\\s*CORINTHIANS|2\\s*CORINTHIANS|GALATIANS|EPHESIANS|PHILIPPIANS|COLOSSIANS|1\\s*THESSALONIANS|2\\s*THESSALONIANS|1\\s*TIMOTHY|2\\s*TIMOTHY|TITUS|PHILEMON|HEBREWS|JAMES|1\\s*PETER|2\\s*PETER|1\\s*JOHN|2\\s*JOHN|3\\s*JOHN|JUDE|REVELATION)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        
        Matcher bookMatcher = bookPattern.matcher(fullBibleText);
        
        int matchCount = 0;
        while (bookMatcher.find()) {
            matchCount++;
            String book = bookMatcher.group(1);
            String chapter = bookMatcher.group(2);
            
            // Normalize book name - convert to proper case
            book = book.substring(0, 1).toUpperCase() + book.substring(1).toLowerCase();
            if (book.toLowerCase().startsWith("1 ") || book.toLowerCase().startsWith("2 ") || book.toLowerCase().startsWith("3 ")) {
                book = book.substring(0, 2) + book.substring(2, 3).toUpperCase() + book.substring(3);
            }
            
            // Handle "Psalm" vs "Psalms"
            if (book.equalsIgnoreCase("psalm")) {
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
            
            // Store chapter summary instead of full text (to avoid exceeding VARCHAR limit)
            String chapterSummary = book + " Chapter " + chapter + " summary";
            // Limit to 900 characters to be safe
            if (chapterSummary.length() <= 900) {
                factService.storeFact(chapterSummary);
            }
            
            // Extract verses - more flexible pattern based on the PDF format
            // Looking for patterns like "1 The words..." or "1 In the beginning..."
            Pattern versePattern = Pattern.compile("(\\d+)\\s+([^\\d].+?)(?=\\s+\\d+\\s+|$)", Pattern.DOTALL);
            Matcher verseMatcher = versePattern.matcher(chapterText);
            
            while (verseMatcher.find()) {
                String verseNum = verseMatcher.group(1);
                String verseText = verseMatcher.group(2).trim();
                
                // Limit verse text to 900 characters to avoid exceeding VARCHAR limit
                if (verseText.length() > 900) {
                    verseText = verseText.substring(0, 900) + "...";
                }
                
                String verseRef = book + " " + chapter + ":" + verseNum;
                
                // Store the verse
                if (!bibleVerses.containsKey(book)) {
                    bibleVerses.put(book, new ArrayList<>());
                }
                bibleVerses.get(book).add(verseRef + " - " + verseText);
                
                // Also store by specific reference for faster lookup
                String verseKey = book + " " + chapter + ":" + verseNum;
                if (!bibleVersesByReference.containsKey(verseKey)) {
                    bibleVersesByReference.put(verseKey, verseText);
                }
                
                // Store each verse as a fact (with length check)
                String factContent = verseRef + ": " + verseText;
                if (factContent.length() <= 900) {
                    factService.storeFact(factContent);
                }
                
                // Store common questions about this verse (with length checks)
                String question1 = "What does " + verseRef + " say?";
                if (question1.length() + verseText.length() <= 900) {
                    factService.storeQuestionAnswer(question1, verseText);
                }
                
                // Be more selective with keywords to avoid long questions
                String keywords = extractKeywords(verseText);
                // Limit keywords to ensure we don't exceed length
                if (keywords.length() > 50) {
                    keywords = keywords.substring(0, 50);
                }
                
                // Only create keyword questions for important verses to reduce database load
                if (verseNum.equals("1") || Integer.parseInt(verseNum) % 10 == 0) {
                    String question2 = "Where in the Bible does it talk about " + keywords + "?";
                    String answer2 = "You can find this in " + verseRef + ": " + verseText;
                    
                    if (question2.length() <= 900 && answer2.length() <= 900) {
                        factService.storeQuestionAnswer(question2, answer2);
                    }
                }
            }
        }
        
        System.out.println("Bible indexed successfully. Books: " + bibleVerses.size() + ", Chapters: " + bibleChapters.size());
        System.out.println("Total matches found: " + matchCount);
        System.out.println("Total verses by reference: " + bibleVersesByReference.size());
    }
    private String extractKeywords(String text) {
        // Simple keyword extraction - remove common words and punctuation
        return text.replaceAll("\\b(the|and|of|to|in|that|is|was|for|on|with|as|by|at|from)\\b", "")
                  .replaceAll("[^a-zA-Z0-9\\s]", "")
                  .trim();
    }
    
    /**
     * Find a Bible passage based on a query
     * Handles direct references, misspellings, and keyword searches
     */
    public String findBiblePassage(String query) {
        // Normalize the query - convert to title case for book names
        query = query.trim();
        
        // Debug the query
        System.out.println("Bible passage query: " + query);
        
        // Check if query is a direct reference (e.g., "John 3:16")
        Pattern referencePattern = Pattern.compile("(\\w+(?:\\s+\\w+)*)\\s+(\\d+)(?::(\\d+))?", Pattern.CASE_INSENSITIVE);
        
        Matcher referenceMatcher = referencePattern.matcher(query);
        if (referenceMatcher.find()) {
            String bookName = referenceMatcher.group(1).trim();
            String chapter = referenceMatcher.group(2);
            String verse = referenceMatcher.group(3); // May be null if no verse specified
            
            // Check for common misspellings and variations
            String normalizedBookName = normalizeBookName(bookName);
            
            if (normalizedBookName != null) {
                // We found a valid book name
                if (verse != null) {
                    // Looking for a specific verse
                    String verseRef = normalizedBookName + " " + chapter + ":" + verse;
                    
                    // First try direct lookup from our reference map
                    if (bibleVersesByReference.containsKey(verseRef)) {
                        return verseRef + " - " + bibleVersesByReference.get(verseRef);
                    }
                    
                    // If not found in map, try searching through the book's verses
                    if (bibleVerses.containsKey(normalizedBookName)) {
                        for (String verseText : bibleVerses.get(normalizedBookName)) {
                            if (verseText.startsWith(verseRef)) {
                                return verseText;
                            }
                        }
                        return "Verse " + verseRef + " not found.";
                    }
                } else {
                    // Return the whole chapter
                    String chapterKey = normalizedBookName + " " + chapter;
                    if (bibleChapters.containsKey(chapterKey)) {
                        return "Chapter " + chapterKey + ":\n" + bibleChapters.get(chapterKey);
                    } else {
                        return "Chapter " + chapterKey + " not found.";
                    }
                }
            }
        }
        
        // If we get here, try a more flexible search
        // First, check if any book name is mentioned
        for (String bookName : bibleVerses.keySet()) {
            if (query.toLowerCase().contains(bookName.toLowerCase())) {
                // Return a sample of verses from this book
                List<String> verses = bibleVerses.get(bookName);
                int sampleSize = Math.min(3, verses.size());
                StringBuilder result = new StringBuilder("Found in " + bookName + ":\n");
                
                for (int i = 0; i < sampleSize; i++) {
                    result.append(verses.get(i)).append("\n\n");
                }
                
                return result.toString();
            }
        }
        
        // If still no match, try keyword search
        String keywords = extractKeywords(query);
        List<String> matchingVerses = new ArrayList<>();
        
        for (List<String> verses : bibleVerses.values()) {
            for (String verse : verses) {
                // Check if verse contains any of the keywords
                String[] keywordArray = keywords.split("\\s+");
                boolean match = false;
                
                for (String keyword : keywordArray) {
                    if (keyword.length() > 2 && verse.toLowerCase().contains(keyword.toLowerCase())) {
                        match = true;
                        break;
                    }
                }
                
                if (match) {
                    matchingVerses.add(verse);
                    if (matchingVerses.size() >= 5) {
                        break; // Limit to 5 matches
                    }
                }
            }
            
            if (matchingVerses.size() >= 5) {
                break;
            }
        }
        
        if (!matchingVerses.isEmpty()) {
            StringBuilder result = new StringBuilder("Found these verses related to your query:\n\n");
            for (String verse : matchingVerses) {
                result.append(verse).append("\n\n");
            }
            return result.toString();
        }
        
        return "No Bible passages found matching your query. Try using a book name, chapter, and verse (e.g., 'John 3:16').";
    }
    
    /**
     * Normalize book name to handle variations and misspellings
     */
    private String normalizeBookName(String bookName) {
        // Check direct match first
        for (String validBook : bibleVerses.keySet()) {
            if (bookName.equalsIgnoreCase(validBook)) {
                return validBook;
            }
        }
        
        // Check for variations and misspellings
        String lowercaseBookName = bookName.toLowerCase();
        if (bookNameVariations.containsKey(lowercaseBookName)) {
            return bookNameVariations.get(lowercaseBookName);
        }
        
        // Try fuzzy matching for misspellings
        for (Map.Entry<String, String> entry : bookNameVariations.entrySet()) {
            // Simple fuzzy match - if the first 3+ chars match
            if (entry.getKey().length() > 3 && 
                lowercaseBookName.length() > 3 &&
                entry.getKey().substring(0, 3).equals(lowercaseBookName.substring(0, 3))) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Get a list of all available Bible books
     */
    public List<String> getAllBibleBooks() {
        return new ArrayList<>(bibleVerses.keySet());
    }
    
    /**
     * Get a list of chapters available for a specific book
     */
    public List<String> getChaptersForBook(String bookName) {
        String normalizedBookName = normalizeBookName(bookName);
        if (normalizedBookName == null) {
            return new ArrayList<>();
        }
        
        Set<String> chapters = new TreeSet<>();
        for (String chapterKey : bibleChapters.keySet()) {
            if (chapterKey.startsWith(normalizedBookName + " ")) {
                chapters.add(chapterKey);
            }
        }
        
        return new ArrayList<>(chapters);
    }
    
    /**
     * Get a random Bible verse
     */
    public String getRandomVerse() {
        List<String> allBooks = new ArrayList<>(bibleVerses.keySet());
        if (allBooks.isEmpty()) {
            return "No Bible verses available.";
        }
        
        // Pick a random book
        String randomBook = allBooks.get((int) (Math.random() * allBooks.size()));
        List<String> verses = bibleVerses.get(randomBook);
        
        if (verses.isEmpty()) {
            return "No verses found in " + randomBook;
        }
        
        // Pick a random verse
        return verses.get((int) (Math.random() * verses.size()));
    }
    
    /**
     * Search for Bible verses containing specific keywords
     */
    public List<String> searchBibleByKeywords(String keywords) {
        List<String> results = new ArrayList<>();
        String[] keywordArray = keywords.toLowerCase().split("\\s+");
        
        for (List<String> verses : bibleVerses.values()) {
            for (String verse : verses) {
                boolean match = true;
                for (String keyword : keywordArray) {
                    if (keyword.length() > 2 && !verse.toLowerCase().contains(keyword)) {
                        match = false;
                        break;
                    }
                }
                
                if (match) {
                    results.add(verse);
                    if (results.size() >= 20) {
                        return results; // Limit to 20 results
                    }
                }
            }
        }
        
        return results;
    }
}