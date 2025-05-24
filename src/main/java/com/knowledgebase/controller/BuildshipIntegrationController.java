package com.knowledgebase.controller;

import com.knowledgebase.model.BuildshipRequest;
import com.knowledgebase.model.BuildshipResponse;
import com.knowledgebase.service.FactService;
import com.knowledgebase.service.RenderApiProxyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/buildship")
public class BuildshipIntegrationController {

    @Autowired
    private FactService factService;
    
    @Autowired
    private RenderApiProxyService renderApiProxy;
    
    @PostMapping("/query")
    public ResponseEntity<Object> handleBuildshipQuery(
            @RequestBody BuildshipRequest request,
            @RequestHeader HttpHeaders headers) {
        
        // Forward the request to the Render API
        return renderApiProxy.forwardPostRequest("/api/buildship/query", request, headers);
    }
    
    @PostMapping("/store")
    public ResponseEntity<Object> storeFact(
            @RequestBody BuildshipRequest request,
            @RequestHeader HttpHeaders headers) {
        
        // Forward the request to the Render API
        return renderApiProxy.forwardPostRequest("/api/buildship/store", request, headers);
    }
    
    @PostMapping("/store-qa")
    public ResponseEntity<Object> storeQuestionAnswer(
            @RequestBody Map<String, String> payload,
            @RequestHeader HttpHeaders headers) {
        
        // Forward the request to the Render API
        return renderApiProxy.forwardPostRequest("/api/buildship/store-qa", payload, headers);
    }
    
    // Add a fallback endpoint to handle any other requests
    @RequestMapping("/**")
    public ResponseEntity<Object> handleAnyRequest(
            @RequestBody(required = false) Object body,
            @RequestHeader HttpHeaders headers,
            @RequestParam Map<String, String> queryParams) {
        
        // Get the path from the request
        String path = "/api/buildship" + headers.getFirst("X-Original-URI");
        
        if (body != null) {
            return renderApiProxy.forwardPostRequest(path, body, headers);
        } else {
            return renderApiProxy.forwardGetRequest(path, queryParams, headers);
        }
    }
}