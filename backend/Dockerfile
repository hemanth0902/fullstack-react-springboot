
FROM eclipse-temurin:17-jre-alpine

RUN apk update && apk upgrade --no-cache

WORKDIR /app

COPY target/fullstack-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
