# Multi-stage Dockerfile for building and running the Spring Boot jar
# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
# Copy sources
COPY src ./src
# If project uses generated sources or other resources in root, copy them as needed
RUN mvn -B -e -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
