# 🔄 User Flows & Interaction Workflows: Auto-Meds

> **Document Type:** Interaction Design & System Workflows Specification  
> **Target Audience:** Engineering Leads, Product Designers, Technical Program Managers (TPMs)  
> **Timeline / Perspective:** Pre-Development Workflow Design & Architecture  
> **Notation:** Mermaid.js diagrams (rendering natively on GitHub)  
> **Author:** Aaditya Aanand  

---

## 1. Planned End-to-End Refill Lifecycle (6-Phase Stepper)

Rather than treating prescription refills as a single instantaneous checkout button, the platform is designed around a **predictive 6-phase state machine** that initiates 5 days before a patient exhausts their medication:

```mermaid
stateDiagram-v2
    [*] --> Scheduled
    Scheduled --> Phase1_InventoryCheck
    
    state Phase1_InventoryCheck {
        [*] --> CheckStockAvailability
        CheckStockAvailability --> ReserveStock
        CheckStockAvailability --> DeficitQueue
        DeficitQueue --> ReserveStock
    }

    ReserveStock --> Phase2_PatientConfirmation
    
    state Phase2_PatientConfirmation {
        [*] --> SendAlerts
        SendAlerts --> WebConfirmation
        SendAlerts --> CaregiverConfirmation
        SendAlerts --> SmsConfirmation
        
        WebConfirmation --> Confirmed
        CaregiverConfirmation --> Confirmed
        SmsConfirmation --> Confirmed
        
        SendAlerts --> AutoConfirmTimeout
        AutoConfirmTimeout --> Confirmed
    }

    Confirmed --> Phase3_PharmacistVerification
    
    state Phase3_PharmacistVerification {
        [*] --> ReviewPrescription
        ReviewPrescription --> DoctorClarification
        DoctorClarification --> ReviewPrescription
        ReviewPrescription --> Approved
        ReviewPrescription --> Cancelled
    }

    Approved --> Phase4_FefoFulfillment
    
    state Phase4_FefoFulfillment {
        [*] --> ScanEarliestExpiryBatch
        ScanEarliestExpiryBatch --> PickFromShelf
        PickFromShelf --> ReleaseReservationAndDeductPhysical
    }

    ReleaseReservationAndDeductPhysical --> Phase5_PaymentSettlement
    Phase5_PaymentSettlement --> Phase6_DoorstepDelivery
    Phase6_DoorstepDelivery --> Scheduled
```

---

## 2. Multi-Channel Patient & Caregiver Confirmation Workflow

This interaction flow specifies how low-tech elderly patients and remote family caregivers can approve scheduled deliveries with zero friction.

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as Refill Scheduler (T-5 Days)
    participant Inventory as Inventory System
    participant SMS as SMS Gateway
    participant Patient as Elderly Patient (Phone)
    participant Caregiver as Caregiver (Web Portal)
    participant Pharmacist as Pharmacist Queue

    Scheduler->>Inventory: Query Subscriptions Due in 5 Days
    Inventory-->>Scheduler: Return Due Regimens (e.g., Blood Pressure Tablets)
    Scheduler->>Inventory: Reserve Required Units (Soft-Lock Stock)
    
    par Patient Text Outreach
        Scheduler->>SMS: Send Refill Alert Text Message
        SMS->>Patient: "Auto-Meds: Your BP medicine is due. Reply YES to confirm delivery."
    and Caregiver Alert
        Scheduler->>Caregiver: Push Notification to Caregiver Portal
    end

    alt Option A: Patient Replies via SMS
        Patient->>SMS: "YES"
        SMS->>Scheduler: Webhook Notification (User Confirmed)
        Scheduler->>Scheduler: Mark Refill Status as CONFIRMED
    else Option B: Caregiver Confirms for Dependent
        Caregiver->>Scheduler: Clicks "Approve Refill for Parent"
        Scheduler->>Scheduler: Mark Refill Status as CONFIRMED
    else Option C: Patient Logs into Web Portal
        Patient->>Scheduler: Clicks "Confirm Scheduled Delivery"
        Scheduler->>Scheduler: Mark Refill Status as CONFIRMED
    end

    Scheduler->>Pharmacist: Move Order to Pharmacist Verification Queue
```

---

## 3. Pharmacist Prescription Verification & Doctor Clarification Flow

This workflow defines the clinical checks required before any medication can be physically packed and dispensed.

```mermaid
flowchart TD
    A["Prescription Uploaded by Patient"] --> B["Background Text Scanning Tool"]
    B --> C["Extract Drug Names, Strengths & Doctor Information"]
    C --> D["Display in Pharmacist Verification Console side-by-side with Original Scan"]
    
    D --> E{"Pharmacist Clinical Review"}
    
    E -->|1. Approve| F["Automatic Drug Interaction Check"]
    F --> G{"Adverse Interaction Found?"}
    G -->|Yes| H["Pharmacist Reviews & Signs Off on Clinical Override"]
    H --> I["Mark Prescription APPROVED"]
    G -->|No| I
    
    E -->|2. Clarification Needed| J["Click 'Request Doctor Clarification'"]
    J --> K["Enter Specific Clinical Question e.g., Dosage Frequency"]
    K --> L["Place Subscription on HOLD"]
    L --> M["Notify Patient / Caregiver to Contact Doctor"]
    
    E -->|3. Reject| N["Select Rejection Reason: Expired, Illegible, Forged"]
    N --> O["Mark Prescription REJECTED"]
    O --> P["Release Reserved Inventory Back to General Pool"]
    P --> Q["Notify Patient via SMS with Resubmission Instructions"]
```

---

## 4. FEFO Inventory Control & Stockout Deficit Recovery

This workflow designs how warehouse inventory is protected against expiration and how temporary stockouts recover automatically.

```mermaid
flowchart LR
    subgraph Inbound["Inbound Stock & Expiry Triage"]
        A1["Receive New Medicine Batch"] --> A2["Log Batch Number & Expiry Date"]
        A2 --> A3{"Days to Expiration?"}
        A3 -->|Less than 90 Days| A4["Place in Near-Expiry Alert Queue"]
        A3 -->|90 Days or More| A5["Standard Available Inventory"]
    end

    subgraph Dispensing["Dispensing Priority (FEFO)"]
        A4 --> B1{"Earliest Expiry Batch Selector"}
        A5 --> B1
        B1 --> B2["Pick Earliest-Expiring Batch for Current Refill"]
        B2 --> B3["Print Dispensing Slip with Verification Stamp"]
    end

    subgraph Deficit["Stockout Deficit Resolution"]
        C1["Stock Depleted on Refill Date"] --> C2["Subscription Placed in Deficit Queue"]
        C2 --> C3["New Stock Scanned into Warehouse"]
        C3 --> C4["Automated Priority Fulfillment Algorithm"]
        C4 --> C5["Fulfill Waiting Deficits First by Queue Order"]
        C5 --> C6["Remaining Surplus Added to General Shelf Stock"]
    end
```

---

## 5. Planned Failure & Exception Scenarios

To ensure safety and predictable behavior, the following edge cases were defined prior to implementation:

| Failure / Edge Case | Detection Mechanism | Planned System Response |
| :--- | :--- | :--- |
| **Insufficient Stock at T-5 Days** | Available stock check returns less than required dosage. | Move order to `DEFICIT_BACKLOG`; alert procurement team; notify patient that their refill is queued with priority fulfillment upon shipment arrival. |
| **Prescription Expired Prior to Refill** | Doctor prescription validity date passes before next refill date. | Halt automated dispatch; alert patient and caregiver 14 days in advance to upload an updated prescription from their physician. |
| **Patient Unresponsive Across All Channels (48 hrs)** | Confirmation timer expires with no SMS reply or portal click. | If tagged as an essential chronic maintenance drug, automatically advance to pharmacist review so therapy is not dropped; otherwise hold dispatch to prevent pill stockpiling. |
| **Dangerous Drug-Drug Interaction Detected** | Clinical interaction engine flags conflicting drugs in patient's active list. | Block one-click approval; require pharmacist to review interaction warning and enter clinical notes before releasing. |
| **Image Scanning Fails (Blurry / Dark Photo)** | Scanned text confidence is too low or image is unreadable. | Automatically tag as `MANUAL_REVIEW_REQUIRED`; preserve original image for pharmacist visual inspection. |
