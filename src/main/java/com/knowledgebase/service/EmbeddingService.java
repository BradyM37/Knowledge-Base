package com.knowledgebase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Random;

@Service
public class EmbeddingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random(42); // Fixed seed for reproducibility
    
    @Value("${embedding.dimension:100}")
    private int embeddingDimension;

    /**
     * Creates a simple embedding for text using a deterministic approach
     * In a production system, this would be replaced with a proper embedding model
     */
    public double[] createEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new double[embeddingDimension];
        }
        
        // Simple deterministic embedding based on character values
        // This is a placeholder for a real embedding model
        String normalizedText = text.toLowerCase().trim();
        double[] embedding = new double[embeddingDimension];
        
        for (int i = 0; i < normalizedText.length(); i++) {
            int charValue = normalizedText.charAt(i);
            int position = i % embeddingDimension;
            embedding[position] += charValue * 0.01;
        }
        
        // Normalize the vector
        double sum = Arrays.stream(embedding).map(Math::abs).sum();
        if (sum > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= sum;
            }
        }
        
        return embedding;
    }
    
    public String serializeEmbedding(double[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }
    
    public double[] deserializeEmbedding(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson, double[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
    }
    
    public double calculateCosineSimilarity(double[] embedding1, double[] embedding2) {
        RealVector vector1 = new ArrayRealVector(embedding1);
        RealVector vector2 = new ArrayRealVector(embedding2);
        
        return vector1.cosine(vector2);
    }
}