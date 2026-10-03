# AutoMeds Design System & UX Engine Manifesto (`DESIGN.md`)

> **Single Source of Truth** for UI/UX architecture, visual hierarchy, component tokens, and senior-designer reasoning across the AutoMeds Clinical & Patient platforms.
> Modeled after the **Design Motion UX Engine** principles to eradicate "AI slop" and guarantee state completeness, intentional density, and clinical precision.

---

## 1. Core Visual Character & Principles

1. **Strict Information Hierarchy & Triage**:
   - Surface actionable clinical items first (stockout deficits, prescription verification, low-stock warnings) instead of flat grids of vanity numbers.
   - Use the **F/Z reading pattern** with high-contrast anchors.

2. **Purposeful, Subtle Iconography & Clean Tokens**:
   - **Never** use loud saturated side borders (`border-start border-4`).
   - Use clean **1px neutral slate borders** (`#E2E8F0`).
   - Use refined **32px–36px icon capsules** with 10% opacity pastel backgrounds.
   - Employ tabular numbers (`font-variant-numeric: tabular-nums`) for currency, stock counts, and timestamps.

3. **Generous Negative Space & Density**:
   - Maintain 24px–32px vertical rhythm (`py-4`, `mb-4`, `g-4`).
   - Content groupings must feel open, breathe naturally, and minimize cognitive friction for clinicians and patients.

4. **Action-Oriented Decision Support**:
   - Avoid read-only dead ends. Every deficit, stockout, or alert must have a **1-click resolution action** (e.g., inline batch restock, 1-click alternative molecule selection, instant prescription audit).

5. **State Completeness**:
   Every component must explicitly handle and design for all 6 states:
   - **Loading State**: Subtle spinner or skeleton placeholder with context text.
   - **Empty State**: Purposeful illustration/icon, reassuring message, and primary CTA.
   - **Error State**: Non-blocking toast/alert with recovery path.
   - **Partial / Pending State**: Progress stepper or pending triage indicator.
   - **Success State**: Clear, temporary confirmation feedback.
   - **Disabled State**: Explanatory title attribute or inline guidance.

---

## 2. Color Palette & Semantic Tokens

| Token Name | Hex Code | Light Tint (10%) | Semantic Usage |
| :--- | :--- | :--- | :--- |
| **Slate Primary (Header & Text)** | `#0F172A` | `#F8FAFC` | Primary headings, dark badges, command bars |
| **Brand Primary (Sky/Blue)** | `#0284C7` | `#F0F9FF` | Primary actions, brand accents, active navigation |
| **Clinical Emerald (Success)** | `#059669` | `#ECFDF5` | In-stock badges, active subscriptions, successful delivery |
| **Amber Caution (Pending/Triage)** | `#D97706` | `#FFFBEB` | Pending pharmacist triage, low-stock reorder warnings |
| **Rose / Crimson (Critical/Error)** | `#DC2626` | `#FEF2F2` | Out-of-stock, DDI critical alerts, urgent deficits |
| **Teal Secondary (Refill/Care)** | `#0F766E` | `#F0FDFA` | Automatic refills, prescription verification stamps |
| **Neutral Slate Border** | `#E2E8F0` | `#F1F5F9` | 1px card borders, table dividers, input borders |
| **Subtle Hover Border** | `#CBD5E1` | — | Card hover states, focused input borders |

---

## 3. Component Anatomy

### 3.1. Clinical Cards (`.card-pharma`)
```css
.card-pharma {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.22s cubic-bezier(0.16, 1, 0.3, 1);
}

.card-pharma:hover {
  transform: translateY(-2px);
  border-color: #cbd5e1;
  box-shadow: 0 10px 24px -4px rgba(0, 0, 0, 0.08), 0 6px 12px -4px rgba(0, 0, 0, 0.04);
}
```

### 3.2. Status Badges & Pills
* **Active / In-Stock**: `.badge-in-stock` (`background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; font-size: 0.72rem; font-weight: 700; border-radius: 20px;`)
* **Pending / Triage**: `.badge-pending` (`background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A;`)
* **Out-of-Stock / Deficit**: `.badge-out-of-stock` (`background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA;`)
* **Brand Pill**: `.badge-brand` (`background: #F1F5F9; color: #475569; border: 1px solid #E2E8F0; border-radius: 6px;`)

### 3.3. Enterprise Tables
* **Table Header (`thead th`)**:
  - `background-color: #F8FAFC`
  - `color: #64748B`
  - `font-size: 0.74rem`
  - `font-weight: 700`
  - `text-transform: uppercase`
  - `letter-spacing: 0.04em`
  - `border-bottom: 1px solid #E2E8F0`
* **Table Rows (`tbody tr`)**:
  - `transition: background-color 0.15s ease`
  - Hover: `background-color: #F8FAFC`
  - Numeric cells: `font-variant-numeric: tabular-nums`

### 3.4. Clinical Modals
* **Backdrop**: `rgba(15, 23, 42, 0.65)` with `backdrop-filter: blur(6px)`.
* **Container**: `border-radius: 16px`, `box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25)`, `border: 1px solid #E2E8F0`.

---

## 4. The 8 UX Engine Senior-Designer Skills

| Skill | Definition | AutoMeds Implementation |
| :--- | :--- | :--- |
| **1. Intent Discovery** | Identify user intent and worst-case scenarios before writing UI. | The Pharmacist Command Center surfaces stockout deficits and pending Rx audits first, preventing missed patient deliveries. |
| **2. Information Hierarchy** | Rank primary, secondary, and tertiary actions with strict scanning flow. | Primary action (Restock, Approve) is prominent; secondary (Clarify, Edit) is outline; destructive (Cancel, Reject) is guarded. |
| **3. State Completeness** | Explicitly handle Loading, Empty, Error, Success, Partial, and Disabled states. | Every catalog, order desk, and subscription view has bespoke empty illustrations, reset actions, and error handling. |
| **4. Form UX & Progressive Disclosure** | Group related fields, provide smart defaults, and defer complexity. | Add Medicine uses 1-click preset disease tags; Checkout groups Shipping Destination, Instant Payment, and Order Summary. |
| **5. Visual Character** | Enforce consistent aesthetic tokens across every tab. | Shared `.card-pharma`, 1px `#E2E8F0` borders, and clinical slate-to-teal gradients across both portals. |
| **6. Micro-Interactions** | Provide subtle tactile motion to confirm user intent. | Subtle hover lift (`translateY(-2px)`), smooth button active states, and animated progress bars for OCR scanning. |
| **7. Error Recovery** | Avoid dead ends when errors or stockouts occur. | Out-of-Stock medicine cards provide a 1-click **"Find Alternatives"** modal showing same composition and strength in-stock options. |
| **8. Accessibility & Clarity** | Ensure readable contrast, legible typography, and clear semantic labels. | Inter / system sans-serif typography, high contrast text on pastel pills, and explicit ARIA labels. |

---

## 5. Verification Checklist for Any Future Screen

Before committing any new tab or component:
- [ ] Are all loud side-borders (`border-start border-4`) eliminated in favor of 1px `#E2E8F0`?
- [ ] Does the card lift subtly on hover (`transform: translateY(-2px)`)?
- [ ] Are all 6 states handled: Loading, Empty, Error, Success, Partial, Disabled?
- [ ] Do currency amounts and quantities use `tabular-nums`?
- [ ] Is there a search or filter toolbar if the view contains tabular data?
- [ ] Are primary actions obvious, secondary subtle, and destructive actions protected?
