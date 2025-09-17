# FreightOps Backend Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM maven:3.9.4-openjdk-17-slim AS build

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Create non-root user for security
RUN groupadd -r freightops && useradd -r -g freightops freightops

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/freightops && chown -R freightops:freightops /var/log/freightops

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R freightops:freightops /app/uploads

# Change ownership of the app directory
RUN chown -R freightops:freightops /app

# Switch to non-root user
USER freightops

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
