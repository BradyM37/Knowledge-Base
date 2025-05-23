# Stage 1: Build the application
FROM eclipse-temurin:21-jdk as build
WORKDIR /app

# Copy the entire project
COPY . .

# Make gradlew executable and ensure line endings are correct
RUN chmod +x ./gradlew && \
    sed -i 's/\r$//' ./gradlew

# Add duplicate handling strategy to build.gradle if it doesn't already have it
RUN if ! grep -q "duplicatesStrategy" build.gradle; then \
    echo "tasks.withType(Jar) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }" >> build.gradle; \
    fi

# Build without tests
RUN ./gradlew bootJar --no-daemon -x test --stacktrace

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Set environment variables
ENV SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
