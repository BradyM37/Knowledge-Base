package com.knowledgebase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildshipResponse {
    private String answer;
    private double confidence;
    private String source;
    private String sessionId;
}