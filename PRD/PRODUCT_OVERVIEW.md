# 📋 Product Overview: Auto-Meds

> **Document Type:** Product Strategy & Definition Document  
> **Target Audience:** Engineering Team, Product Leadership, Healthcare Stakeholders  
> **Timeline / Perspective:** Product Inception & Pre-Build Specification  
> **Author:** Aaditya Aanand  

---

## 1. Product Vision

**Auto-Meds** is an intelligent, patient-centric chronic care e-pharmacy platform designed to make daily maintenance medication uninterrupted, safe, and accessible.

Rather than operating as a conventional reactive shopping cart where patients must remember to log in and re-order every 30 days, Auto-Meds is designed from the ground up around **automated clinical refill cycles**, **clinical drug safety**, **inventory protection**, and **inclusive accessibility for elderly and dependent patients**.

---

## 2. The Problem Space

Millions of patients suffering from chronic conditions such as hypertension, diabetes, asthma, and cardiovascular disease require continuous, daily pharmacotherapy. Yet in practice, the healthcare system experiences major breakdowns:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CORE PROBLEMS IDENTIFIED                        │
└────────────────────────────────────────────────────────────────────────┘
          │
          ├── 1. Therapy Non-Adherence (Forgetfulness & Friction)
          │      Patients get busy and forget to re-order medications before running out,
          │      leading to missed doses, therapy lapses, and emergency hospital visits.
          │
          ├── 2. Sudden Pharmacy Stockouts & Supply Pinches
          │      When a patient's regular branded drug is unavailable, pharmacies lack
          │      an automated mechanism to suggest safe, bio-equivalent generic substitutes.
          │
          ├── 3. The Digital Barrier for Senior Patients
          │      Elderly patients—who carry the highest chronic disease burden—struggle
          │      with modern digital apps, small fonts, tiny buttons, and confusing checkout flows.
          │
          └── 4. Caregiver Isolation
                 Adult children managing their elderly parents' medications remotely have no
                 authorized way to track refills, approve dispatches, or monitor deliveries.
```

---

## 3. Target User Groups & Their Challenges

The platform is designed to serve four distinct user cohorts:

1. **Working Adult Chronic Patients:** Need an effortless, automated recurring delivery mechanism so they never run out of daily maintenance pills.
2. **Senior & Low-Vision Patients:** Need high visual contrast, oversized touch targets, simple layouts, and non-app communication options (like simple SMS text confirmation).
3. **Family Caregivers:** Need the ability to link to their aging parents' accounts to remotely oversee prescriptions, approve refill alerts, and track shipments.
4. **Dispensing Pharmacists:** Need an efficient verification desk with automated prescription text extraction, drug interaction alerts, and inventory tools that prevent stockouts and medicine expiration.

---

## 4. Proposed Solution & Core Value Proposition

Auto-Meds shifts the healthcare paradigm from **reactive purchasing** to **proactive chronic care delivery**:

```
Current Reality: Reactive Cycle
[Patient runs out of pills] ➔ [Scrambles to order] ➔ [Item out of stock] ➔ [Misses medication for days]

Auto-Meds Target: Proactive Refill Engine
[Refill cycle scheduled] ➔ [Stock reserved in advance] ➔ [SMS / Web confirmation] ➔ [Checked & Delivered]
```

### Key Value Pillars

1. **Automated Scheduled Refills:** Patients set their daily dosage once; the platform automatically schedules upcoming deliveries ahead of time.
2. **Effortless SMS Confirmation:** Patients without smartphone fluency can confirm deliveries simply by replying "YES" to an SMS prompt.
3. **Guaranteed Stock via 5-Day Reservation:** To prevent walk-in buyers from exhausting stock allocated for chronic patients, the pharmacy system reserves necessary quantities 5 days before dispatch.
4. **Intelligent Generic Substitution:** If a prescribed brand is out of stock or high-cost, the platform suggests bio-equivalent generic medicines with identical active ingredients and strengths.
5. **Caregiver Oversight:** Family members can link accounts with consent, review active regimens, approve refill reminders, and monitor doorstep shipments.
6. **Universal Senior Accessibility:** Built with large touch controls ($\ge 44\text{px}$), dynamic text sizing, and high-contrast color palettes designed to meet WCAG AA standards.

---

## 5. Planned Scope & Boundaries

### Included in Product Scope (MVP)
* Patient registration, profile setup, and recurring chronic subscription configuration.
* Digital prescription upload and automated text scanning (OCR) for pharmacist verification.
* Pharmacist review console with approval, rejection, and doctor clarification workflows.
* Automated 5-day inventory reservation engine and First-Expired, First-Out (FEFO) dispensing rules.
* 90-day near-expiry inventory surveillance and stockout deficit resolution queue.
* Caregiver account linking and multi-patient management dashboard.
* Two-way SMS refill confirmation integration.
* Drug-Drug Interaction (DDI) safety checks during ordering.

### Out of Product Scope (Explicit Non-Goals for Initial Phase)
* **Telehealth Video Consultations:** Platform verifies valid existing prescriptions rather than providing in-app doctor visits.
* **Inpatient Hospital Bedside Dispensing:** Focus is solely on outpatient, home-delivered chronic maintenance medications.
* **Direct Real-Time Insurance Claim Billing (EDI):** Initial product will focus on direct patient payment and Cash on Delivery before integrating complex payer clearinghouses.
* **IoT Refrigerated Cargo Telemetry:** Real-time container temperature tracking is handled by the courier service, not this platform.

---

## 6. Success Goals & Targets

```
┌────────────────────────────────────────────────────────────────────────┐
│                        TARGET SUCCESS CRITERIA                         │
├────────────────────────────────┬───────────────────────────────────────┤
│ Objective                      │ Target Metric                         │
├────────────────────────────────┼───────────────────────────────────────┤
│ Eliminate Refill Delays        │ 0% cancellation of chronic orders      │
│                                │ due to sudden pharmacy stockouts      │
├────────────────────────────────┼───────────────────────────────────────┤
│ High Senior Engagement         │ > 40% of elderly refill confirmations  │
│                                │ completed via simple SMS              │
├────────────────────────────────┼───────────────────────────────────────┤
│ Zero Expired Medicine Waste    │ 100% adherence to FEFO dispensing rule│
├────────────────────────────────┼───────────────────────────────────────┤
│ Accessible for All Demographics│ Full compliance with WCAG AA standards │
└────────────────────────────────┴───────────────────────────────────────┘
```
