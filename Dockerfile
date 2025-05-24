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