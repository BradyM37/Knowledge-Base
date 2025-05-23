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
    private RenderApiProxyService apiProxyService;
    
    @GetMapping("/**")
    public ResponseEntity<Object> proxyGetRequest(HttpServletRequest request, @RequestParam Map<String, String> queryParams, @RequestHeader HttpHeaders headers) {
        String path = request.getRequestURI().substring("/api".length());
        return apiProxyService.forwardGetRequest(path, queryParams, headers);
    }
    
    @PostMapping("/**")
    public ResponseEntity<Object> proxyPostRequest(HttpServletRequest request, @RequestBody(required = false) Object body, @RequestHeader HttpHeaders headers) {
        String path = request.getRequestURI().substring("/api".length());
        return apiProxyService.forwardPostRequest(path, body, headers);
    }
}