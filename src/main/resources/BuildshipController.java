package com.yourpackage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yourpackage.service.KnowledgeService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BuildshipController {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildshipController.class);
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    /**
     * Endpoint for Buildship to query knowledge
     * This endpoint will be called by Buildship to get information from your knowledge base
     */
    @GetMapping("/query")
    public ResponseEntity<Object> queryKnowledge(@RequestParam String query) {
        logger.info("Received query from Buildship: {}", query);
        
        try {
            Object result = knowledgeService.findKnowledge(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to process query",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint for Buildship to store new knowledge
     * This endpoint will be called by Buildship to store new information in your knowledge base
     */
    @PostMapping("/store")
    public ResponseEntity<Object> storeKnowledge(@RequestBody Map<String, Object> knowledge) {
        logger.info("Received knowledge to store from Buildship: {}", knowledge);
        
        try {
            Object result = knowledgeService.storeKnowledge(knowledge);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error storing knowledge: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to store knowledge",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Health check endpoint for Buildship
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Knowledge Base API is running"
        ));
    }
}