# 1. Stage de build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Stage d'exécution
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/service-notifications-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]