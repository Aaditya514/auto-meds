# 🏥 Auto-Meds: Smart E-Pharma & Automated Refill Subscription Platform

<div align="center">

  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-16.2-dd0031.svg?logo=angular&logoColor=white)](https://angular.io/)
  [![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169e1.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Java](https://img.shields.io/badge/Java-17%2F20-ED8B00.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
  [![TypeScript](https://img.shields.io/badge/TypeScript-5.1-3178c6.svg?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
  [![JWT](https://img.shields.io/badge/Security-JWT%20Stateless-000000.svg?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
  [![License](https://img.shields.io/badge/License-MIT-success.svg)](LICENSE)

  <p align="center">
    <strong>An intelligent, compliance-driven E-Pharmacy platform automating maintenance medication cycles, clinical prescription verification, and real-time inventory management.</strong>
  </p>

  [Explore Features](#-key-features) •
  [System Architecture](#-system-architecture) •
  [Getting Started](#-getting-started) •
  [API Reference](#-api-endpoints) •
  [Demo Access](#-demo-credentials)

</div>

---

## 📌 Problem Statement & Solution

Millions of patients suffering from chronic health conditions (hypertension, diabetes, cardiac care) require strict adherence to medication routines. Missed doses often occur due to:
1. **Forgetfulness or friction** in remembering to reorder monthly supplies.
2. **Local pharmacy stockouts** without safe, clinically-identical alternatives.
3. **Delayed manual prescription approvals**.

**Auto-Meds** bridges the gap between commercial e-commerce and medical compliance:
- **Set-and-Forget Auto-Refills**: Automatically forecasts refill exhaustion and generates dispatch orders 5 days ahead.
- **Smart Chemical Alternative Engine**: Dynamically identifies generic/substitute brands sharing identical composition & strength when an item is out of stock.
- **Strict Clinical Governance**: Dual-portal architecture segregating patient care from pharmacist verification.

---

## 🚀 Key Features

### 👨‍⚕️ Patient Experience
- **Dynamic Catalog & Smart Search**: Real-time filtering by drug trade name, chemical composition, manufacturer, or ailment category.
- **Composition-Based Alternative Resolution**: If *Metformin 500mg (Brand A)* is out of stock, the system suggests *Metformin 500mg (Brand B)* ranked by price and stock.
- **Digital Prescription Vault**: Secure upload (PDF/Images) with doctor visit tracking and expiration dates.
- **Recurring Medication Subscriptions**: Define dosage intervals (e.g., *2 tablets/day for 30 days*) and receive recurring shipments before depletion.
- **Seamless Cart & Order Tracking**: Instant checkout with Cash on Delivery or Online Payment, backed by real-time notification alerts.

### 🛡️ Admin & Pharmacist Control Center
- **Segregated Admin Gateway**: Isolated entry portal (`/admin/login`) with role-based access control (RBAC).
- **Prescription Triaging & Approval**: Pharmacists review uploaded prescriptions and assign exact catalog formulations, reject invalid scripts, or request clarifications.
- **Real-Time Warehouse Inventory**: Visual inventory dials (In Stock, Low Stock $\le 10$, Out of Stock) with single-click stock increments.
- **Order Fulfillment Pipeline**: Track and advance fulfillment states (`PENDING` ➔ `CONFIRMED` ➔ `DISPATCHED` ➔ `DELIVERED`).
- **Administrative Telemetry**: High-level dashboard tracking active refill pipelines, pending approvals, and low-inventory warnings.

---

## 🏛️ System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│           Angular 16 Single Page Application (Responsive UI)           │
│                                                                        │
│   ┌───────────────────────────────┐   ┌────────────────────────────┐   │
│   │   Patient Portal (/login)     │   │  Admin Portal (/admin)     │   │
│   │   Catalog, Cart, Subscriptions│   │  Inventory, Approvals, RBAC│   │
│   └───────────────┬───────────────┘   └─────────────┬──────────────┘   │
└───────────────────┼─────────────────────────────────┼──────────────────┘
                    │ HTTP REST (JWT Bearer Token)    │
                    ▼                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LAYER                             │
│               Spring Boot 3.1.5 + Undertow High-Performance            │
│                                                                        │
│   ┌─────────────────────┐  ┌─────────────────────┐  ┌──────────────┐   │
│   │ Security / JWT      │  │ Auto-Refill Engine  │  │ Smart Alt    │   │
│   │ Role-Based Filtering│  │ Spring @Scheduled   │  │ Resolver     │   │
│   └─────────────────────┘  └─────────────────────┘  └──────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Spring Data JPA / Hibernate ORM                                │   │
│   └───────────────────────────────┬────────────────────────────────┘   │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ JDBC Connection Pool (HikariCP)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            DATABASE LAYER                              │
│                PostgreSQL 14+ Relational Database Engine               │
│                                                                        │
│   • users           • medicines       • prescriptions                  │
│   • carts           • cart_items      • subscriptions                  │
│   • orders          • order_items     • notifications                  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 💻 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | Angular 16, TypeScript, RxJS, Bootstrap 5, Bootstrap Icons |
| **Backend** | Spring Boot 3.1.5, Java 17+, Undertow Server, Jakarta Persistence |
| **Security** | Spring Security 6, JJWT (io.jsonwebtoken 0.11.5), BCrypt Hashing |
| **Database** | PostgreSQL 14+, HikariCP Connection Pooling, Hibernate ORM 6.2 |
| **Automation** | Spring `@EnableScheduling`, JavaMailSender SMTP Notification |

---

## ⚙️ Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher
- **Node.js**: Version 18+ & **npm**
- **PostgreSQL**: Version 14+ installed and running
- **Apache Maven**: Version 3.8+ (optional, Maven wrapper supported)

### 1. Clone the Repository
```bash
git clone https://github.com/Aaditya514/auto-meds.git
cd auto-meds
```

### 2. Configure Database
Create a PostgreSQL database named `automeds`:
```sql
CREATE DATABASE automeds;
```

Update your database credentials in `auto-meds-backend/auto-meds-backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/automeds
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 3. Launch Spring Boot Backend
```bash
cd auto-meds-backend/auto-meds-backend
mvn clean spring-boot:run
```
> *The backend server boots on `http://localhost:8080`. Initial seed accounts and sample medications are automatically seeded into PostgreSQL on first run.*

### 4. Launch Angular Frontend
In a separate terminal window:
```bash
cd auto-meds-frontend/auto-meds-frontend
npm install
npm start
```
> *Open your browser at `http://localhost:4200`.*

---

## 🔐 Demo Credentials

| Role | Portal URL | Email | Password | Access Highlights |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | [`/admin/login`](http://localhost:4200/admin/login) | `admin@automeds.com` | `admin123` *(Auto-filled)* | Inventory management, prescription review, order pipeline |
| **Patient** | [`/login`](http://localhost:4200/login) | `patient@automeds.com` | `patient123` | Cart checkout, refill subscriptions, prescription upload |

---

## 📡 Core API Endpoints

### 💊 Public & Patient Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user & receive JWT token |
| `POST` | `/api/auth/register` | Register new patient account |
| `GET` | `/api/medicines` | Retrieve all active pharmaceutical items |
| `GET` | `/api/medicines/{id}/alternatives` | Resolve in-stock alternatives by chemical composition |
| `GET` | `/api/subscriptions/my` | View patient's active medication subscriptions |
| `POST` | `/api/subscriptions/create` | Submit prescription & refill subscription request |
| `GET` | `/api/orders/my` | Fetch personal order history & tracking details |

### 🛡️ Administrative Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Aggregated analytics & stock alerts |
| `POST` | `/api/admin/medicines` | Onboard new medicine formulation into inventory |
| `PUT` | `/api/admin/inventory/{id}/stock` | Update warehouse stock count |
| `GET` | `/api/admin/subscription-requests` | View pending patient prescription submissions |
| `PUT` | `/api/admin/subscriptions/{id}/approve` | Pharmacist clinical approval & item assignment |
| `PUT` | `/api/admin/orders/{id}/status` | Advance fulfillment pipeline status |

---

## 👨‍💻 Author

**Aaditya Aanand**
- GitHub: [@Aaditya514](https://github.com/Aaditya514)
- Repository: [https://github.com/Aaditya514/auto-meds](https://github.com/Aaditya514/auto-meds)

---

<div align="center">
  <sub>Built with ❤️ using Spring Boot, Angular, and PostgreSQL.</sub>
</div>
