# Use a single-stage build with JRE only
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy only the built JAR file
COPY build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]