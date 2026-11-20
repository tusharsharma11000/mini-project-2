# ── Stage 1: Build the JAR ──────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache dependencies first (layer caching optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create directories for persistent storage
RUN mkdir -p /app/data /app/uploads

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Railway will map
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Xms256m -Xmx512m -jar app.jar --server.port=${PORT:-8080}"]
