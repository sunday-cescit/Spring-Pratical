# Use the explicitly AMD64 version of OpenJDK
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /usr/src/app
COPY target/rest-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]