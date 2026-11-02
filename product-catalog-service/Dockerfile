# ==========================================
# STAGE 1: BUILD
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Copy source code
COPY src ./src

# Build the Spring Boot application
RUN ./mvnw -B clean package -DskipTests


# ==========================================
# STAGE 2: RUNTIME
# ==========================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated JAR from build stage
COPY --from=build --chown=10001:10001 /app/target/*.jar app.jar

# Run as non-root user
USER 10001:10001

# Spring Boot application port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
