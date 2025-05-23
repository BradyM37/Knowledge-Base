package com.knowledgebase.model;

import lombok.Data;

@Data
public class BuildshipRequest {
    private String userId;
    private String question;
    private String sessionId;
}