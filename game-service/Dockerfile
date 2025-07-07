# Use the explicitly AMD64 version of OpenJDK
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /usr/src/app
COPY target/game-service.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]