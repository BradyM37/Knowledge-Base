# Stage 1: Build the application
FROM eclipse-temurin:21-jdk as build
WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew* ./
COPY gradle gradle
COPY build.gradle ./

# Create empty settings.gradle if it doesn't exist
RUN touch settings.gradle

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build without tests
RUN ./gradlew assemble --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]