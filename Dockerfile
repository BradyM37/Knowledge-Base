# Stage 1: Build the application
FROM eclipse-temurin:21-jdk as build
WORKDIR /app

# Copy the entire project
COPY . .

# Make gradlew executable and ensure line endings are correct
RUN chmod +x ./gradlew && \
    sed -i 's/\r$//' ./gradlew

# Fix potential BOM in build.gradle
RUN sed -i '1s/^\xEF\xBB\xBF//' build.gradle

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