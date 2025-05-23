package com.knowledgebase.service;

import com.knowledgebase.model.Fact;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KnowledgeEnhancementService {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeEnhancementService.class);
    
    @Autowired
    private FactService factService;
    
    @Value("${knowledge.preload.enabled:true}")
    private boolean preloadEnabled;
    
    @Value("${knowledge.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    @Value("${knowledge.fallback.response:I don't have specific information about that yet.}")
    private String fallbackResponse;
    
    @Value("${external.api.enabled:false}")
    private boolean externalApiEnabled;
    
    @Value("${external.api.url:}")
    private String externalApiUrl;
    
    @Value("${external.api.key:}")
    private String externalApiKey;
    
    @Value("${external.api.timeout:5000}")
    private int externalApiTimeout;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @PostConstruct
    public void init() {
        if (preloadEnabled) {
            preloadKnowledge();
        }
    }
    
    private void preloadKnowledge() {
        logger.info("Preloading knowledge base with common facts...");
        
        // General knowledge facts
        storeCommonFact("How long is the Bible?", "The Bible is approximately 1,200 pages long in standard print, containing around 750,000 words across 66 books.");
        storeCommonFact("What is the capital of France?", "The capital of France is Paris.");
        storeCommonFact("How tall is Mount Everest?", "Mount Everest is 29,032 feet (8,849 meters) tall, making it the Earth's highest mountain above sea level.");
        storeCommonFact("Who wrote Hamlet?", "William Shakespeare wrote Hamlet around 1600-1601.");
        storeCommonFact("What is the speed of light?", "The speed of light in vacuum is 299,792,458 meters per second (approximately 186,282 miles per second).");
        storeCommonFact("What is the distance from Earth to the Moon?", "The average distance from Earth to the Moon is about 238,855 miles (384,400 kilometers).");
        storeCommonFact("How many elements are in the periodic table?", "There are 118 elements in the modern periodic table, with elements 1-94 occurring naturally and the rest being synthetic.");
        storeCommonFact("What is the population of Earth?", "As of 2023, the world population is approximately 8 billion people.");
        storeCommonFact("What is the largest ocean?", "The Pacific Ocean is the largest and deepest ocean on Earth, covering more than 60 million square miles.");
        storeCommonFact("What is the smallest country in the world?", "Vatican City is the smallest country in the world, with an area of approximately 49 hectares (121 acres).");
        
        // Technology facts
        storeCommonFact("What is an API?", "An API (Application Programming Interface) is a set of rules and protocols that allows different software applications to communicate with each other.");
        storeCommonFact("What is machine learning?", "Machine learning is a branch of artificial intelligence focused on building systems that learn from data, identify patterns, and make decisions with minimal human intervention.");
        storeCommonFact("What is cloud computing?", "Cloud computing is the delivery of computing services—including servers, storage, databases, networking, software, analytics, and intelligence—over the Internet ('the cloud') to offer faster innovation, flexible resources, and economies of scale.");
        
        // Health facts
        storeCommonFact("How much water should I drink daily?", "The U.S. National Academies of Sciences, Engineering, and Medicine recommends about 3.7 liters (125 ounces) of fluids per day for men and 2.7 liters (91 ounces) for women, though individual needs may vary.");
        storeCommonFact("How much sleep do adults need?", "Most adults need 7-9 hours of sleep per night for optimal health, according to the National Sleep Foundation.");
        
        logger.info("Knowledge base preloaded successfully");
    }
    
    private void storeCommonFact(String question, String answer) {
        try {
            factService.storeQuestionAnswer(question, answer);
        } catch (Exception e) {
            logger.error("Error storing common fact: " + question, e);
        }
    }
    
    public String enhanceResponse(String query, String initialResponse) {
        if (!initialResponse.equals(fallbackResponse) || !fallbackEnabled) {
            return initialResponse;
        }
        
        // If external API is enabled, try to get a response from there
        if (externalApiEnabled && !externalApiUrl.isEmpty()) {
            try {
                String externalResponse = queryExternalApi(query);
                if (externalResponse != null && !externalResponse.isEmpty()) {
                    // Store this new knowledge for future use
                    CompletableFuture.runAsync(() -> {
                        factService.storeQuestionAnswer(query, externalResponse);
                    });
                    return externalResponse;
                }
            } catch (Exception e) {
                logger.error("Error querying external API", e);
            }
        }
        
        return fallbackResponse;
    }
    
    private String queryExternalApi(String query) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (!externalApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + externalApiKey);
            }
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", query);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            CompletableFuture<ResponseEntity<Map>> future = CompletableFuture.supplyAsync(() -> {
                return restTemplate.postForEntity(externalApiUrl, request, Map.class);
            });
            
            ResponseEntity<Map> response = future.get(externalApiTimeout, TimeUnit.MILLISECONDS);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("answer");
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error in external API call", e);
            return null;
        }
    }
}