package com.knowledgebase.controller;

import com.knowledgebase.model.QueryRequest;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.service.BibleService;
import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api", ""})
public class ApiProxyController {

    @Autowired
    private FactService factService;
    
    @Autowired
    private BibleService bibleService;

    /**
     * Determines if a query is Bible-related
     */
    private boolean isBibleQuery(String query) {
        if (query == null) return false;
        
        String lowerQuery = query.toLowerCase();
        
        // Check for Bible book names
        String[] bibleBooks = {"genesis", "exodus", "leviticus", "numbers", "deuteronomy", 
                              "joshua", "judges", "ruth", "samuel", "kings", "chronicles",
                              "ezra", "nehemiah", "esther", "job", "psalm", "psalms", "proverbs",
                              "ecclesiastes", "song of solomon", "isaiah", "jeremiah", "lamentations",
                              "ezekiel", "daniel", "hosea", "joel", "amos", "obadiah", "jonah",
                              "micah", "nahum", "habakkuk", "zephaniah", "haggai", "zechariah",
                              "malachi", "matthew", "mark", "luke", "john", "acts", "romans",
                              "corinthians", "galatians", "ephesians", "philippians", "colossians",
                              "thessalonians", "timothy", "titus", "philemon", "hebrews", "james",
                              "peter", "jude", "revelation"};
        
        for (String book : bibleBooks) {
            if (lowerQuery.contains(book)) {
                return true;
            }
        }
        
        // Check for Bible-specific terms
        String[] bibleTerms = {"bible", "scripture", "verse", "chapter", "testament", "gospel",
                              "jesus", "christ", "god", "holy spirit", "apostle", "prophet"};
        
        for (String term : bibleTerms) {
            if (lowerQuery.contains(term)) {
                return true;
            }
        }
        
        // Check for verse references (e.g., John 3:16)
        if (lowerQuery.matches(".*\\d+:\\d+.*")) {
            return true;
        }
        
        return false;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestBody Map<String, String> request) {
        String question = request.get("query");
        String sessionId = request.get("sessionId");
        
        // Check if this is a Bible-related query
        if (isBibleQuery(question)) {
            String bibleAnswer = bibleService.findBiblePassage(question);
            return ResponseEntity.ok(new QueryResponse(bibleAnswer, sessionId));
        }
        
        // Otherwise, use the fact service
        String answer = factService.queryFact(question); // Changed from queryFactString to queryFact
        
        return ResponseEntity.ok(new QueryResponse(answer, sessionId));
    }
}