# 🏥 Auto-Meds: Smart E-Pharma & Medicine Subscription Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-16+-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Auto-Meds** is a full-stack, enterprise-grade e-pharmacy and recurring prescription management platform. It streamlines recurring medication refills, automated dispatch tracking, alternative medicine suggestions, prescription verification, and comprehensive administrative inventory control.

---

## 🚀 Key Features

### 👨‍⚕️ Patient Portal
- **Browse & Search Catalog**: Find medicines by name, brand, composition, or category.
- **Smart Brand Alternatives**: Automatically suggests active in-stock generic or alternative brands sharing the identical active chemical composition and strength.
- **Prescription Upload & Verification**: Seamless upload of medical prescriptions for doctor/pharmacist approval.
- **Automated Refill Subscriptions**: Subscribe to monthly/periodic medication deliveries with automated refilling cycles.
- **Order Tracking & Notifications**: Real-time order status tracking with in-app notification alerts.

### 🛡️ Admin & Pharmacist Portal
- **Dedicated Secure Admin Portal**: Distinct portal (`/admin/login`) with auto-authenticated administrative access.
- **Real-Time Inventory Management**: Monitor stock levels, low-stock warnings, and instantly increment/decrement warehouse quantities.
- **Prescription & Subscription Approval**: Review uploaded patient prescriptions, match and approve medicine assignments, or request clarifications.
- **Order Fulfillment**: Track and transition order states (`PENDING` ➔ `CONFIRMED` ➔ `DISPATCHED` ➔ `DELIVERED`).

---

## 🛠️ Architecture & Tech Stack

### Backend
- **Framework**: Spring Boot 3 (Java 17 / 20)
- **Web Server**: High-performance Undertow
- **ORM / Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT (JSON Web Tokens)
- **Background Jobs**: Spring `@Scheduled` automated refill processor

### Frontend
- **Framework**: Angular 16+
- **Styling**: Bootstrap 5 + Bootstrap Icons + Custom CSS
- **State & HTTP**: RxJS, Angular Reactive Forms, HTTP Interceptors (JWT & Global Error Handling)

---

## 📂 Project Structure

```text
auto-meds/
├── auto-meds-backend/
│   └── auto-meds-backend/
│       ├── pom.xml
│       └── src/
│           ├── main/java/com/automeds/
│           │   ├── config/          # Security, Data Initialization, Undertow config
│           │   ├── controller/      # REST API endpoints (Auth, Medicines, Cart, Orders, Admin)
│           │   ├── dto/             # Data Transfer Objects
│           │   ├── entity/          # JPA Entities (User, Medicine, Cart, Order, Subscription, etc.)
│           │   ├── repository/      # Spring Data JPA Repositories
│           │   ├── scheduler/       # Automated refill background worker
│           │   └── service/         # Core business logic
│           └── main/resources/
│               ├── application.properties
│               └── schema-postgres.sql
│
└── auto-meds-frontend/
    └── auto-meds-frontend/
        ├── package.json
        ├── angular.json
        └── src/app/
            ├── admin/               # Admin dashboard, inventory, orders, subscriptions
            ├── auth/                # Patient login, register & dedicated Admin portal
            ├── core/                # Guards, interceptors, services, models
            ├── patient/             # Medicines catalog, cart, checkout, orders, subscriptions
            └── shared/              # Navbar, common components
```

---

## ⚙️ Getting Started

### Prerequisites
- **Java 17+**
- **Node.js 18+** & **npm**
- **PostgreSQL 14+**

### 1. Database Setup
Create a PostgreSQL database named `automeds`:
```sql
CREATE DATABASE automeds;
```
Ensure your database credentials in `application.properties` match your local environment:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/automeds
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 2. Run the Backend
```bash
cd auto-meds-backend/auto-meds-backend
mvn spring-boot:run
```
*Backend runs on `http://localhost:8080`.*

### 3. Run the Frontend
```bash
cd auto-meds-frontend/auto-meds-frontend
npm install
npm start
```
*Frontend runs on `http://localhost:4200`.*

---

## 🔐 Demo Credentials

| Role | Portal URL | Email | Password |
| :--- | :--- | :--- | :--- |
| **Admin** | `http://localhost:4200/admin/login` | `admin@automeds.com` | `admin123` *(Pre-filled)* |
| **Patient** | `http://localhost:4200/login` | `patient@automeds.com` | `patient123` |

---

## 👤 Author
**Aaditya Aanand**  
- GitHub: [@Aaditya514](https://github.com/Aaditya514)
