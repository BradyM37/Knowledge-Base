package com.knowledgebase.service;

import com.knowledgebase.model.Fact;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.repository.FactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FactService {

    @Autowired
    private FactRepository factRepository;
    
    @Value("${knowledge.fallback.response:No answer available}")
    private String fallbackResponse;
    
    private final double similarityThreshold = 0.5;

    /**
     * Store a new fact in the database
     */
    public void storeFact(String factContent) {
        if (factContent == null || factContent.trim().isEmpty()) {
            return;
        }
        
        // Check if fact already exists
        Optional<Fact> existingFact = factRepository.findByContentIgnoreCase(factContent);
        if (existingFact.isPresent()) {
            // Fact already exists, no need to store it again
            return;
        }
        
        // Create and save new fact
        Fact fact = new Fact(factContent);
        factRepository.save(fact);
    }

    /**
     * Query the knowledge base for an answer
     */
    public QueryResponse queryFact(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new QueryResponse(fallbackResponse, 0.0, "No query provided");
        }
        
        // Try direct match first
        Optional<Fact> directMatch = factRepository.findByContentIgnoreCase(query);
        if (directMatch.isPresent()) {
            return new QueryResponse(
                directMatch.get().getContent(),
                1.0,
                "Direct match"
            );
        }
        
        // Try to find facts containing the query terms
        List<Fact> facts = factRepository.findByContentContainingIgnoreCase(query);
        
        if (!facts.isEmpty()) {
            // Return the first matching fact
            return new QueryResponse(
                facts.get(0).getContent(),
                0.8,
                "Partial match"
            );
        }
        
        // Return fallback response if no facts match
        return new QueryResponse(
            fallbackResponse,
            0.0,
            "No match found"
        );
    }
}