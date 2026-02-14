# Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle.kts ./build.gradle.kts
COPY settings.gradle.kts ./settings.gradle.kts
COPY backend/build.gradle.kts ./backend/build.gradle.kts

RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew :backend:dependencies --no-daemon || true

# Copy source code
COPY backend/src ./backend/src

# Build the application
RUN ./gradlew :backend:bootJar --no-daemon -x test -x spotlessCheck

# Runtime
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Create a non-root user
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Copy the built JAR
COPY --from=builder /app/backend/build/libs/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
