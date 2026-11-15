# 📓 Architecture & Product Decision Log (ADR): Auto-Meds

> **Document Type:** Architecture & Product Decision Records (ADRs)  
> **Target Audience:** Engineering Team, System Architects, Technical Program Managers (TPMs)  
> **Timeline / Perspective:** Pre-Development & Inception Design Records  
> **Author:** Aaditya Aanand  

---

## 1. Context & Decision Standards

Before writing application code or committing to specific frameworks, the engineering and product team established these Architecture Decision Records (ADRs). Each record captures:
* **The Problem:** The specific user or architectural challenge.
* **Alternatives Considered:** Options evaluated during initial technical spikes.
* **The Decision:** The selected design pattern or technology.
* **Trade-Offs Accepted:** Known compromises acknowledged at design time.

---

## 2. Master Decision Records

* [ADR-001: Two-Tier Inventory Reservation Architecture](#adr-001-two-tier-inventory-reservation-architecture)
* [ADR-002: Anti-Panic UX — Binary Stock Masking over Exact Quantities](#adr-002-anti-panic-ux--binary-stock-masking-over-exact-quantities)
* [ADR-003: Undertow Server over Apache Tomcat for Lightweight Memory Footprint](#adr-003-undertow-server-over-apache-tomcat-for-lightweight-memory-footprint)
* [ADR-004: In-Memory Caffeine Cache over Distributed Redis](#adr-004-in-memory-caffeine-cache-over-distributed-redis)
* [ADR-005: Clustered Scheduling using ShedLock for Multi-Instance Deployments](#adr-005-clustered-scheduling-using-shedlock-for-multi-instance-deployments)
* [ADR-006: Asynchronous Prescription OCR via Dedicated Thread Pool](#adr-006-asynchronous-prescription-ocr-via-dedicated-thread-pool)
* [ADR-007: Server-Sent Events (SSE) over Full-Duplex WebSockets](#adr-007-server-sent-events-sse-over-full-duplex-websockets)
* [ADR-008: Declarative Database Schema Migrations via Liquibase SQL](#adr-008-declarative-database-schema-migrations-via-liquibase-sql)

---

### ADR-001: Two-Tier Inventory Reservation Architecture

* **Context:** In chronic medication care, missing a scheduled refill because an item suddenly went out of stock can cause severe clinical regressions. In an e-pharmacy that sells to both one-time walk-in buyers and recurring chronic subscribers, walk-in shoppers can unexpectedly buy up stock needed for tomorrow's scheduled chronic refills.
* **Alternatives Considered:**
  1. *Immediate Hard Stock Deduction:* Subtract inventory from warehouse counts 5 days before dispatch.
  2. *Just-in-Time Allocation:* Check and allocate stock only on the exact morning of packaging and dispatch.
  3. *Two-Tier Reservation:* Separate physical stock (`stock_quantity`) from reserved stock (`reserved_quantity`). Available stock is calculated dynamically:
     $$\text{available\_for\_sale} = \text{stock\_quantity} - \text{reserved\_quantity}$$
* **Decision:** **Option 3 (Two-Tier Reservation).**
* **Trade-Offs Accepted:** Requires transactional synchronization across all inventory writes. If a patient modifies or cancels their refill during the confirmation period, the system must explicitly release `reserved_quantity`.
* **Expected Outcome:** Guaranteed stock for chronic patients without creating phantom inventory discrepancies during physical warehouse stock audits.

---

### ADR-002: Anti-Panic UX — Binary Stock Masking over Exact Quantities

* **Context:** Most retail e-commerce stores display exact stock levels to create urgency (e.g., *"Only 2 items left!"*). In healthcare, seeing dwindling stock numbers for life-critical medications causes panic buying, medicine hoarding, and severe patient anxiety.
* **Alternatives Considered:**
  1. *Display Exact Stock Numbers:* Show exact warehouse unit counts on product cards.
  2. *Urgency Badges:* Display "Limited stock remaining" banners.
  3. *Binary Availability Display:* Show only a clean, simple binary status (`In Stock` vs `Out of Stock`). When stock is depleted, disable the order button and immediately suggest an identical generic alternative.
* **Decision:** **Option 3 (Binary Availability Display).**
* **Trade-Offs Accepted:** Gives up artificial urgency marketing tactics common in consumer retail.
* **Expected Outcome:** Eliminates hoarding behaviors, keeps patients calm, and protects pharmacy inventory numbers from competitor web scrapers.

---

### ADR-003: Undertow Server over Apache Tomcat for Lightweight Memory Footprint

* **Context:** The platform will maintain persistent Server-Sent Events (SSE) connections to push live status updates to pharmacist queues and patient dashboards. Standard Apache Tomcat creates thread-per-connection overhead that rapidly consumes memory on cloud servers with limited RAM (e.g., AWS EC2 t2.micro free tier).
* **Alternatives Considered:**
  1. *Apache Tomcat (Spring Boot Default)*
  2. *Jetty*
  3. *Undertow (XNIO Non-Blocking High-Throughput Server)*
* **Decision:** **Option 3 (Undertow).** Exclude Tomcat and configure Undertow as the embedded web server.
* **Trade-Offs Accepted:** Minor differences in thread configuration properties compared to standard Tomcat tutorials.
* **Expected Outcome:** Over 40% reduction in idle memory usage, allowing the full Spring Boot application, PostgreSQL database, and NGINX proxy to run comfortably on a single 1GB RAM cloud server without running out of memory.

---

### ADR-004: In-Memory Caffeine Cache over Distributed Redis

* **Context:** Catalog searches and generic medicine lookups are read-heavy (95% queries, 5% writes). We need high-speed caching without increasing cloud infrastructure costs or adding another external server dependency.
* **Alternatives Considered:**
  1. *Standalone Redis Cluster:* External caching server over network sockets.
  2. *Direct Database Queries:* Query the relational database on every search request.
  3. *In-Memory Caffeine Cache:* High-performance local in-process cache with event-driven cache invalidation.
* **Decision:** **Option 3 (Caffeine Cache).**
* **Trade-Offs Accepted:** Cache is local to the running server process. In a multi-server setup, cache invalidation must be broadcasted or handled with short expiration times.
* **Expected Outcome:** Near-zero lookup latency ($<2\text{ms}$) with zero extra infrastructure hosting fees and zero external network hops.

---

### ADR-005: Clustered Scheduling using ShedLock for Multi-Instance Deployments

* **Context:** The chronic refill engine runs background daily evaluations. If the backend is deployed across multiple container replicas or horizontally scaled instances, standard Spring `@Scheduled` annotations would trigger simultaneously on all instances, causing duplicate orders, duplicate SMS messages, and double billing.
* **Alternatives Considered:**
  1. *Single Instance Deployment Constraint:* Restrict the application to run strictly on one server container.
  2. *Quartz Clustered Scheduler:* Heavyweight database tables and complex enterprise setup.
  3. *ShedLock Database Lock Table:* Lightweight distributed database table lock (`shedlock`) that ensures only one node acquires the execution lock at any given time.
* **Decision:** **Option 3 (ShedLock).**
* **Trade-Offs Accepted:** Requires maintaining a dedicated `shedlock` database table.
* **Expected Outcome:** Safe horizontal scaling without fear of duplicate refill processing or duplicate notifications.

---

### ADR-006: Asynchronous Prescription OCR via Dedicated Thread Pool

* **Context:** Extracting text from uploaded prescription photos using OCR is computationally heavy and takes between 2 to 6 seconds per image. Running OCR inside the main web request thread would block incoming HTTP connections, lead to gateway timeouts, and degrade platform responsiveness for other users.
* **Alternatives Considered:**
  1. *Synchronous In-Request OCR:* Parse the image before returning an HTTP response.
  2. *External Commercial OCR APIs (AWS Textract / Google Cloud Vision):* Incurs per-scan third-party API fees and introduces external network dependencies.
  3. *Asynchronous Background Thread Pool:* Immediately return a success response (`Upload Received`), process the image in a dedicated background worker pool, and stream the extracted text via SSE once complete.
* **Decision:** **Option 3 (Asynchronous Background Thread Pool).**
* **Trade-Offs Accepted:** Prescription initially shows an intermediate status (`PROCESSING_OCR`) while the background worker finishes parsing.
* **Expected Outcome:** Web response times remain under 100ms, while compute-intensive OCR runs in the background without affecting general browsing or checkout performance.

---

### ADR-007: Server-Sent Events (SSE) over Full-Duplex WebSockets

* **Context:** Pharmacist triage desks and patient delivery progress bars need real-time status updates whenever an order advances or new prescriptions arrive.
* **Alternatives Considered:**
  1. *Short Polling:* Browser sends a new request every 3 seconds (heavy network waste).
  2. *Full-Duplex WebSockets:* Bidirectional socket protocol upgrade.
  3. *Server-Sent Events (SSE):* Unidirectional, browser-standard event stream over standard HTTP.
* **Decision:** **Option 3 (Server-Sent Events).**
* **Trade-Offs Accepted:** SSE is one-way (server to client). Client actions (approvals, button clicks) continue to use standard REST API requests.
* **Expected Outcome:** Automatic reconnection built into modern browsers, zero protocol upgrade complications, seamless operation through NGINX and corporate firewalls, and minimal server memory overhead.

---

### ADR-008: Declarative Database Schema Migrations via Liquibase SQL

* **Context:** Using automatic database schema generation (`hibernate.ddl-auto=update` or `create-drop`) in a healthcare platform risks accidental data loss, schema discrepancies between development and production, and lost clinical audit logs.
* **Alternatives Considered:**
  1. *Hibernate Auto-DDL:* Easy during rapid prototyping, dangerous in production.
  2. *Manual SQL Scripts run via Database GUI:* High human error rate, no version control tracking.
  3. *Version-Controlled Liquibase Changelogs:* Declarative, auditable SQL changelogs tracked directly in Git.
* **Decision:** **Option 3 (Liquibase SQL Changelogs).**
* **Trade-Offs Accepted:** Developers must write structured forward and rollback migration files for every table, index, or constraint change.
* **Expected Outcome:** Safe, reproducible, and verifiable database migrations across local, Docker, and cloud deployments with a complete change history.
