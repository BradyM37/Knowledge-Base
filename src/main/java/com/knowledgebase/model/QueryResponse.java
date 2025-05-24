package com.knowledgebase.model;

public class QueryResponse {
    private String answer;
    private double confidence;
    private String source;

    public QueryResponse() {
    }

    public QueryResponse(String answer) {
        this.answer = answer;
        this.confidence = 1.0;
        this.source = "Direct match";
    }
    
    public QueryResponse(String answer, double confidence, String source) {
        this.answer = answer;
        this.confidence = confidence;
        this.source = source;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}