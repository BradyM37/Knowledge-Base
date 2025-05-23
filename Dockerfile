package com.knowledgebase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class RenderApiProxyService {
    
    private static final Logger logger = LoggerFactory.getLogger(RenderApiProxyService.class);
    
    @Value("${api.base-url}")
    private String renderApiBaseUrl;
    
    @Value("${api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public RenderApiProxyService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Forward a GET request to the Render API
     */
    public ResponseEntity<Object> forwardGetRequest(String path, Map<String, String> queryParams, HttpHeaders headers) {
        try {
            // Build URL with query parameters
            StringBuilder urlBuilder = new StringBuilder(renderApiBaseUrl);
            if (!path.startsWith("/")) {
                urlBuilder.append("/");
# Save ApiProxyController.java without BOM
$apiProxyControllerContent | Out-File -FilePath "src\main\java\com\knowledgebase\controller\ApiProxyController.java" -Encoding ascii -NoNewline

# Save RenderApiProxyService.java without BOM
$renderApiProxyServiceContent | Out-File -FilePath "src\main\java\com\knowledgebase\service\RenderApiProxyService.java" -Encoding ascii -NoNewline

# Update Dockerfile to handle BOM characters
@'
# Stage 1: Build the application
FROM eclipse-temurin:21-jdk as build
WORKDIR /app

# Copy the entire project
COPY . .

# Make gradlew executable and ensure line endings are correct
RUN chmod +x ./gradlew && \
    sed -i 's/\r$//' ./gradlew

# Fix BOM characters in all Java files
RUN find src -name "*.java" -exec sed -i '1s/^\xEF\xBB\xBF//' {} \;

# Create a wrapper properties file if it doesn't exist
RUN mkdir -p gradle/wrapper && \
    if [ ! -f gradle/wrapper/gradle-wrapper.properties ]; then \
        echo "distributionBase=GRADLE_USER_HOME" > gradle/wrapper/gradle-wrapper.properties && \
        echo "distributionPath=wrapper/dists" >> gradle/wrapper/gradle-wrapper.properties && \
        echo "distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip" >> gradle/wrapper/gradle-wrapper.properties && \
        echo "networkTimeout=10000" >> gradle/wrapper/gradle-wrapper.properties && \
        echo "zipStoreBase=GRADLE_USER_HOME" >> gradle/wrapper/gradle-wrapper.properties && \
        echo "zipStorePath=wrapper/dists" >> gradle/wrapper/gradle-wrapper.properties; \
    fi

# Download the gradle wrapper if it doesn't exist
RUN if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then \
        mkdir -p gradle/wrapper && \
        curl -o gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/master/gradle/wrapper/gradle-wrapper.jar; \
    fi

# Build with verbose output
RUN ./gradlew bootJar --no-daemon -x test --info

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Set environment variables
ENV SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]