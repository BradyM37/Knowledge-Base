package com.knowledgebase.controller;

import com.knowledgebase.model.Fact;
import com.knowledgebase.model.QueryRequest;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/facts")
public class FactController {

    @Autowired
    private FactService factService;
    
    @PostMapping
    public ResponseEntity<Fact> createFact(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Fact savedFact = factService.storeFact(content);
        return ResponseEntity.ok(savedFact);
    }
    
    @PostMapping("/qa")
    public ResponseEntity<Fact> createQuestionAnswer(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = payload.get("answer");
        
        if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Fact savedFact = factService.storeQuestionAnswer(question, answer);
        return ResponseEntity.ok(savedFact);
    }
    
    @PostMapping("/query")
    public ResponseEntity<QueryResponse> queryFact(@RequestBody QueryRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        QueryResponse response = factService.queryFact(request.getQuery());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<Fact>> getAllFacts() {
        List<Fact> facts = factService.getAllFacts();
        return ResponseEntity.ok(facts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Fact> getFactById(@PathVariable Long id) {
        Optional<Fact> fact = factService.getFactById(id);
        return fact.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFact(@PathVariable Long id) {
        factService.deleteFact(id);
        return ResponseEntity.noContent().build();
    }
}