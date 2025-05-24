package com.knowledgebase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class RenderApiProxyService {
    
    private static final Logger logger = LoggerFactory.getLogger(RenderApiProxyService.class);
    
    @Value("${api.base-url:}")
    private String renderApiBaseUrl;
    
    @Value("${api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public RenderApiProxyService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Forward a GET request to the Render API
     */
    public ResponseEntity<Object> forwardGetRequest(String path, Map<String, String> queryParams, HttpHeaders headers) {
        try {
            // Check if base URL is configured
            if (renderApiBaseUrl == null || renderApiBaseUrl.isEmpty()) {
                logger.warn("API base URL is not configured. Cannot forward request.");
                return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.singletonMap("error", "API forwarding is not configured"));
            }
            
            // Build URL with query parameters
            StringBuilder urlBuilder = new StringBuilder(renderApiBaseUrl);
            if (!path.startsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(path);
            
            // Rest of the method remains the same...
            
            // Add API key if available
            addApiKeyToHeaders(headers);
            
            // Create request entity
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
            
            logger.info("Forwarding GET request to: {}", urlBuilder.toString());
            
            // Execute request
            ResponseEntity<Object> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                requestEntity,
                Object.class
            );
            
            return response;
        } catch (Exception e) {
            logger.error("Error forwarding GET request: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to forward request: " + e.getMessage()));
        }
    }
    
    /**
     * Forward a POST request to the Render API
     */
    public ResponseEntity<Object> forwardPostRequest(String path, Object body, HttpHeaders headers) {
        try {
            // Check if base URL is configured
            if (renderApiBaseUrl == null || renderApiBaseUrl.isEmpty()) {
                logger.warn("API base URL is not configured. Cannot forward request.");
                return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.singletonMap("error", "API forwarding is not configured"));
            }
            
            // Build URL
            StringBuilder urlBuilder = new StringBuilder(renderApiBaseUrl);
            if (!path.startsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(path);
            
            // Add API key if available
            addApiKeyToHeaders(headers);
            
            // Create request entity
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
            
            logger.info("Forwarding POST request to: {}", urlBuilder.toString());
            
            // Execute request
            ResponseEntity<Object> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.POST,
                requestEntity,
                Object.class
            );
            
            return response;
        } catch (Exception e) {
            logger.error("Error forwarding POST request: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to forward request: " + e.getMessage()));
        }
    }
    
    private void addApiKeyToHeaders(HttpHeaders headers) {
        if (apiKey != null && !apiKey.isEmpty() && !headers.containsKey("Authorization")) {
            headers.set("Authorization", "Bearer " + apiKey);
        }
    }
}