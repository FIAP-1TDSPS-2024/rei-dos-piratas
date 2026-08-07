# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper and project metadata first for dependency caching
COPY --link pom.xml mvnw ./
COPY --link .mvn .mvn/

# Ensure Maven wrapper is executable and download dependencies (offline cache)
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code
COPY --link src ./src/

# Build the application (skip tests compilation and execution for faster build)
RUN ./mvnw package -DskipTests -Dmaven.test.skip=true

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl and create a non-root user
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy the built jar from the build stage
COPY --link --from=build /app/target/*.jar /app/app.jar

# Download Datadog Java agent before switching user (ensures correct permissions)
RUN curl -Lo /app/dd-java-agent.jar https://dtdg.co/latest-java-tracer && \
    chown appuser:appgroup /app/dd-java-agent.jar

USER appuser

# JVM container-aware flags for memory/resource management
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 -XX:+UseContainerSupport"


# Expose the default Spring Boot port
EXPOSE 8080

# Use exec form for proper signal handling
ENTRYPOINT ["sh", "-c", "exec java \
  -javaagent:/app/dd-java-agent.jar \
  -Ddd.service=rei-dos-piratas \
  -Ddd.env=prod \
  -Ddd.version=1.0 \
  -Ddd.agent.host=${DD_AGENT_HOST:-dd-agent} \
  -Ddd.agent.port=8126 \
  -Ddd.profiling.enabled=true \
  -Ddd.logs.injection=true \
  -Ddd.trace.sample.rate=1 \
  $JAVA_OPTS \
  -jar /app/app.jar"]
