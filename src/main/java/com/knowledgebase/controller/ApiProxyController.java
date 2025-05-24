package com.knowledgebase.controller;

import com.knowledgebase.model.QueryRequest;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;  // Make sure this import is here

@RestController
@RequestMapping({"/api", ""})
public class ApiProxyController {

    @Autowired
    private FactService factService;

    @PostMapping("/query")
    public ResponseEntity<Object> handleQuery(@RequestBody QueryRequest request) {
        // Process the query directly
        String answer = factService.queryFact(request.getQuery());
        QueryResponse response = new QueryResponse(answer);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/store")
    public ResponseEntity<Object> handleStore(@RequestBody String fact) {
        // Store the fact directly
        factService.storeFact(fact);
        return ResponseEntity.ok().body(Map.of("status", "success", "message", "Fact stored successfully"));
    }

    // Buildship specific endpoints
    @PostMapping("/buildship/query")
    public ResponseEntity<Object> handleBuildshipQuery(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String sessionId = request.get("sessionId");
        
        // Process the query
        String answer = factService.queryFact(question);
        
        return ResponseEntity.ok(Map.of(
            "answer", answer,
            "sessionId", sessionId
        ));
    }

    @PostMapping("/buildship/store")
    public ResponseEntity<Object> handleBuildshipStore(@RequestBody Map<String, String> request) {
        String fact = request.get("fact");
        String sessionId = request.get("sessionId");
        
        // Store the fact
        factService.storeFact(fact);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Fact stored successfully",
            "sessionId", sessionId
        ));
    }
}