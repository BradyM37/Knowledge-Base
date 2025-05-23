package com.knowledgebase.service;

import com.knowledgebase.model.Fact;
import com.knowledgebase.model.QueryResponse;
import com.knowledgebase.repository.FactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FactService {

    @Autowired
    private FactRepository factRepository;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private KnowledgeEnhancementService enhancementService;
    
    @Value("${embedding.similarity.threshold:0.7}")
    private double similarityThreshold;
    
    private static final Pattern FACT_PATTERN = Pattern.compile("(?i)(?:my|our|the|their)\\s+([\\w\\s]+)\\s+(?:is|are|was|were)\\s+([\\w\\s,]+)");
    private static final Pattern QUESTION_PATTERN = Pattern.compile("(?i)(?:what|who|where|when|how|why)\\s+(?:is|are|was|were|do|does|did)\\s+([\\w\\s?]+)");
    
    public Fact storeFact(String content) {
        // Check if this fact already exists
        Optional<Fact> existingFact = factRepository.findByContentIgnoreCase(content);
        if (existingFact.isPresent()) {
            return existingFact.get();
        }
        
        Fact fact = new Fact();
        fact.setContent(content);
        
        // Extract subject-predicate-object if possible
        Matcher factMatcher = FACT_PATTERN.matcher(content);
        if (factMatcher.find()) {
            fact.setType("FACT");
            fact.setSubject(factMatcher.group(1).trim());
            fact.setPredicate("is");
            fact.setObject(factMatcher.group(2).trim());
        } else {
            Matcher questionMatcher = QUESTION_PATTERN.matcher(content);
            if (questionMatcher.find()) {
                fact.setType("QUESTION");
                fact.setSubject(questionMatcher.group(1).trim());
            } else {
                fact.setType("GENERAL");
            }
        }
        
        // Create and store embedding
        double[] embedding = embeddingService.createEmbedding(content);
        fact.setEmbeddingJson(embeddingService.serializeEmbedding(embedding));
        
        return factRepository.save(fact);
    }
    
    public Fact storeQuestionAnswer(String question, String answer) {
        // Check if this question already exists
        Optional<Fact> existingFact = factRepository.findByContentIgnoreCase(question);
        if (existingFact.isPresent()) {
            Fact fact = existingFact.get();
            fact.setAnswer(answer);
            return factRepository.save(fact);
        }
        
        Fact fact = new Fact();
        fact.setContent(question);
        fact.setAnswer(answer);
        fact.setType("QUESTION");
        
        // Create and store embedding
        double[] embedding = embeddingService.createEmbedding(question);
        fact.setEmbeddingJson(embeddingService.serializeEmbedding(embedding));
        
        return factRepository.save(fact);
    }
    
    public QueryResponse queryFact(String query) {
        // First try direct match
        Optional<Fact> directMatch = factRepository.findByContentIgnoreCase(query);
        if (directMatch.isPresent() && directMatch.get().getAnswer() != null) {
            return new QueryResponse(
                directMatch.get().getAnswer(),
                1.0,
                "Direct match"
            );
        }
        
        // Try to extract subject from query
        String subject = extractSubjectFromQuery(query);
        if (subject != null && !subject.isEmpty()) {
            List<Fact> subjectMatches = factRepository.findBySubjectContainingIgnoreCase(subject);
            if (!subjectMatches.isEmpty()) {
                Fact bestMatch = subjectMatches.get(0);
                return new QueryResponse(
                    bestMatch.getObject() != null ? bestMatch.getObject() : bestMatch.getContent(),
                    0.9,
                    "Subject match: " + subject
                );
            }
        }
        
        // Use embedding similarity search
        QueryResponse similarityResponse = findBySimilarity(query);
        
        // If we couldn't find a good match, try to enhance the response
        if (similarityResponse.getConfidence() < similarityThreshold) {
            String enhancedAnswer = enhancementService.enhanceResponse(query, similarityResponse.getAnswer());
            similarityResponse.setAnswer(enhancedAnswer);
            if (!enhancedAnswer.equals(similarityResponse.getAnswer())) {
                similarityResponse.setSource("Enhanced response");
                similarityResponse.setConfidence(0.8); // Set a reasonable confidence for enhanced responses
            }
        }
        
        return similarityResponse;
    }
    
    private String extractSubjectFromQuery(String query) {
        // Extract subject from questions like "What is my favorite color?"
        Pattern favoritePattern = Pattern.compile("(?i)what(?:'s| is| are) (?:my|our|the|their) ([\\w\\s]+)\\??");
        Matcher matcher = favoritePattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Extract subject from general questions
        Pattern generalPattern = Pattern.compile("(?i)(?:what|who|where|when|how|why) (?:is|are|was|were) ([\\w\\s]+)\\??");
        matcher = generalPattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    private QueryResponse findBySimilarity(String query) {
        double[] queryEmbedding = embeddingService.createEmbedding(query);
        List<Fact> allFacts = factRepository.findAll();
        
        Fact bestMatch = null;
        double highestSimilarity = -1;
        
        for (Fact fact : allFacts) {
            if (fact.getEmbeddingJson() == null) continue;
            
            double[] factEmbedding = embeddingService.deserializeEmbedding(fact.getEmbeddingJson());
            double similarity = embeddingService.calculateCosineSimilarity(queryEmbedding, factEmbedding);
            
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatch = fact;
            }
        }
        
        if (bestMatch != null && highestSimilarity >= similarityThreshold) {
            String answer = bestMatch.getAnswer();
            if (answer == null || answer.isEmpty()) {
                if (bestMatch.getObject() != null) {
                    answer = bestMatch.getObject();
                } else {
                    answer = bestMatch.getContent();
                }
            }
            
            return new QueryResponse(
                answer,
                highestSimilarity,
                "Similarity match: " + bestMatch.getContent()
            );
        }
        
        return new QueryResponse(
            "I don't have information about that.",
            0.0,
            "No match found"
        );
    }
    
    public List<Fact> getAllFacts() {
        return factRepository.findAll();
    }
    
    public Optional<Fact> getFactById(Long id) {
        return factRepository.findById(id);
    }
    
    public void deleteFact(Long id) {
        factRepository.deleteById(id);
    }
}