# ==============================================================================
# Auto-Meds Multi-Stage Production Dockerfile with Tesseract OCR (Linux)
# ==============================================================================

# Stage 1: Build Java Backend Application
FROM maven:3.9.6-eclipse-temurin-20 AS backend-build
WORKDIR /app/backend
COPY auto-meds-backend/auto-meds-backend/pom.xml .
COPY auto-meds-backend/auto-meds-backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime with Linux Tesseract OCR Engine
FROM eclipse-temurin:20-jre
WORKDIR /app

LABEL maintainer="Auto-Meds Engineering Team"
LABEL description="Smart E-Pharma Automated Refill Platform with Linux Tesseract OCR"

# Install Native Tesseract OCR Engine and English trained language models
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Verify Tesseract installation
RUN tesseract --version

# Create non-root system user for security
RUN groupadd -r automeds && useradd -r -g automeds automeds
RUN mkdir -p /app/uploads/prescriptions && chown -R automeds:automeds /app

USER automeds

# Copy compiled executable JAR from build stage
COPY --from=backend-build --chown=automeds:automeds /app/backend/target/auto-meds-backend-*.jar app.jar

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Health check probe
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
