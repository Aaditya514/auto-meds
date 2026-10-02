# 📦 Phase 2: High-Concurrency Inventory Soft-Lock, ShedLock & Flyway Migrations

**Document Status:** ✅ Completed & Verified  
**Milestone:** Concurrency Control, 5-Day Soft-Lock Engine, Distributed Scheduler & Database Migrations  
**Date:** October 2, 2026  
**Primary Source Directives:**
- `SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md` (Sections 2.B, 3.A, 3.B, and User Prompts 4 & 5)
- `PRODUCTION_ROADMAP.md` (Phase 2: Database Integrity & Concurrency)
- `DISCUSSION_NOTES.md` (Data Model & Schema Evolution)

**Associated Core Files:**
- `Medicine.java`
- `MedicineDTO.java`
- `MedicineRepository.java`
- `MedicineService.java`
- `Subscription.java`
- `SubscriptionResponseDTO.java`
- `SubscriptionRepository.java`
- `SubscriptionService.java`
- `CartService.java`
- `OrderService.java`
- `AutoRefillScheduler.java`
- `ShedLockConfig.java`
- `InventorySoftLockTest.java`
- `V1__init_schema.sql`
- `V2__inventory_reservation_and_indexes.sql`
- `application.properties`
- `pom.xml`

---

## ⚡ The 2-Minute Executive Gist

If someone wants to understand Phase 2 at a glance:

> **In a sentence:** Guided directly by the senior engineering transcript in `SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md`, we eliminated critical inventory race conditions, stopped walk-in shoppers from stealing chronic patients' upcoming refills via a **5-day automated inventory soft-lock engine**, replaced Hibernate's hazardous `ddl-auto=update` with version-controlled **Flyway migrations**, converted multi-step read-modify-write loops into **atomic SQL conditional decrements**, and guarded automated refill batch jobs across multi-pod deployments using **ShedLock distributed locking**.

### 📊 Before vs. After Snapshot

| Dimension | Before Phase 2 (Prototype) | After Phase 2 (Senior Engineering Standard) |
|---|---|---|
| **Chronic Care Refill Guarantee** | No reservation; walk-in e-commerce shoppers could deplete physical stock right before a chronic subscriber's scheduled delivery. | **5-Day Inventory Soft-Lock**: Subscriptions automatically lock needed stock 5 days prior to `nextRefillDate`. Reserved stock is invisible to walk-in cart shoppers. |
| **Available Stock Calculation** | Stock checks only evaluated raw `stock_quantity`. | Dynamic availability: `availableQuantity = max(0, stock - reservedQuantity)`. Cart additions and checkout enforce availability. |
| **Stock Deduction Concurrency** | Read entity into memory, calculate `newStock = stock - qty`, and call `save()`. Highly vulnerable to Time-of-Check to Time-of-Use (TOCTOU) race conditions under concurrent checkouts. | **Atomic SQL Conditional Decrements**: Single-statement DB updates: `UPDATE medicines SET stock = stock - :qty WHERE id = :id AND (stock - reserved_quantity) >= :qty`. Negative inventory is physically impossible. |
| **Multi-Node Job Scheduling** | Simple `@Scheduled` cron; running 2 or more backend containers/pods caused duplicate refill orders and multi-billed patient credit cards. | **ShedLock Distributed Locking**: Backed by PostgreSQL `shedlock` table. Exactly one container acquires the cluster lock (`AutoRefillScheduler_processAutoRefills`). |
| **Database Schema Evolution** | Risky Hibernate `ddl-auto=update` running on production boot; no audit trail, no rollback, risk of silent schema drift. | **Flyway Versioned Migrations**: Explicit SQL migration scripts (`V1__init_schema.sql`, `V2__inventory_reservation_and_indexes.sql`) with `ddl-auto=validate`. |
| **Reorder & Deficit Monitoring** | No automated detection of impending stockouts or deficit subscriptions. | Native tracking: `reorder_threshold`, `suggested_reorder_pack_size`, `OUT_OF_STOCK_DEFICIT` state alerts for pharmacy procurement. |
| **Test Verification** | 167 tests passing. | **174 tests passing (0 failures, 0 errors)** including comprehensive concurrency and soft-lock scenario test suite. |

---

## 🎯 Part 1: What Was Required (Scope & Analysis from Senior Blueprint)

In clinical pharmacy operations, running out of chronic medications (such as Insulin, Metformin, or Anti-hypertensives) can trigger severe health emergencies. An automated pharmacy cannot treat chronic recurring refills the same as one-off impulse cart purchases.

The scope for Phase 2 was derived directly from **`SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md` (Sections 2.B, 3.A, 3.B, and User Prompts 4 & 5)**:

### Identified Vulnerabilities Solved:
1. **The Walk-In Stockout Collision (`Transcript §3.A & Prompt 4`)**:
   - When a patient's monthly chronic subscription is due on day 30, a walk-in shopper on day 29 could purchase the remaining units. When the night batch scheduler runs, the chronic order fails or ships late.
   - *Requirement:* Implement a 5-day soft-lock window pre-allocating inventory (`reserved_quantity`) so walk-in shoppers cannot add reserved stock to their carts.
2. **Time-of-Check to Time-of-Use (TOCTOU) Race Conditions (`Transcript §3.A & §3.B`)**:
   - In `OrderService.java`, stock was fetched via `medicineRepository.findById(id)`, checked in Java, decremented, and saved.
   - If two customers checked out simultaneously with 1 item remaining in stock, both passed the Java check and both decremented, resulting in negative inventory and oversold stock.
   - *Requirement:* Atomic SQL decrements with database-level boundary constraints and pessimistic locking.
3. **Multi-Pod Duplicate Order Duplication (`Transcript §2.B & Prompt 5`)**:
   - In Kubernetes or horizontally scaled cloud deployments, running `@Scheduled` executes concurrently on every replica at midnight, duplicating refill orders, shipments, and customer charges.
   - *Requirement:* Implement ShedLock with a database lock provider to guarantee cluster-wide single execution.
4. **Schema Fragility & Uncontrolled DDL (`Transcript §2.B`)**:
   - Relying on Spring Boot's `spring.jpa.hibernate.ddl-auto=update` in staging or production is dangerous and non-compliant with standard database governance.
   - *Requirement:* Establish versioned Flyway migrations (`V1` and `V2`) and shift Hibernate to `validate`.
5. **Subscription Cancellation Inventory Leak (`Transcript §3.A`)**:
   - If a patient cancels or pauses a subscription that already has inventory locked, the reserved stock must be instantly released back to general availability.

---

## 🛠️ Part 2: What Was Done in Detail (Implementation Breakdown)

### 1. Data Model Enhancements for Inventory Reservation
- **Files:** `Medicine.java`, `MedicineDTO.java`, `Subscription.java`, `SubscriptionResponseDTO.java`
- **Transcript Directives:** Section 3.A & User Prompt 4
- **Changes Made:**
  - Extended `Medicine.java` with three essential clinical procurement fields:
    ```java
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "reorder_threshold", nullable = false)
    private Integer reorderThreshold = 10;

    @Column(name = "suggested_reorder_pack_size", nullable = false)
    private Integer suggestedReorderPackSize = 50;

    public Integer getAvailableQuantity() {
        if (stock == null) return 0;
        int reserved = (reservedQuantity != null) ? reservedQuantity : 0;
        return Math.max(0, stock - reserved);
    }
    ```
  - Extended `Subscription.java` with reservation state tracking:
    ```java
    public enum ReservationStatus {
        NONE,
        RESERVED,
        OUT_OF_STOCK_DEFICIT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus = ReservationStatus.NONE;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;
    ```
  - Mapped all new properties in `MedicineDTO.java`, `MedicineService.java`, and `SubscriptionResponseDTO.java` to ensure frontend dashboards and administrative consoles can display reserved vs. available units.

---

### 2. High-Performance Atomic SQL Concurrency & Pessimistic Locks
- **File:** `MedicineRepository.java`
- **Transcript Directives:** Section 3.B
- **Changes Made:**
  - Implemented atomic conditional updates that execute directly at the database engine level, eliminating race conditions entirely:
    ```java
    // 1. Atomic decrement for walk-in / regular checkouts (only consumes unreserved stock)
    @Modifying
    @Query("UPDATE Medicine m SET m.stock = m.stock - :quantity WHERE m.id = :id AND (m.stock - m.reservedQuantity) >= :quantity")
    int deductAvailableStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 2. Atomic decrement for reserved chronic subscription refills (decrements both stock & reserved pool)
    @Modifying
    @Query("UPDATE Medicine m SET m.stock = m.stock - :quantity, m.reservedQuantity = m.reservedQuantity - :quantity WHERE m.id = :id AND m.reservedQuantity >= :quantity AND m.stock >= :quantity")
    int deductReservedStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 3. Atomic reservation (locks stock 5 days prior to auto-refill execution)
    @Modifying
    @Query("UPDATE Medicine m SET m.reservedQuantity = m.reservedQuantity + :quantity WHERE m.id = :id AND (m.stock - m.reservedQuantity) >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 4. Atomic release (releases stock when subscription is cancelled or paused)
    @Modifying
    @Query("UPDATE Medicine m SET m.reservedQuantity = CASE WHEN m.reservedQuantity >= :quantity THEN m.reservedQuantity - :quantity ELSE 0 END WHERE m.id = :id")
    int releaseReservedStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 5. Pessimistic Write Lock for batch operations requiring explicit row-level synchronization
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medicine m WHERE m.id = :id")
    Optional<Medicine> findByIdForUpdate(@Param("id") Long id);
    ```

---

### 3. Cart & Checkout Availability Enforcement
- **Files:** `CartService.java`, `OrderService.java`
- **Transcript Directives:** Section 3.A
- **Changes Made:**
  - **Cart Guarding (`CartService.java`):**
    - Updated `addItemToCart` and `updateCartItemQuantity` to evaluate `medicine.getAvailableQuantity()` instead of total stock:
      ```java
      int available = medicine.getAvailableQuantity();
      if (available < request.getQuantity()) {
          throw new InsufficientStockException("Insufficient stock available for " + medicine.getName() + 
                  ". Available (unreserved): " + available + ", Requested: " + request.getQuantity());
      }
      ```
  - **Order Checkout Atomic Deduction (`OrderService.java`):**
    - In `createOrderFromCart`, replaced entity mutation with `deductAvailableStock`:
      ```java
      int rowsUpdated = medicineRepository.deductAvailableStock(medicine.getId(), item.getQuantity());
      if (rowsUpdated == 0) {
          throw new InsufficientStockException("Stock unavailable for " + medicine.getName() + " during checkout confirmation.");
      }
      ```
    - In `createRefillOrder`, differentiated between subscriptions that have active soft-locks vs. non-reserved ones:
      ```java
      if (subscription.getReservationStatus() == Subscription.ReservationStatus.RESERVED) {
          int rows = medicineRepository.deductReservedStock(medicine.getId(), quantity);
          if (rows == 0) {
              throw new InsufficientStockException("Reserved stock unavailable for refill order.");
          }
          subscription.setReservationStatus(Subscription.ReservationStatus.NONE);
      } else {
          int rows = medicineRepository.deductAvailableStock(medicine.getId(), quantity);
          if (rows == 0) {
              throw new InsufficientStockException("Insufficient stock available for refill order.");
          }
      }
      ```

---

### 4. 5-Day Soft-Lock Scheduler & Subscription Lifecycle Hook
- **Files:** `AutoRefillScheduler.java`, `SubscriptionService.java`
- **Transcript Directives:** Section 3.A & User Prompt 4
- **Changes Made:**
  - In `AutoRefillScheduler.java`, structured the automated workflow into two distinct sequential phases:
    1. **5-Day Pre-Allocation Window (`reserveInventoryForSubscription`):**
       - Identifies all active subscriptions due within the next 5 days (`nextRefillDate <= today + 5 days`) whose reservation status is `NONE`.
       - Calls atomic `medicineRepository.reserveStock(medicineId, quantity)`.
       - If reservation succeeds: Sets `reservationStatus = RESERVED` and `reservationDate = LocalDate.now()`.
       - If stock is insufficient: Sets `reservationStatus = OUT_OF_STOCK_DEFICIT`, records an emergency warning in logs, and alerts clinical staff without throwing uncaught exceptions.
    2. **Due Date Refill Dispatch (`processDueRefills`):**
       - Processes all subscriptions due today (`nextRefillDate <= today`), generates the official patient order, and fulfills the order using `deductReservedStock`.
  - In `SubscriptionService.java`:
    - Added reservation release on cancellation and pausing:
      ```java
      if (subscription.getReservationStatus() == Subscription.ReservationStatus.RESERVED) {
          medicineRepository.releaseReservedStock(
              subscription.getMedicine().getId(), 
              subscription.getQuantity()
          );
          subscription.setReservationStatus(Subscription.ReservationStatus.NONE);
          subscription.setReservationDate(null);
      }
      ```

---

### 5. Multi-Node Distributed Scheduler Guard via ShedLock
- **Files Created/Updated:** `ShedLockConfig.java`, `AutoRefillScheduler.java`, `pom.xml`
- **Transcript Directives:** Section 2.B & User Prompt 5
- **Changes Made:**
  - Added ShedLock Spring and JDBC dependencies in `pom.xml`:
    - `shedlock-spring` (v5.10.0)
    - `shedlock-provider-jdbc-template` (v5.10.0)
  - Created `ShedLockConfig.java`:
    ```java
    @Configuration
    @EnableSchedulerLock(defaultLockAtMostFor = "10m")
    public class ShedLockConfig {
        @Bean
        public LockProvider lockProvider(DataSource dataSource) {
            return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                    .withJdbcTemplate(new JdbcTemplate(dataSource))
                    .usingDbTime()
                    .build()
            );
        }
    }
    ```
  - Decorated `AutoRefillScheduler.processAutoRefills()` with `@SchedulerLock`:
    ```java
    @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM daily
    @SchedulerLock(name = "AutoRefillScheduler_processAutoRefills", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    @Transactional
    public void processAutoRefills() { ... }
    ```
  - Created distributed coordination table `shedlock` in PostgreSQL:
    ```sql
    CREATE TABLE shedlock (
        name VARCHAR(64) NOT NULL,
        lock_until TIMESTAMP NOT NULL,
        locked_at TIMESTAMP NOT NULL,
        locked_by VARCHAR(255) NOT NULL,
        PRIMARY KEY (name)
    );
    ```

---

### 6. Production-Ready Flyway Database Migrations
- **Files Created/Updated:** `V1__init_schema.sql`, `V2__inventory_reservation_and_indexes.sql`, `application.properties`, `pom.xml`
- **Transcript Directives:** Section 2.B
- **Changes Made:**
  - Added `flyway-core` to `pom.xml`.
  - Configured Flyway in `application.properties`:
    ```properties
    spring.flyway.enabled=true
    spring.flyway.baseline-on-migrate=true
    spring.flyway.baseline-version=0
    spring.flyway.locations=classpath:db/migration
    spring.jpa.hibernate.ddl-auto=validate
    ```
  - **`V1__init_schema.sql` (Baseline Schema):**
    - Full clean DDL for all core tables: `users`, `medicines`, `cart`, `cart_items`, `orders`, `order_items`, `subscriptions`, `prescriptions`, `refresh_tokens`, `notifications`, and `shedlock`.
  - **`V2__inventory_reservation_and_indexes.sql` (Soft-Lock & Index Evolution):**
    - Added columns: `reserved_quantity`, `reorder_threshold`, `suggested_reorder_pack_size` to `medicines`.
    - Added columns: `reservation_status`, `reservation_date` to `subscriptions`.
    - Added composite performance indexes for query and lock optimization:
      ```sql
      CREATE INDEX IF NOT EXISTS idx_medicines_stock_reserved ON medicines (stock, reserved_quantity);
      CREATE INDEX IF NOT EXISTS idx_subscriptions_refill_status ON subscriptions (status, reservation_status, next_refill_date);
      CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens (user_id);
      CREATE INDEX IF NOT EXISTS idx_orders_patient_status ON orders (patient_id, status);
      ```

---

## 🧪 Part 3: Quality Gates & Automated Verification Evidence

### 1. Dedicated Concurrency & Soft-Lock Test Suite
- **File:** `InventorySoftLockTest.java`
- Created 7 rigorous scenario tests verifying all Phase 2 business rules:
  1. `testAvailableQuantityComputation()`: Verifies `stock - reservedQuantity` formula and zero-floor bounding.
  2. `testSoftLockReservationFiveDaysAhead()`: Validates that subscriptions due within 5 days have stock reserved and are tagged `RESERVED`.
  3. `testSoftLockInsufficientStockTriggersDeficit()`: Validates that stock deficits flag `OUT_OF_STOCK_DEFICIT` cleanly.
  4. `testCartCannotPurchaseReservedStock()`: Verifies that walk-in shoppers attempting to buy reserved stock receive an `InsufficientStockException`.
  5. `testFulfillmentDeductsReservedStock()`: Verifies that order dispatch decrements both total stock and reserved count atomically.
  6. `testSubscriptionCancellationReleasesReservedStock()`: Verifies that cancelling or pausing a reserved subscription releases the lock immediately.
  7. `testFindMedicinesNeedingReorder()`: Verifies that the automated procurement query correctly detects inventory at or below `reorder_threshold`.

### 2. Full Test Suite Execution (All 174 Tests Passing)
```bash
mvn test
```
```text
[INFO] Running com.automeds.service.InventorySoftLockTest
2026-10-02T15:31:26.582+05:30  WARN 2904 --- [ main] c.a.scheduler.AutoRefillScheduler : Could not reserve 30 units of Metformin 500mg for subscription #100. Deficit recorded.
2026-10-02T15:31:26.603+05:30  INFO 2904 --- [ main] c.a.scheduler.AutoRefillScheduler : Successfully soft-locked 30 units of Metformin 500mg for subscription #100
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.085 s - in com.automeds.service.InventorySoftLockTest
...
[INFO] Results:
[INFO] Tests run: 174, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  39.630 s
```

---

## ⏭️ Part 4: Preview of Next Phase

### Phase 3: Clinical Prescription OCR, Image Processing & Verification State Machine
Directly addressing **Sections 4.A, 4.B, and User Prompts 2 & 6 of `SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md`**:
1. **Multi-Format Prescription Ingestion**: Support PDF and high-resolution images (JPEG, PNG, WebP) with virus scanning and magic byte validation.
2. **Clinical Verification State Machine**: Strict status transitions (`PENDING` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `VERIFIED` / `REJECTED` / `CLARIFICATION_REQUESTED`) with audit logging.
3. **OCR Integration Layer**: Optical Character Recognition engine to extract doctor credentials, patient details, and medication dosages automatically.
4. **Prescription Expiry & Schedule H/X Drug Compliance**: Automatic enforcement of Indian CDSCO Schedule H/X regulations, preventing automated dispensations of restricted narcotics or expired prescriptions.
