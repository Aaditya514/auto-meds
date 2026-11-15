# 👥 User Needs & User Stories: Auto-Meds

> **Document Type:** Product Requirements Document (PRD) — User Research & Persona Problem Discovery  
> **Target Audience:** Engineering Team, Product Designers, Clinical Advisors  
> **Timeline / Perspective:** Pre-Development Planning & Inception Phase  
> **Author:** Aaditya Aanand  

---

## 1. User Personas & Discovery

Before writing any software or designing data models, we conducted persona discovery across the primary healthcare stakeholders involved in chronic medication management.

---

### Persona 1: The Busy Chronic Patient

```
┌────────────────────────────────────────────────────────────────────────┐
│ PERSONA: Rajesh Sharma                                                 │
├────────────────────────────────────────────────────────────────────────┤
│ • Age: 44 years old                                                    │
│ • Occupation: Senior Operations Manager                                │
│ • Location: Tier-1 Metro City                                          │
│ • Health Condition: Stage-2 Hypertension & Chronic Type-2 Diabetes     │
│ • Tech Savvy: High (Daily smartphone & laptop user)                    │
└────────────────────────────────────────────────────────────────────────┘
```

#### What problems does he face?
* **Forgetfulness under work stress:** Rajesh works demanding 10-hour corporate days and frequently realizes he has run out of daily blood pressure and blood sugar maintenance pills (like Telmisartan and Metformin) only on the morning he reaches into an empty blister pack.
* **Manual re-ordering fatigue:** Every month, he has to log into pharmacy apps, search for his medicines from scratch, re-add them to cart, and re-enter payment details.
* **Sudden stockouts:** On multiple occasions, his specific brand was out of stock on delivery apps, forcing him to miss critical doses while searching across physical pharmacies.

#### What does he want that could help him?
* An automated, set-and-forget subscription where his 30-day supply is automatically scheduled and delivered to his home 3 to 5 days before he runs out.
* If his prescribed brand is unavailable, he wants the platform to automatically recommend an identical, bio-equivalent generic drug with the exact same active chemical salt and strength so his treatment is never interrupted.
* Clear upfront warnings if any new medication he adds might dangerously interact with his current daily pills.

---

### Persona 2: The Elderly Independent Patient

```
┌────────────────────────────────────────────────────────────────────────┐
│ PERSONA: Shanti Devi                                                   │
├────────────────────────────────────────────────────────────────────────┤
│ • Age: 71 years old                                                    │
│ • Occupation: Retired School Teacher                                   │
│ • Location: Suburban Town                                              │
│ • Health Condition: Mild Age-Related Joint Stiffness & Vitamin Deficiencies│
│ • Tech Savvy: Low (Uses a basic smartphone primarily for phone calls)  │
└────────────────────────────────────────────────────────────────────────┘
```

#### What problems does she face?
* **Physical & visual discomfort:** Shanti takes daily calcium, vitamin D supplements, and mild joint pain relievers to stay mobile. However, she has mild hand tremors and reduced vision. Standard e-commerce apps have tiny buttons, low-contrast grey text, confusing pop-ups, and complicated checkout steps that she cannot easily navigate.
* **App fatigue and password lockouts:** She forgets passwords, struggles with one-time passwords (OTPs) switching between screens, and dislikes installing complex apps.
* **Fear of expired medicine:** She cannot read small expiry dates printed on foil blister packs and worries about taking expired supplements and tablets.

#### What does she want that could help her?
* An interface with very large buttons, high contrast, and large readable fonts that are easy to press without accidental clicks.
* The ability to confirm her scheduled monthly medicine delivery through a simple, friction-free SMS message (e.g., replying "YES" to a text) without having to open an app or log into a website.
* A clear guarantee that the pharmacy checks all medicine expiration dates and only dispenses fresh batches with ample shelf life.

---

### Persona 3: The Long-Distance Family Caregiver

```
┌────────────────────────────────────────────────────────────────────────┐
│ PERSONA: Vikram Verma                                                  │
├────────────────────────────────────────────────────────────────────────┤
│ • Age: 36 years old                                                    │
│ • Occupation: Software Consultant                                      │
│ • Location: Lives in Bengaluru; elderly parents live in Lucknow        │
│ • Role: Remote Healthcare Caregiver for aging parents                  │
│ • Tech Savvy: Very High                                                │
└────────────────────────────────────────────────────────────────────────┘
```

#### What problems does he face?
* **Lack of remote visibility:** Vikram worries constantly that his diabetic father forgets to refill his insulin and cardiovascular tablets on time.
* **Account-sharing friction:** Currently, his parents have to read OTPs over the phone or Vikram has to log in with their credentials, which creates confusion and security risks.
* **No oversight on delivery:** He has no way to verify whether the delivered package was actually verified by a certified pharmacist and received safely at his parents' doorstep.

#### What does he want that could help him?
* A dedicated caregiver link where he can officially connect to his parents' patient profiles with their permission.
* The ability to view his parents' active medication schedules, receive notifications when a refill is due, approve the order on their behalf, and track the delivery straight to their doorstep.
* Instant alerts if a prescription is nearing its expiration date so he can schedule a timely doctor appointment for them.

---

### Persona 4: The Overburdened Retail Pharmacist

```
┌────────────────────────────────────────────────────────────────────────┐
│ PERSONA: Dr. Meera Nambiar                                             │
├────────────────────────────────────────────────────────────────────────┤
│ • Age: 32 years old                                                    │
│ • Occupation: Chief Dispensing Pharmacist & Inventory Supervisor       │
│ • Location: Central Fulfillment Pharmacy                               │
│ • Responsibility: Clinical verification, legal compliance, inventory  │
│ • Tech Savvy: Moderate (Uses pharmacy billing software & desktop tools)│
└────────────────────────────────────────────────────────────────────────┘
```

#### What problems does she face?
* **Illegible prescriptions and manual data entry:** Meera spends hours manually deciphering handwritten doctor prescriptions, squinting at uploaded photos, and typing medicine names into billing systems.
* **Sudden inventory exhaustion:** Retail customers buying bulk quantities at the counter suddenly wipe out the medicine stock needed for long-term chronic patients due for dispatch tomorrow.
* **Near-expiry medicine write-offs:** Boxes of medicines get buried at the back of the shelf and expire before being sold, causing heavy financial loss and inventory waste.
* **Risk of adverse drug interactions:** Catching drug-drug interactions manually across patients taking 5–8 different daily medications is error-prone under high-volume pressure.

#### What does she want that could help him?
* An automated tool that scans uploaded prescriptions and highlights extracted medicine names and dosages side-by-side with the original image so she can verify and approve them quickly.
* A system that reserves medicine stock for scheduled chronic patients several days before dispatch so counter sales don't cause sudden stockouts.
* An inventory dashboard that flags any medicine expiring within 90 days and enforces a strict First-Expired, First-Out (FEFO) dispensing rule.
* A clear communication channel to request clarification from the prescribing doctor whenever a dosage or frequency is ambiguous.

---

## 2. Agile User Stories & Acceptance Criteria

User stories are organized into iterative Epics aligned with our Agile sprint cycles.

---

### Epic 0: Base E-Commerce Model (Release 0: Walking Skeleton)

#### US-01: User Registration & Secure Authentication
* **As a** patient visiting Auto-Meds for the first time,
* **I want** to securely register and log into my personal profile,
* **So that** my personal medical details, delivery address, and order history are kept private.
* **Agile Acceptance Criteria:**
  - Password must be hashed securely; supports separate `PATIENT` and `ADMIN` roles.
  - Successful authentication issues a session token.

#### US-02: Medicine Search & Catalog Browsing
* **As a** customer looking for my medications,
* **I want** to search for drugs by trade name, chemical composition, or health ailment,
* **So that** I can easily find the exact medication I need.
* **Agile Acceptance Criteria:**
  - Search returns medicine name, strength, category, and price in $<500\text{ms}$.
  - Hides internal commercial warehouse quantities, displaying a clean `In Stock` / `Out of Stock` indicator.

#### US-03: Cart Management & One-Time Checkout
* **As a** customer ready to purchase,
* **I want** to add items to my shopping cart, review total cost, and submit an order with my delivery address,
* **So that** my medications can be processed and delivered to my doorstep.
* **Agile Acceptance Criteria:**
  - Cart calculates itemized total and taxes correctly.
  - Supports Cash on Delivery and online payment submission.

---

### Epic 1: Chronic Refill Automation & Stock Protection (Iteration 1)

#### US-11: Automated Recurring Refill Scheduling
* **As a** busy chronic care patient (Rajesh),
* **I want** to set up a recurring medication schedule that automatically triggers my refill before my current supply runs out,
* **So that** I never experience a gap in my daily therapy due to forgetting to reorder.
* **Agile Acceptance Criteria:**
  - System automatically calculates the cycle interval ($T - 5\text{ days}$) and initiates the 6-phase refill pipeline.
  - Patient can pause or modify their schedule at any time.

#### US-12: Two-Tier Inventory Reservation (Soft-Locking)
* **As an** inventory manager (Dr. Meera),
* **I want** the system to reserve necessary stock 5 days ahead of scheduled refill dispatches,
* **So that** walk-in retail buyers cannot accidentally buy up stock promised to chronic patients.
* **Agile Acceptance Criteria:**
  - Total physical stock (`stock_quantity`) remains intact for physical audits, while `reserved_quantity` increments.
  - Retail cart addition checks only `stock_quantity - reserved_quantity`.

---

### Epic 2: Clinical Safety & Prescription Intelligence (Iteration 2)

#### US-21: Digital Prescription Intake & Background Scanning
* **As a** patient (Rajesh),
* **I want** to upload a scan or photo of my doctor's prescription,
* **So that** the pharmacy team can legally verify and dispense my prescription-only medications.
* **Agile Acceptance Criteria:**
  - Accepts image files (JPG, PNG, PDF up to 10MB).
  - Background scanning extracts text without causing HTTP timeouts.

#### US-22: Pharmacist Verification Desk & Doctor Clarification
* **As a** licensed dispensing pharmacist (Dr. Meera),
* **I want** an intake dashboard that displays uploaded prescriptions alongside extracted drug names,
* **So that** I can approve, reject, or place an order on hold to contact the prescribing physician.
* **Agile Acceptance Criteria:**
  - Split-screen view showing original image and extracted medications.
  - Clinical action (`Approve`, `Reject`, `Clarification`) records an immutable timestamped audit log.

#### US-23: Drug-Drug Interaction (DDI) Safety Alerts
* **As a** patient taking daily maintenance pills (Rajesh),
* **I want** the platform to alert me immediately if a new medicine I add clashes dangerously with my existing daily pills,
* **So that** I am protected from adverse drug reactions before ordering.
* **Agile Acceptance Criteria:**
  - Cross-references active patient medication list against known clinical contraindications.
  - Blocks one-click dispensing until a clinical pharmacist signs off.

---

### Epic 3: Senior Ergonomics & Multi-Channel Inclusion (Iteration 3)

#### US-31: Universal Senior-Accessible Interface (WCAG AA)
* **As an** elderly patient with declining eyesight and hand tremors (Shanti Devi),
* **I want** extra-large buttons ($\ge 44\text{px}$), high-contrast colors, and large readable text on screen,
* **So that** I can easily check my medicine names and order status without making accidental taps.
* **Agile Acceptance Criteria:**
  - All clickable touch targets are at least $44 \times 44\text{px}$.
  - Color contrast meets WCAG 2.1 AA standards ($\ge 4.5:1$).

#### US-32: Frictionless 2-Way SMS Refill Confirmation ("Reply YES")
* **As an** elderly patient who dislikes complex mobile apps (Shanti Devi),
* **I want** to receive a simple text message asking if I need my regular refill and simply reply "YES",
* **So that** my medicines arrive on time without me having to navigate websites or remember passwords.
* **Agile Acceptance Criteria:**
  - SMS prompt sent 5 days before medication exhaustion.
  - Inbound reply "YES" automatically advances the refill to pharmacist dispensing.

---

### Epic 4: Caregiver Delegation & Family Oversight (Iteration 4)

#### US-41: Remote Caregiver Profile Linking
* **As a** family caregiver living in another city (Vikram),
* **I want** to link my account to my aging parents' health profiles with their consent,
* **So that** I can oversee their ongoing prescriptions without having to share passwords.
* **Agile Acceptance Criteria:**
  - Requires a secure 2-way authorization code before linking accounts.
  - Caregiver can manage multiple dependents from a single dashboard.

#### US-42: Remote Refill Approval & Order Tracking
* **As a** caregiver responsible for my parents' healthcare (Vikram),
* **I want** to receive alerts when my parents' medication is due and confirm the refill myself,
* **So that** I have complete peace of mind that their treatment is uninterrupted.
* **Agile Acceptance Criteria:**
  - Caregiver receives notification when parent's refill window opens.
  - Caregiver approval fulfills the confirmation step and moves order to packaging.

---

### Epic 5: Closed-Loop Warehouse Operations & Traceability (Iteration 5)

#### US-51: Near-Expiry Surveillance & First-Expired Dispensing (FEFO)
* **As a** pharmacy inventory manager (Dr. Meera),
* **I want** an alert for all medicine batches expiring within 90 days and an automated priority rule to dispense the earliest-expiring stock first,
* **So that** we prevent clinical hazards and avoid medicine waste.
* **Agile Acceptance Criteria:**
  - Batches $<90$ days to expiry are highlighted in red/amber.
  - Warehouse pickers are automatically routed to the earliest-expiring batch first.

#### US-52: Manufacturer Batch Recall Traceability
* **As a** pharmacy compliance lead (Dr. Meera),
* **I want** every dispensed order to record the exact manufacturer batch number and expiry date,
* **So that** we can immediately identify and notify affected patients in the event of a drug recall.
* **Agile Acceptance Criteria:**
  - Tamper-evident packaging slip displays printed batch number and verification QR code.
  - Audit trail links patient order ID to specific batch number.

#### US-53: Closed-Loop Deficit Restocking
* **As a** procurement manager (Dr. Meera),
* **I want** subscriptions stalled by temporary stockouts to automatically unblock and fulfill when new inventory arrives,
* **So that** waiting patients receive their orders with zero manual re-entry.
* **Agile Acceptance Criteria:**
  - Incoming stock auto-assigns to oldest waiting deficits first before being added to general sale.
