package com.knowledgebase.controller;

import com.knowledgebase.model.BuildshipRequest;
import com.knowledgebase.model.BuildshipResponse;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/buildship")
public class BuildshipIntegrationController {

    @Autowired
    private FactService factService;
    
    @PostMapping("/query")
    public ResponseEntity<BuildshipResponse> handleBuildshipQuery(
            @RequestBody BuildshipRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        // You can implement API key validation here if needed
        
        // Process the query
        QueryResponse queryResponse = factService.queryFact(request.getQuestion());
        
        // Map to Buildship response format
        BuildshipResponse response = new BuildshipResponse(
            queryResponse.getAnswer(),
            queryResponse.getConfidence(),
            queryResponse.getSource(),
            request.getSessionId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/store")
    public ResponseEntity<BuildshipResponse> storeFact(
            @RequestBody BuildshipRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        // Store the fact from the question field
        factService.storeFact(request.getQuestion());
        
        BuildshipResponse response = new BuildshipResponse(
            "Fact stored successfully",
            1.0,
            "Knowledge Base",
            request.getSessionId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/store-qa")
    public ResponseEntity<BuildshipResponse> storeQuestionAnswer(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        String question = payload.get("question");
        String answer = payload.get("answer");
        String sessionId = payload.get("sessionId");
        
        if (question == null || answer == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Store the question and answer
        factService.storeQuestionAnswer(question, answer);
        
        BuildshipResponse response = new BuildshipResponse(
            "Question and answer stored successfully",
            1.0,
            "Knowledge Base",
            sessionId
        );
        
        return ResponseEntity.ok(response);
    }
}