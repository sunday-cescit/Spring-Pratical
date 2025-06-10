FROM openjdk:17-jdk-slim as build

WORKDIR /app

COPY target/rest-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
