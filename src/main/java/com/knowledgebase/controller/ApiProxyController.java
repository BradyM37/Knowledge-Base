package com.knowledgebase.controller;

import com.knowledgebase.model.QueryRequest;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.service.BibleService;
import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                              "peter", "john", "jude", "revelation"};
        
        for (String book : bibleBooks) {
            if (lowerQuery.contains(book)) {
                return true;
            }
        }
        
        // Check for Bible-related keywords
        String[] bibleKeywords = {"bible", "scripture", "verse", "chapter", "testament", 
                                 "gospel", "jesus", "christ", "god", "holy spirit", "apostle"};
        
        for (String keyword : bibleKeywords) {
            if (lowerQuery.contains(keyword)) {
                return true;
            }
        }
        
        // Check for verse reference patterns (e.g., John 3:16)
        if (lowerQuery.matches(".*\\d+:\\d+.*")) {
            return true;
        }
        
        return false;
    }

    @PostMapping("/query")
    public ResponseEntity<Object> handleQuery(@RequestBody QueryRequest request) {
        // Process the query directly
        String query = request.getQuery();
        
        // Check if it's a Bible-related query
        if (isBibleQuery(query)) {
            String bibleAnswer = bibleService.findBiblePassage(query);
            return ResponseEntity.ok(new QueryResponse(bibleAnswer, 1.0, "Bible"));
        }
        
        // Otherwise use the regular fact service
        QueryResponse response = factService.queryFact(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/store")
    public ResponseEntity<Object> handleStore(@RequestBody String fact) {
        // Store the fact directly
        factService.storeFact(fact);
        return ResponseEntity.ok().body(java.util.Map.of("status", "success", "message", "Fact stored successfully"));
    }

    // Buildship specific endpoints
    @PostMapping("/buildship/query")
    public ResponseEntity<Object> handleBuildshipQuery(@RequestBody java.util.Map<String, String> request) {
        String question = request.get("question");
        String sessionId = request.get("sessionId");
        
        // Check if it's a Bible-related query
        if (isBibleQuery(question)) {
            String answer = bibleService.findBiblePassage(question);
            return ResponseEntity.ok(java.util.Map.of(
                "answer", answer,
                "sessionId", sessionId
            ));
        }
        
        // Process the query and get just the answer string
        String answer = factService.queryFactString(question);
        
        return ResponseEntity.ok(java.util.Map.of(
            "answer", answer,
            "sessionId", sessionId
        ));
    }

    @PostMapping("/buildship/store")
    public ResponseEntity<Object> handleBuildshipStore(@RequestBody java.util.Map<String, String> request) {
        String fact = request.get("fact");
        String sessionId = request.get("sessionId");
        
        // Store the fact
        factService.storeFact(fact);
        
        return ResponseEntity.ok(java.util.Map.of(
            "status", "success",
            "message", "Fact stored successfully",
            "sessionId", sessionId
        ));
    }
}