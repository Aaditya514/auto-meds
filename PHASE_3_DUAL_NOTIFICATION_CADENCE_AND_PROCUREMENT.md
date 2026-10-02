# 📢 Phase 3: Dual Notification Cadence & Closed-Loop Procurement Desk

**Document Status:** ✅ Completed & Verified  
**Milestone:** Proactive Staff & Patient Alert Fan-Out, Refill Depletion Watchers, and Closed-Loop FEFO Reorder Queue  
**Date:** October 2, 2026  
**Primary Source Directives:**
- `SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md` (User Prompts 3 & 5, Sections 3 & 4)
- `PRODUCTION_ROADMAP.md` (Phase 3: Dual Notification Cadence & Procurement)
- `DISCUSSION_NOTES.md` (Section 2: Subscription Notification Intervals & Section 4: Automated Pharmacist Reorder Alerts)

**Associated Core Files:**
- `UserRepository.java`
- `NotificationService.java`
- `AutoRefillScheduler.java`
- `SubscriptionRepository.java`
- `MedicineRepository.java`
- `ProcurementAlertDTO.java`
- `DeficitSubscriptionDTO.java`
- `RestockRequestDTO.java`
- `RestockResponseDTO.java`
- `AdminDashboardDTO.java`
- `AdminService.java`
- `AdminController.java`
- `SecurityConfig.java`
- `User.java`
- `ProcurementAndNotificationTest.java`
- `medicine.model.ts`
- `admin.service.ts`
- `admin-dashboard.component.html`
- `admin-inventory.component.ts`
- `admin-inventory.component.html`

---

## ⚡ The 2-Minute Executive Gist

If someone wants to understand Phase 3 at a glance:

> **In a sentence:** We expanded Auto-Meds from a silent one-way notification system into an **intelligent two-way clinical alert cadence** and **closed-loop pharmacy procurement desk**—fanning out proactive multi-stage alerts (at 30, 17, 7, 5, 2 days for prescription renewals and 14, 7, 5, 3, 1 days for upcoming refills) to both patients and clinical pharmacists, while equipping pharmacy staff with an automated Reorder Queue, live subscriber deficit tracking, and 1-click batch restock capabilities enforcing First-Expired, First-Out (FEFO) dispensing.

### 📊 Before vs. After Snapshot

| Operational Dimension | Before Phase 3 (Prototype) | After Phase 3 (Senior Engineering Standard) |
|---|---|---|
| **Prescription Expiry Alert Intervals** | Rigid check for only 30, 15, and 7 days. Missed critical 17-day, 5-day, and 2-day emergency renewal windows. | **30, 17, 7, 5, and 2 Days**: Configurable cadence (`ALERT_DAYS_RX`) running across all active subscribers. |
| **Notification Recipients** | Alerts were sent **exclusively to the patient**. Pharmacists, dispensary managers, and admins received zero visibility. | **Dual Fan-Out (`notifyStaffAndPatient`)**: Broadcasts contextual clinical alerts to both patient and all `ROLE_PHARMACIST` / `ROLE_ADMIN` users. |
| **Refill Depletion Monitoring** | Zero advance warning for upcoming chronic medicine depletion. Patients only received an alert when the order was already placed or failed. | **14, 7, 5, 3, and 1 Days Refill Watcher**: Alerts patient of dispatch countdown and warns warehouse if available stock is insufficient. |
| **Stockout Deficit Reaction** | Code logged `"Our warehouse has been notified"`, but in reality, **no message or record was ever sent to staff**. | **Immediate Urgent Deficit Broadcast**: When 5-day soft-lock fails, instantly alerts staff with patient details, deficit count, and due date. |
| **Procurement & Reorder Queue** | No automated inventory reorder tracking or subscriber deficit view. | **Procurement Desk UI & API**: Real-time view of medicines $\le$ `reorder_threshold` prioritized by urgency (`CRITICAL_STOCKOUT`, `DEFICIT_QUEUED`, `LOW_STOCK`). |
| **Batch Restock Fulfillment** | Simple arbitrary `+10` button updating raw stock; no link to waiting subscriptions, no batch numbers, no expiry dates. | **Closed-Loop FEFO Batch Restock**: 1-click restock with Manufacturer Batch No and Expiry Date that **automatically fulfills and locks stock for awaiting deficit subscribers**! |
| **Test Verification** | 174 backend tests passing. | **181 backend tests passing (0 failures, 0 errors)** + verified production Angular build. |

---

## 🎯 Part 1: What Was Required (Scope & Analysis from Senior Blueprint)

In clinical pharmacy operations, running out of chronic medications (like Metformin or Atorvastatin) can lead to hospitalization. A platform that waits until the day of refill to check stock or that never alerts pharmacy staff of impending renewals fails basic healthcare safety standards.

The scope for Phase 3 was derived directly from **`SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md` (User Prompts 3 & 5)** and **`DISCUSSION_NOTES.md` (Sections 2 & 4)**:

### Identified Deficiencies Solved:
1. **The Single-Sided Alert Flaw (`Transcript Prompt 3 & Discussion Notes §2`)**:
   - In `AutoRefillScheduler.java`, `notificationService.createNotification` only notified `sub.getPatient().getId()`. Pharmacists were never informed when a patient’s doctor prescription was expiring or when refills were due.
   - *Requirement:* Implement `notifyStaffAndPatient` to broadcast notifications to the patient while fanning out clinical digests to all active staff (`ROLE_PHARMACIST` and `ROLE_ADMIN`).
2. **Missing Notification Cadence Windows (`Transcript Prompt 3`)**:
   - The original code checked only 30, 15, and 7 days for prescription expiry, and completely missed the critical 17-day mark (when doctors schedule review appointments) and 5-day/2-day emergency marks.
   - It also lacked an upcoming refill supply countdown for patients.
   - *Requirement:* Implement dual watchers:
     - Prescription Expiry Watcher: **30, 17, 7, 5, 2 days**.
     - Medication Supply Depletion Watcher: **14, 7, 5, 3, 1 days**.
3. **The "Silent Warehouse" Stockout Flaw (`Transcript Prompt 5 & Discussion Notes §4`)**:
   - When a medicine was out of stock, the code printed a message to the patient saying *"Our warehouse has been notified"*, but never actually alerted any warehouse staff.
   - *Requirement:* When a subscription enters `OUT_OF_STOCK_DEFICIT`, immediately trigger a `DANGER` alert to all pharmacists with patient name, required dosage, and exact unit deficit.
4. **Lack of a Procurement & Reorder System (`Transcript Prompt 5`)**:
   - Pharmacists had no consolidated dashboard view to know which medicines were running low or which chronic patients were stuck waiting for stock replenishment.
   - *Requirement:* Build a dedicated Pharmacist Reorder & Restock Queue on both backend (`/api/admin/procurement/**`) and frontend (`Warehouse & Procurement Desk`).
5. **Disconnected Restocking (`Discussion Notes §4`)**:
   - When a pharmacist restocked inventory, pending deficit subscriptions remained stuck in deficit until the midnight cron ran again.
   - *Requirement:* Closed-loop restock that immediately attempts to soft-lock stock for waiting deficit subscriptions and automatically marks them `RESERVED`.

---

## 🛠️ Part 2: What Was Done in Detail (Implementation Breakdown)

### 1. Two-Way Notification Fan-Out Engine
- **Files:** `NotificationService.java`, `UserRepository.java`
- **Transcript Directives:** User Prompt 3 & Discussion Notes §2
- **Changes Made:**
  - Added derived query in `UserRepository.java`:
    ```java
    List<User> findByRoleIn(List<String> roles);
    ```
  - Added fan-out broadcasting methods in `NotificationService.java`:
    ```java
    @Transactional
    public void notifyStaffAndPatient(Long patientId, String title, String patientMsg, String staffMsg, String type) {
        if (patientId != null) {
            createNotification(patientId, title, patientMsg, type);
        }
        notifyStaff(title, staffMsg, type);
    }

    @Transactional
    public void notifyStaff(String title, String staffMsg, String type) {
        List<User> staff = userRepository.findByRoleIn(List.of("ROLE_PHARMACIST", "ROLE_ADMIN"));
        for (User s : staff) {
            try {
                createNotification(s.getId(), "[Clinical/Staff Alert] " + title, staffMsg, type);
            } catch (Exception ex) {
                // Safeguard: Ensure one failure doesn't break the loop
            }
        }
    }
    ```

---

### 2. Dual Cadence Schedulers (Prescription Expiry & Refill Depletion)
- **File:** `AutoRefillScheduler.java`
- **Transcript Directives:** User Prompt 3
- **Changes Made:**
  - Configured cadence constants:
    ```java
    private static final Set<Long> ALERT_DAYS_RX = Set.of(30L, 17L, 7L, 5L, 2L);
    private static final Set<Long> ALERT_DAYS_REFILL = Set.of(14L, 7L, 5L, 3L, 1L);
    ```
  - **Prescription Expiry Watcher (`sendPrescriptionExpiryAlerts`)**:
    - Scans all `ACTIVE` subscriptions.
    - Computes `daysLeft = between(today, rx.getExpiryDate().toLocalDate())`.
    - At 30, 17, 7, 5, 2 days: Notifies the patient to prepare/upload a renewal, and alerts staff to perform clinical outreach. If $\le 5$ days, tags severity as `DANGER`.
  - **Medication Supply Depletion Watcher (`sendRefillApproachingAlerts`)**:
    - Computes `daysLeft = between(today, sub.getNextRefillDate().toLocalDate())`.
    - At 14, 7, 5, 3, 1 days: Reminds the patient of upcoming dispatch. Checks available warehouse stock:
      - If `available < quantity` and not reserved: Triggers urgent `DANGER` alert to staff: *"⚠️ Refill Stockout Risk: Subscription #X due in Y days. Restock immediately!"*
      - If stock is verified: Sends standard `INFO` digest.
  - **Urgent Deficit Alerting in Soft-Lock**:
    - If `reserveStock` fails during 5-day soft-lock, instantly computes unit deficit and dispatches `🚨 URGENT: Reorder Required for {medicine}` alert to all staff with patient name, prescription, and refill buffer days.

---

### 3. Closed-Loop Procurement & Restock Backend Engine
- **Files Created/Updated:** `ProcurementAlertDTO.java`, `DeficitSubscriptionDTO.java`, `RestockRequestDTO.java`, `RestockResponseDTO.java`, `AdminDashboardDTO.java`, `AdminService.java`, `AdminController.java`, `SecurityConfig.java`
- **Transcript Directives:** User Prompt 5 & Discussion Notes §4
- **Changes Made:**
  - **Procurement Alerts Endpoint (`GET /api/admin/procurement/alerts`)**:
    - Retrieves medicines where `(stock - reserved) <= reorderThreshold`.
    - Calculates deficit subscriptions count for each medicine.
    - Classifies urgency: `CRITICAL_STOCKOUT` (0 available), `DEFICIT_QUEUED` (has waiting subscribers), `LOW_STOCK` (below threshold).
  - **Deficit Subscriptions Endpoint (`GET /api/admin/procurement/deficits`)**:
    - Lists all active subscriptions in `OUT_OF_STOCK_DEFICIT` state sorted by earliest due date.
  - **Closed-Loop Batch Restock (`POST /api/admin/procurement/restock`)**:
    - Locks medicine with `findByIdForUpdate` (pessimistic write lock).
    - Increments physical stock count and updates manufacturer batch/expiry date (FEFO compliance).
    - **Automatic Deficit Fulfillment**: Instantly queries awaiting deficit subscriptions for this medicine in order of due date. Atomically soft-locks stock (`reserveStock`) for each, flips their status to `RESERVED`, and notifies the patient and pharmacy staff that their medication is now secured.
  - **Security Configuration**:
    - Mapped `/api/admin/procurement/**` to `ROLE_ADMIN` and `ROLE_PHARMACIST` in `SecurityConfig.java`.

---

### 4. Frontend Pharmacist & Admin Procurement Desk
- **Files Updated:** `medicine.model.ts`, `admin.service.ts`, `admin-dashboard.component.html`, `admin-inventory.component.ts`, `admin-inventory.component.html`
- **Changes Made:**
  - **Domain Model Extension**: Added `reservedQuantity`, `availableQuantity`, `reorderThreshold`, and `suggestedReorderPackSize` to `Medicine` interface.
  - **Dashboard Cards**: Added "Deficit Subscriptions" (with emergency warning indicator) and "Procurement Alerts" metric tiles to `admin-dashboard.component.html`.
  - **Warehouse & Procurement Desk UI (`admin-inventory`)**:
    - **Dual Tab Pill Navigation**: Switch smoothly between "📦 Warehouse Stock" and "🚨 Reorder & Deficit Queue" (with dynamic red badge indicator showing total pending alerts).
    - **Enhanced Stock Table**: Explicit columns displaying Total Physical Stock, 🔒 Soft-Locked (Subscriptions), and 🟢 Available to Sell (Walk-ins).
    - **Reorder & Deficit Queue View**:
      - Section 1: Active Subscriber Stockout Deficits (showing patient, quantity needed, available stock, and refill countdown).
      - Section 2: Procurement Reorder Alerts (showing urgency tags, threshold limits, suggested pack sizes, and waiting deficit counts).
    - **Interactive 1-Click Restock Modal**: Allows entering Batch Number, Expiry Date (FEFO), and Restock Quantity (pre-filled with `suggestedReorderPackSize`).

---

## 🧪 Part 3: Quality Gates & Automated Verification Evidence

### 1. Dedicated Phase 3 Test Suite Run
- **Test File:** `ProcurementAndNotificationTest.java`
- Verified 7 scenario tests covering all Phase 3 requirements:
  1. `testPrescriptionExpiryCadenceWatcher`: Verifies 17-day notice triggers warning to patient and staff.
  2. `testPrescriptionExpiryCadenceDangerNotice`: Verifies 5-day notice triggers DANGER alert.
  3. `testRefillApproachingCadenceWatcher`: Verifies 7-day advance notice triggers refill dispatch alert.
  4. `testSoftLockDeficitTriggersStaffReorderAlert`: Verifies soft-lock failure dispatches urgent staff warning.
  5. `testGetProcurementAlertsCategorization`: Verifies correct categorization (`CRITICAL_STOCKOUT`, `DEFICIT_QUEUED`, `LOW_STOCK`).
  6. `testGetDeficitSubscriptions`: Verifies deficit query returns waiting patients with due date countdown.
  7. `testRestockWithAutomaticDeficitFulfillment`: Verifies restock replenishes stock and automatically fulfills waiting deficit subscribers to `RESERVED` status.
```text
[INFO] Running com.automeds.service.ProcurementAndNotificationTest
15:56:15.038 [main] WARN com.automeds.scheduler.AutoRefillScheduler -- Could not reserve 30 units of Atorvastatin 20mg for subscription #100. Deficit recorded.
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.579 s - in com.automeds.service.ProcurementAndNotificationTest
[INFO] BUILD SUCCESS
```

### 2. Complete Backend Test Suite Run
```bash
mvn test
```
```text
[INFO] Results:
[INFO] Tests run: 181, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  29.540 s
```

### 3. Frontend Production Compilation Run
```bash
npm run build
```
```text
> auto-meds-frontend@1.0.0 build
> ng build

- Generating browser application bundles (phase: setup)...
√ Browser application bundle generation complete.
- Copying assets...
√ Copying assets complete.
- Generating index html...
√ Index html generation complete.

Initial Chunk Files           | Names         |  Raw Size | Estimated Transfer Size
main.bc4b66820a1cc9d4.js      | main          | 474.23 kB |               104.90 kB
styles.6c0f5c2168bf64f0.css   | styles        | 303.76 kB |                32.53 kB
scripts.425520de70bbab43.js   | scripts       |  77.73 kB |                20.88 kB
polyfills.a6d567acb992ca2c.js | polyfills     |  33.04 kB |                10.64 kB
runtime.746eeadf693b463e.js   | runtime       | 914 bytes |               523 bytes

| Initial Total | 889.65 kB |               169.47 kB
Build at: 2026-10-02T10:29:45.003Z - Time: 11599ms
```

---

## ⏭️ Part 4: Preview of Next Phase

### Phase 4: Persona UX Upgrades & Real-World Resilience Flows
Directly addressing **Sections 5, 6, and User Prompts 6 & 7 of `SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md`**:
1. **Normal / Acute Buyer**:
   - "Shop by Symptom / Ailment" taxonomy search (Fever, Migraine, Acid Reflux, Allergy, First Aid).
   - Generic vs. Branded Cost-Savings comparison slider (*"Save 60% with clinically equivalent generic"*).
   - 1-Click Fallback to Cash on Delivery (COD) on payment gateway timeouts.
2. **Chronic Maintenance Patient**:
   - "Sync My Refills" button (Multi-medication sync to arrive on a single consolidated "Pillbox Day").
   - "Snooze / Vacation Pause" controls (defer refill by 7 or 14 days or change destination address for one cycle).
   - Emergency 5-Day Bridge Supply request flow when awaiting doctor prescription renewals.
3. **Pharmacist Clinical Desk**:
   - Split-screen prescription triage desk (zoomable prescription PDF on left; catalog medication matcher on right).
   - Automated Drug-Drug Interaction (DDI) & patient allergy warning engine.
