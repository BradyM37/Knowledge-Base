FROM gradle:8.4-jdk21 as build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
# List files to verify content
RUN ls -la
# Show Java version
RUN java -version
# Run Gradle with debug info
RUN ./gradlew build --no-daemon --info --stacktrace

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]