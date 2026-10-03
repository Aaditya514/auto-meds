# 🏥 Auto-Meds: Smart E-Pharma & Automated Refill Subscription Platform

<div align="center">

  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-16.2-dd0031.svg?logo=angular&logoColor=white)](https://angular.io/)
  [![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169e1.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Java](https://img.shields.io/badge/Java-17-ED8B00.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
  [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
  [![JWT](https://img.shields.io/badge/Security-JWT%20Stateless-000000.svg?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
  [![Accessibility](https://img.shields.io/badge/Accessibility-WCAG%20AA-blueviolet.svg)](https://www.w3.org/WAI/standards-guidelines/wcag/)
  [![License](https://img.shields.io/badge/License-MIT-success.svg)](LICENSE)

  <p align="center">
    <strong>An enterprise-grade, compliance-driven E-Pharmacy platform automating maintenance medication cycles, clinical prescription verification, real-time inventory soft-locking, and caregiver delegation — with built-in WCAG AA accessibility for elderly patients.</strong>
  </p>

  [Features](#-key-features) •
  [Architecture](#-system-architecture) •
  [Getting Started](#-getting-started) •
  [Docker](#-docker-deployment) •
  [API Reference](#-api-endpoints) •
  [Accessibility](#-accessibility-wcag-aa) •
  [Demo Credentials](#-demo-credentials)

</div>

---

## 📌 Problem Statement & Solution

Millions of patients suffering from chronic conditions (hypertension, diabetes, cardiac care) face critical gaps in their medication cycles:
1. **Forgetfulness & Friction** — Missing monthly reorders leads to therapy discontinuation.
2. **Local Pharmacy Stockouts** — Absence of immediate, clinically-safe generic alternative suggestions.
3. **Friction for Elderly & Low-Tech Users** — Inaccessible UI layouts and lack of automated SMS alternatives.
4. **Caregiver Blindspots** — Family members cannot remotely manage prescriptions and deliveries for dependent parents.

**Auto-Meds** bridges the gap between commercial e-commerce convenience and medical compliance:
- **Set-and-Forget Auto-Refills** with distributed ShedLock scheduling and 5-day predictive dispatch cycles.
- **SMS Refill Confirmation** — Interactive refill confirmation via Twilio SMS ("Reply YES") with zero app requirement.
- **Async OCR Engine** — Dedicated thread-pool prescription analysis extracting medicines without blocking HTTP threads.
- **Caffeine In-Memory Caching** — High-throughput catalog queries with automatic cache eviction on inventory updates.
- **Generic Alternative Engine** — Automatic Bio-equivalent drug substitution matching identical chemical composition and dosage strength.
- **Near-Expiry & Batch Traceability** — Proactive 90-day expiry triage and manufacturer batch tracking for recall compliance.
- **Full Caregiver Delegation** — Family members can link patient profiles to supervise prescriptions and orders.
- **Real-Time SSE Feeds** — Live server-sent events for instant order status progression and stock alerts.
- **WCAG AA Accessibility** — 44px minimum touch targets, high-contrast focus rings, font scaling, and reduced-motion support.

---

## 🚀 Key Features

### 👨‍⚕️ Patient & Caregiver Experience
- **Dynamic Catalog & Smart Search**: Real-time multi-attribute search across trade names, composition, manufacturer, and ailments.
- **Composition-Based Alternative Resolution**: If an item is out of stock, suggests bio-equivalent alternatives ranked by price.
- **Digital Prescription Vault**: Secure upload with OCR processing, physician tracking, and expiration validation.
- **Recurring Medication Subscriptions**: Configurable dosage schedules (e.g., 2 tablets/day for 30 days) with automated refill pipelines.
- **Interactive SMS Refill Alerts**: Patients can confirm auto-refills straight from standard SMS without opening a browser.
- **Caregiver Delegation**: Link caregiver accounts to manage medication, upload prescriptions, and track deliveries for elderly family members.
- **Clinical Drug Interaction Checker**: Instant safety checks across active medication regimens to prevent adverse drug events.

### 🛡️ Admin & Pharmacist Control Center
- **Prescription Triaging & Async OCR**: Automated text extraction with pharmacist approval/rejection workflows and dosage validation.
- **Near-Expiry Dashboard (H5)**: Proactive surveillance filtering inventory expiring within 90 days, prioritized by expiration date.
- **Batch Number Recall Traceability**: Every stock unit tracks manufacturer batch numbers for regulatory audit standards.
- **Procurement & Deficit Queues**: Automated allocation of incoming inventory to waiting chronic subscribers upon restocking.
- **Real-Time SSE Notification Bus**: Instant dashboard updates for orders, low-stock warnings, and refill cycles.
- **Dispensing Slip Generation**: Clinical packing slips with medication details, batch codes, and pharmacist sign-offs.
- **Immutable Audit Trail**: Structured audit logging for all prescription approvals, modifications, and stock adjustments.

### ⚙️ Platform & Engineering Highlights
- **Distributed Scheduling (ShedLock)**: Safe multi-replica background execution for cron jobs and auto-refill triggers.
- **Asynchronous Execution (`@Async`)**: Non-blocking OCR thread pools isolated from web request threads.
- **In-Memory Caching (Caffeine)**: Low-latency caching on active medicine catalogs with automatic cache eviction on mutations.
- **Stateless Security (JWT + Refresh Tokens)**: Fine-grained Role-Based Access Control (`PATIENT`, `ADMIN`).
- **Production Containerization**: Multi-stage Docker builds with NGINX reverse proxy, Undertow web server, and PostgreSQL.

---

## 🏛️ System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│           Angular 16 SPA  ←─── NGINX Reverse Proxy (Port 80)           │
│                                                                        │
│   ┌───────────────────────────────┐   ┌────────────────────────────┐   │
│   │   Patient & Caregiver Portal  │   │   Pharmacist Admin Portal  │   │
│   │   Catalog, Cart, Subscriptions│   │   Triaging, Inventory, SSE │   │
│   └───────────────┬───────────────┘   └─────────────┬──────────────┘   │
└───────────────────┼─────────────────────────────────┼──────────────────┘
                    │ HTTP REST / SSE (JWT Bearer)    │
                    ▼                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LAYER                             │
│             Spring Boot 3.1.5 + Undertow High-Performance Server       │
│                                                                        │
│   ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐   │
│   │ Security Filter  │  │ Refill Scheduler │  │ Async OCR Worker   │   │
│   │ JWT Stateless    │  │ ShedLock Clustered│ │ ThreadPoolExecutor │   │
│   └──────────────────┘  └──────────────────┘  └────────────────────┘   │
│                                                                        │
│   ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐   │
│   │ Caffeine Cache   │  │ SSE Event Stream │  │ Twilio SMS Gateway │   │
│   │ Medicine Catalog │  │ SseEmitter Bus   │  │ Refill Verification│   │
│   └──────────────────┘  └──────────────────┘  └────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Spring Data JPA + Hibernate ORM (Flyway Migrations)            │   │
│   └───────────────────────────────┬────────────────────────────────┘   │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ HikariCP Connection Pool
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            DATABASE LAYER                              │
│                      PostgreSQL 14+ Relational Engine                  │
│                                                                        │
│   • users           • medicines (batch/expiry)  • prescriptions        │
│   • carts & items   • subscriptions             • orders & audit_logs  │
│   • shedlock        • caregiver_links           • notifications        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 💻 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | Angular 16, TypeScript 5.1, RxJS 7.8, Bootstrap 5, Bootstrap Icons, WCAG AA Accessibility CSS |
| **Backend** | Spring Boot 3.1.5, Java 17 LTS, Undertow Server, Spring Security 6, Spring Data JPA |
| **Database** | PostgreSQL 14+, HikariCP Connection Pool, Flyway Database Migrations |
| **Caching & Scheduling** | Caffeine Cache, ShedLock (Distributed lock over JDBC) |
| **Asynchronous & Realtime** | Spring `@Async` ThreadPoolTaskExecutor, Server-Sent Events (`SseEmitter`) |
| **Integrations** | Twilio SMS API, Tesseract OCR Engine, JavaMailSender SMTP |
| **DevOps & Containers** | Docker (Multi-stage build), Docker Compose, NGINX Alpine Reverse Proxy |

---

## ⚙️ Getting Started

### Prerequisites
- **JDK 17+**
- **Node.js 18+** & **npm**
- **PostgreSQL 14+** (or use Docker Compose)
- **Maven 3.8+** (or use included `mvnw`)

---

### Option A: Docker Compose (Recommended)

Run the entire multi-tier system with a single command:

```bash
git clone https://github.com/Aaditya514/auto-meds.git
cd auto-meds
docker compose up --build
```

| Service | Endpoint | Description |
|---|---|---|
| **Frontend Web App** | http://localhost | Patient & Admin Portals via NGINX |
| **Backend REST API** | http://localhost/api | Spring Boot API via Reverse Proxy |
| **Direct Backend** | http://localhost:8080 | Direct Undertow HTTP Server |
| **Actuator Health** | http://localhost:8080/actuator/health | Service Health & Diagnostics |

---

### Option B: Local Development

#### 1. Setup Database
```sql
CREATE DATABASE automeds;
```

Configure credentials in `auto-meds-backend/auto-meds-backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/automeds
spring.datasource.username=postgres
spring.datasource.password=your_password
```

#### 2. Start Backend
```bash
cd auto-meds-backend/auto-meds-backend
mvn clean spring-boot:run
```
> Boots on `http://localhost:8080`. Flyway automatically executes migrations and populates seed data.

#### 3. Start Frontend
```bash
cd auto-meds-frontend/auto-meds-frontend
npm install
npm start
```
> Web application starts on `http://localhost:4200`.

---

## 🐳 Docker Deployment & Environment Variables

See [DOCKER.md](DOCKER.md) for detailed container architecture and configuration.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/automeds` | Database connection string |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(Internal Secret)* | HMAC signing key for JWT tokens |
| `SMS_ENABLED` | `false` | Enable Twilio SMS notifications (`true`/`false`) |
| `TWILIO_ACCOUNT_SID` | — | Twilio Account SID |
| `TWILIO_AUTH_TOKEN` | — | Twilio Auth Token |
| `TWILIO_FROM_NUMBER` | — | Twilio Registered Phone Number |
| `MAIL_USERNAME` | — | SMTP mail sender address |
| `MAIL_PASSWORD` | — | SMTP app password |

---

## 🔐 Demo Credentials

| Role | Portal URL | Email | Password | Access Highlights |
| :--- | :--- | :--- | :--- | :--- |
| **Pharmacist Admin** | [`/admin/login`](http://localhost/admin/login) | `admin@automeds.com` | `admin123` *(Auto-fill available)* | Inventory, Near-Expiry alerts, Batch codes, Approvals |
| **Patient** | [`/login`](http://localhost/login) | `patient@automeds.com` | `patient123` | Medicine catalog, Subscriptions, Prescriptions, Cart |

---

## 📡 API Endpoints

### 🔐 Authentication
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate & retrieve JWT access and refresh tokens |
| `POST` | `/api/auth/register` | Register new patient account |
| `POST` | `/api/auth/refresh` | Refresh expired access token |

### 💊 Medicines & Catalog (Patient)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/medicines` | Retrieve active medicines (**Caffeine-cached**) |
| `GET` | `/api/medicines/{id}` | Detailed medicine info |
| `GET` | `/api/medicines/search?q=` | Multi-attribute search across composition, brand, ailment |
| `GET` | `/api/medicines/{id}/alternatives` | Resolve bio-equivalent alternatives by chemical composition |

### 📋 Prescriptions & Subscriptions
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/prescriptions/upload` | Upload prescription (PDF/Image) triggering **Async OCR** |
| `GET` | `/api/subscriptions/my` | Retrieve patient's active refill subscriptions |
| `POST` | `/api/subscriptions/create` | Create a recurring subscription request |

### 🛒 Cart & Orders
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/cart` | View patient shopping cart |
| `POST` | `/api/cart/add` | Add medication item to cart |
| `POST` | `/api/orders/checkout` | Submit order (Cash on Delivery / Online) |
| `GET` | `/api/orders/my` | View personal order history |

### 🔔 Real-Time Streams (SSE)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/sse/subscribe` | Connect to live Server-Sent Events notification channel |

### 🛡️ Administrative & Inventory Operations
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Aggregated dashboard KPIs and stock health |
| `GET` | `/api/admin/subscription-requests` | View pending prescription submissions |
| `PUT` | `/api/admin/subscriptions/{id}/approve` | Approve subscription & allocate inventory |
| `PUT` | `/api/admin/subscriptions/{id}/reject` | Reject request with reason |
| `POST` | `/api/admin/medicines` | Onboard new medicine with batch number & expiry date |
| `PUT` | `/api/admin/inventory/{id}/stock` | Quick-update inventory stock level |
| `GET` | `/api/admin/inventory/near-expiry` | **Near-expiry alert queue** (expiring within 90 days) |
| `GET` | `/api/admin/procurement/alerts` | Procurements alerts and deficit backlog queue |
| `POST` | `/api/admin/procurement/restock` | Restock inventory and auto-fulfill waiting subscriptions |
| `PUT` | `/api/admin/orders/{id}/status` | Advance fulfillment status (`PENDING` ➔ `DELIVERED`) |
| `POST` | `/api/admin/scheduler/trigger-refill` | Manually execute auto-refill evaluation cycle |

### 🩺 Clinical Safety & Caregiver
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/clinical/drug-interactions` | Check interactions for a combination of medications |
| `GET` | `/api/audit-logs` | Retrieve full clinical and administrative audit trail |
| `POST` | `/api/caregiver/link` | Link caregiver authorization to a patient account |
| `GET` | `/api/caregiver/patients` | Retrieve patients managed under active caregiver profile |

---

## ♿ Accessibility (WCAG AA)

Auto-Meds provides first-class support for elderly and low-vision patients:
- **44×44px Minimum Touch Targets**: Complies with Apple HIG and Google Material Design standard for motor impairment.
- **Visible Focus Rings**: High-contrast outline (`3px solid`) for seamless keyboard tab navigation.
- **Dynamic Font Scaling**: Native scaling tokens via `data-font-size="large"` and `"xlarge"` attributes.
- **WCAG AA Color Contrast**: All text, status badges, and interactive controls satisfy a contrast ratio $\ge 4.5:1$.
- **Motion Reduction**: Honored via `@media (prefers-reduced-motion: reduce)` disabling non-essential transitions.
- **Windows High Contrast Mode**: Out-of-the-box support using `@media (forced-colors: active)`.

---

## 📊 Engineering Decisions & Trade-Offs

| Architecture Decision | Selection | Rationale |
| :--- | :--- | :--- |
| **Web Server** | Undertow over Tomcat | Up to 3× lower memory footprint and superior handling of high-concurrency SSE connections. |
| **Catalog Caching** | Caffeine over Redis | Zero network latency and zero infrastructure overhead; eviction hooks maintain strict consistency. |
| **Distributed Locks** | ShedLock over raw `@Scheduled` | Prevents duplicate refill order generation across multi-replica container deployments. |
| **Prescription OCR** | Asynchronous Thread Pool | OCR text parsing (2–6s) runs in a background thread pool without blocking the HTTP request thread. |
| **Real-time Notifications** | SSE over WebSocket | Lightweight, unidirectional server-to-client push; seamless integration with standard HTTP/HTTPS proxies. |
| **SMS Gateway** | Twilio with toggle flag | Fallback mechanism for non-smartphone users; toggleable via environment variables for easy local testing. |
| **Database Migrations** | Flyway Versioned Migrations | Declarative, reproducible database schema management ensuring identical environments across dev and production. |

---

## 👨‍💻 Author

**Aaditya Aanand**
- GitHub: [@Aaditya514](https://github.com/Aaditya514)
- Repository: [https://github.com/Aaditya514/auto-meds](https://github.com/Aaditya514/auto-meds)

---

<div align="center">
  <sub>Built with ❤️ using Spring Boot, Angular, PostgreSQL, Docker, and Twilio.</sub>
</div>
