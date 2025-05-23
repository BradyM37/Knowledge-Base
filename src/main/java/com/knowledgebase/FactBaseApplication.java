package com.knowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FactBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactBaseApplication.class, args);
    }
}