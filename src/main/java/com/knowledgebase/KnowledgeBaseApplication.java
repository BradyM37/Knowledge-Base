package com.knowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseApplication.class, args);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            // Get port from environment variable or use 10000 as default (Render's default port)
            String port = System.getenv("PORT");
            int portNumber = port != null ? Integer.parseInt(port) : 10000;
            
            System.out.println("Setting server port to: " + portNumber);
            factory.setPort(portNumber);
        };
    }
}