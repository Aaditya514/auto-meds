# ⚖️ Feature Prioritization & Decision Matrix: Auto-Meds

> **Document Type:** Feature Evaluation & Prioritization Framework  
> **Target Audience:** Product Team, Engineering Leads, Clinical Advisors  
> **Frameworks Used:** RICE Scoring (Reach, Impact, Confidence, Effort) + Clinical Risk Assessment  
> **Timeline / Perspective:** Pre-Build Architecture & Scoping Phase  
> **Author:** Aaditya Aanand  

---

## 1. Prioritization Framework & Criteria

When planning a healthcare and chronic medicine management platform, feature selection cannot rely purely on consumer engagement or virality. Features must be evaluated across **clinical patient safety**, **operational reliability**, and **accessibility**.

We utilized a dual-layer evaluation framework:
1. **RICE Scoring** to quantify return on engineering effort:
   $$\text{RICE Score} = \frac{\text{Reach} \times \text{Impact} \times \text{Confidence}}{\text{Effort}}$$
   * **Reach:** Estimated volume of target users impacted each cycle.
   * **Impact:** $3 = \text{Critical/Life-Saving}$, $2 = \text{High Patient Value}$, $1 = \text{Moderate Convenience}$, $0.5 = \text{Low}$.
   * **Confidence:** $100\% = \text{High Clarity}$, $80\% = \text{Medium Clarity}$, $50\% = \text{Speculative}$.
   * **Effort:** Estimated developer weeks to design, build, and test.

2. **Clinical Safety & Compliance Gate:** Any capability that touches prescription validity, drug dispensing, or inventory reserves must guarantee that errors will not result in adverse health events.

---

## 2. Feature Prioritization Matrix & Agile Sprint Mapping

The table below outlines how candidate features were scored, prioritized, and scheduled into incremental Agile delivery slices:

| Feature Candidate | Target User Problem | Agile Release Slice | Priority | RICE Score | Team Rationale & Trade-Off |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **User Authentication & Catalog Cart** | Foundational ordering functionality. | **Release 0 (Base Model)** | **P0 (Must-Have)** | **320** | Non-negotiable foundation. Proves basic catalog search, cart checkout, and user account creation. |
| **Scheduled Auto-Refills (6-Phase Stepper)** | Patients forgetting monthly medication reorders, causing missed doses. | **Iteration 1** | **P0 (Must-Have)** | **288** | Core reason for the platform to exist. Automates the recurring cycle so chronic therapy is never dropped. |
| **Two-Tier Inventory Reservation** | Walk-in counter buyers depleting medicine stock promised to chronic patients. | **Iteration 1** | **P0 (Must-Have)** | **240** | Essential for trust. An auto-refill platform that fails on refill day due to preventable stockouts destroys patient confidence immediately. |
| **Prescription Intake & Pharmacist Verification** | Ensuring drugs are dispensed only against valid, legal doctor prescriptions. | **Iteration 2** | **P0 (Must-Have)** | **210** | Non-negotiable legal and regulatory requirement for any licensed pharmacy platform. |
| **Drug Interaction (DDI) Safety Checks** | Polypharmacy patients accidentally combining medications with harmful interactions. | **Iteration 2** | **P1 (High)** | **135** | Crucial patient safety differentiator compared to commercial retail apps. Prevents adverse clinical reactions. |
| **Universal Senior Accessibility (WCAG AA)** | Seniors with tremors and poor eyesight struggling with standard tiny buttons. | **Iteration 3** | **P1 (High)** | **180** | Universal inclusion. Ensures buttons are $\ge 44\text{px}$ with high-contrast text and font scaling. |
| **Interactive SMS Refill ("Reply YES")** | Elderly and low-tech patients struggling with mobile apps and web logins. | **Iteration 3** | **P1 (High)** | **144** | High demographic reach with zero barrier to entry. Drastically improves refill confirmation rates for seniors. |
| **Caregiver Delegation Portal** | Elderly parents unable to manage digital apps; remote family unable to help. | **Iteration 4** | **P1 (High)** | **160** | Critical demographic need. In chronic care, the primary manager is often the adult child rather than the elderly patient alone. |
| **Near-Expiry Alerts & FEFO Dispensing** | Risk of dispensing degraded medicines; high inventory write-offs for the pharmacy. | **Iteration 5** | **P1 (High)** | **180** | Protects patient clinical safety and prevents costly pharmacy waste by prioritizing earliest-expiring stock first. |
| **Closed-Loop Deficit Restocking** | Subscriptions stalled by temporary stockouts with no automated recovery. | **Iteration 5** | **P1 (High)** | **120** | Operational necessity. Ensures incoming new stock automatically unblocks waiting patients before going on general sale. |
| **Bio-Equivalent Generic Substitution** | Patients unable to afford expensive brands or blocked by single-brand stockouts. | **Iteration 5** | **P2 (Medium)** | **96** | Promotes cost savings and therapy continuation by matching identical active chemical salts and strengths. |
| **One-Touch "Simple Mode" Interface** | Seniors overwhelmed by complex navigation and small text. | **Post-MVP Backlog** | **P2 (Medium)** | **80** | High value, but partially addressed in initial release by WCAG AA large targets and SMS confirmation. Slated for Post-MVP. |
| **Direct Insurance Claim Adjudication (EDI)** | Manual reimbursement claiming for insured patients. | **Future Horizon** | **P3 (Deferred)** | **32** | Extreme integration complexity with third-party insurance payers; deferred in favor of direct payment / CoD in MVP. |
| **In-App Telehealth Video Consultations** | Patients lacking a prescription needing an immediate doctor appointment. | **Future Horizon** | **P3 (Deferred)** | **24** | Outside the core focus. The platform is designed as an adherence and dispensing engine, not a telemedicine clinic. |

---

## 3. Key Design Debates & Evaluated Trade-Offs

### Debate 1: Two-Tier Inventory Reservation vs. Immediate Hard Deduction
* **Context:** When a patient's refill is due in 5 days, should the system immediately deduct the stock from the warehouse count or place a temporary reservation?
* **Alternative Considered:** Immediately reduce the physical stock count 5 days in advance.
* **Chosen Approach:** Maintain a two-tier system: `stock_quantity` (physical items on shelf) and `reserved_quantity` (items earmarked for upcoming refills). Calculate available stock as `stock_quantity - reserved_quantity`.
* **Rationale:** Deducting stock 5 days early creates discrepancies during physical warehouse audits. Furthermore, if a patient pauses or changes their dose during the confirmation window, reversing a hard deduction causes inventory mismatches. A reservation guarantees stock while keeping physical counts accurate.

### Debate 2: Native Mobile Application vs. Accessible Web App + SMS Gateway
* **Context:** Should initial development focus on building separate iOS and Android native apps or a responsive accessible web app paired with SMS?
* **Alternative Considered:** Build native mobile apps on iOS and Android.
* **Chosen Approach:** Responsive Web App designed to WCAG AA standards paired with interactive 2-way SMS ("Reply YES").
* **Rationale:** Elderly chronic patients have the lowest App Store fluency and the highest app abandonment rates. Building native apps introduces download barriers, app updates, and storage constraints on budget phones. A web app accessible on any browser, combined with direct SMS confirmation for non-digital users, guarantees 100% reach across all age groups.

### Debate 3: Real-Time WebSockets vs. Server-Sent Events (SSE)
* **Context:** Pharmacist triage dashboards and refill progress bars need live updates as statuses change.
* **Alternative Considered:** Full-duplex WebSockets.
* **Chosen Approach:** Server-Sent Events (SSE).
* **Rationale:** Pharmacy dashboard notifications and status progressions are one-way (server pushing updates to the browser). WebSockets add connection management overhead, require specialized proxy handling, and are prone to disconnections on corporate firewalls. SSE is lightweight, operates over standard HTTP, and reconnects automatically.
