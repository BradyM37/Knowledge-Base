package com.knowledgebase.config;

import com.knowledgebase.service.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Autowired
    private FactService factService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Add some basic facts if they don't exist
            factService.storeQuestionAnswer(
                "What is this application?",
                "This is a knowledge base application that can store facts and answer questions based on stored knowledge."
            );
            
            factService.storeQuestionAnswer(
                "Who created you?",
                "I was created as a knowledge base system to help answer questions efficiently."
            );
            
            factService.storeQuestionAnswer(
                "What can you do?",
                "I can store facts that you tell me, answer questions based on my knowledge, and learn from our interactions."
            );
        };
    }
}