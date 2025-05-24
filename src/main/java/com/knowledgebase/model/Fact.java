package com.knowledgebase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Fact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100)
    private String subject;
    
    @Column(length = 1000)
    private String content;
    
    public Fact() {
    }
    
    public Fact(String content) {
        this.content = content;
        this.subject = extractSubject(content);
    }
    
    public Fact(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        if (this.subject == null || this.subject.isEmpty()) {
            this.subject = extractSubject(content);
        }
    }
    
    private String extractSubject(String content) {
        // Simple subject extraction - take first few words
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        String[] words = content.split("\\s+");
        if (words.length <= 3) {
            return content;
        }
        
        return String.join(" ", words[0], words[1], words[2]);
    }
}