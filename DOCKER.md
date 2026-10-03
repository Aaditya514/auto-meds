# Auto-Meds Production Containerization & Deployment Guide (`DOCKER.md`)

> Complete, enterprise-grade Docker containerization for the Auto-Meds E-Pharma platform.
> Orchestrates **Angular 16 (NGINX Reverse Proxy)**, **Spring Boot 3 (Java 17 LTS + Linux Tesseract OCR)**, and **PostgreSQL 16**.

---

## 1. Container Architecture Overview

```
                      ┌────────────────────────────────────────┐
                      │             Client Browser             │
                      └───────────────────┬────────────────────┘
                                          │  Port 80 / 4200
                                          ▼
                      ┌────────────────────────────────────────┐
                      │    frontend (automeds-frontend)        │
                      │  • Alpine NGINX Reverse Proxy          │
                      │  • Static Asset Caching (1yr)          │
                      │  • Angular SPA HTML5 Fallback          │
                      │  • 25MB Body Size for Prescription Rx  │
                      └───────────────────┬────────────────────┘
                                          │
                   /api/ proxy forwarding │ Docker Network (`automeds-net`)
                                          ▼
                      ┌────────────────────────────────────────┐
                      │    backend (automeds-backend)          │
                      │  • Spring Boot 3 on Java 17 LTS        │
                      │  • Native Linux Tesseract OCR Engine   │
                      │  • Non-root system user (`automeds`)   │
                      │  • Flyway DB Migrations (V1..V5)       │
                      │  • Spring Actuator Health Probes       │
                      └───────────────────┬────────────────────┘
                                          │
                         JDBC Connection  │ Internal Port 5432
                                          ▼
                      ┌────────────────────────────────────────┐
                      │    postgres (automeds-postgres)        │
                      │  • PostgreSQL 16 Alpine                │
                      │  • Named volume: `automeds-pgdata`     │
                      │  • Named volume: `automeds-uploads`    │
                      └────────────────────────────────────────┘
```

---

## 2. Quick Start (1-Command Startup)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.
- Docker Compose v2.0+ (`docker compose version`).

### Launch the Entire Stack
From the project root directory:

```bash
# Build images and start all 3 services in detached mode
docker compose up --build -d
```

### Accessing the Portals
Once the health probes pass:
- **Patient & Pharmacist Web App**: [http://localhost](http://localhost) (or [http://localhost:4200](http://localhost:4200))
- **Spring Boot Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **PostgreSQL Database**: `localhost:5432` (`user: postgres`, `password: postgres`, `db: automeds`)

---

## 3. Service Details

### A. Frontend (`automeds-frontend`)
- **Base Image**: `node:18-alpine` (builder) $\rightarrow$ `nginx:1.25-alpine` (runtime).
- **Features**:
  - Automatically resolves API calls using relative path `/api/` (proxied to `backend:8080/api/`), eliminating all CORS concerns.
  - SPA routing with `try_files $uri $uri/ /index.html`.
  - Max upload size set to `25M` for high-resolution prescription images and multi-page PDFs.
  - Immutable 1-year caching for fingerprinted assets; `no-cache` on `index.html`.

### B. Backend (`automeds-backend`)
- **Base Image**: `maven:3.9.6-eclipse-temurin-17` (builder) $\rightarrow$ `eclipse-temurin:17-jre` (runtime).
- **Features**:
  - Embedded native Linux `tesseract-ocr` and `tesseract-ocr-eng` packages for real-time prescription OCR parsing.
  - Multi-stage build isolates Maven dependencies and produces an optimized ~350MB production image.
  - Runs under an unprivileged `automeds` service user for container security.
  - Health checks monitored by Spring Boot Actuator (`/actuator/health`).

### C. Database (`automeds-postgres`)
- **Base Image**: `postgres:16-alpine`.
- **Features**:
  - Automatically runs schema validation and Flyway migrations on initial boot.
  - Health-checked using `pg_isready -U postgres -d automeds`.
  - Persists data to named volume `automeds-pgdata`.

---

## 4. Operational Commands & Maintenance

### Checking Service Health
```bash
docker compose ps
```
*Expected status for all three containers is `healthy` or `running`.*

### Viewing Real-Time Logs
```bash
# Tail all container logs
docker compose logs -f

# Tail backend logs specifically (e.g. OCR parsing, scheduler)
docker compose logs -f backend

# Tail NGINX access and error logs
docker compose logs -f frontend
```

### Stopping the Services
```bash
# Graceful shutdown (preserves all database and prescription data)
docker compose down

# Teardown including volumes (WARNING: removes all database data)
docker compose down -v
```

### Rebuilding a Single Container
```bash
# Rebuild only the frontend after UI edits
docker compose up --build -d frontend

# Rebuild only the backend after Java edits
docker compose up --build -d backend
```

---

## 5. Environment Variables & Production Secrets

For cloud production environments (AWS, GCP, Azure, DigitalOcean), override the following in an `.env` file or orchestrator secrets manager:

| Environment Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://postgres:5432/automeds` | PostgreSQL JDBC connection string |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password (must be rotated in production) |
| `APP_JWT_SECRET` | *(64-character hex key)* | HMAC-SHA256 signature secret for JWT access tokens |
| `APP_JWT_EXPIRATION_MS` | `86400000` (24h) | Access token validity duration |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Boot active profile |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host for patient dispatch & refill alerts |
| `MAIL_USERNAME` | — | SMTP username / API key |
| `MAIL_PASSWORD` | — | SMTP app password |
