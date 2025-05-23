package com.knowledgebase.controller;

import com.knowledgebase.service.RenderApiProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiProxyController {

    @Autowired
    private RenderApiProxyService renderApiProxy;
    
    // Handle GET requests
    @GetMapping("/**")
    public ResponseEntity<Object> handleGetRequest(
            @RequestHeader HttpHeaders headers,
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request) {
        
        // Extract the path from the request
        String path = request.getRequestURI().replaceFirst("^/api", "");
        
        // Forward to Render API
        return renderApiProxy.forwardGetRequest(path, queryParams, headers);
    }
    
    // Handle POST requests
    @PostMapping("/**")
    public ResponseEntity<Object> handlePostRequest(
            @RequestBody(required = false) Object body,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        
        // Extract the path from the request
        String path = request.getRequestURI().replaceFirst("^/api", "");
        
        // Forward to Render API
        return renderApiProxy.forwardPostRequest(path, body, headers);
    }
}
