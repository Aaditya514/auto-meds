-- ============================================================================
-- Auto-Meds Schema Migration: V4__phase4_resilience_and_ocr.sql
-- Description: Phase 4 Persona UX, OCR Data, Order-Prescription Double Check,
--              Symptom Taxonomy, and Chronic Care Resilience (Bridge Supply & Snooze).
-- ============================================================================

-- 1. Add prescription reference, bridge supply flag, and dispensing notes to Orders
ALTER TABLE orders 
    ADD COLUMN IF NOT EXISTS prescription_id BIGINT REFERENCES prescriptions(id),
    ADD COLUMN IF NOT EXISTS is_bridge_supply BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS dispensing_notes VARCHAR(500);

-- 2. Add OCR extraction storage to Prescriptions
ALTER TABLE prescriptions 
    ADD COLUMN IF NOT EXISTS ocr_data TEXT;

-- 3. Add Bridge Supply tracking and Snooze tracking to Subscriptions
ALTER TABLE subscriptions 
    ADD COLUMN IF NOT EXISTS is_bridge_supply BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS last_snooze_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS snooze_count INTEGER NOT NULL DEFAULT 0;

-- 4. Add Symptoms taxonomy to Medicines for ailment discovery
ALTER TABLE medicines 
    ADD COLUMN IF NOT EXISTS symptoms VARCHAR(255);

-- 5. Seed common symptom keywords into active catalog medicines
UPDATE medicines SET symptoms = 'Diabetes, Blood Sugar, Type 2 Diabetes, High Glucose' 
    WHERE LOWER(medicine_name) LIKE '%metformin%' OR LOWER(composition) LIKE '%metformin%';

UPDATE medicines SET symptoms = 'Hypertension, High Blood Pressure, Heart Health, Angina' 
    WHERE LOWER(medicine_name) LIKE '%amlodipine%' OR LOWER(composition) LIKE '%amlodipine%';

UPDATE medicines SET symptoms = 'Cholesterol, High Lipid, Cardiovascular, Triglycerides' 
    WHERE LOWER(medicine_name) LIKE '%atorvastatin%' OR LOWER(composition) LIKE '%atorvastatin%';

UPDATE medicines SET symptoms = 'Fever, Headache, Body Ache, Pain, Mild Arthritis, Flu' 
    WHERE LOWER(medicine_name) LIKE '%paracetamol%' OR LOWER(composition) LIKE '%paracetamol%';

UPDATE medicines SET symptoms = 'Acidity, Acid Reflux, GERD, Heartburn, Gastric, Stomach Ulcer' 
    WHERE LOWER(medicine_name) LIKE '%pantoprazole%' OR LOWER(composition) LIKE '%pantoprazole%';

UPDATE medicines SET symptoms = 'Allergy, Sneezing, Runny Nose, Skin Rash, Itching, Hay Fever' 
    WHERE LOWER(medicine_name) LIKE '%cetirizine%' OR LOWER(composition) LIKE '%cetirizine%';

UPDATE medicines SET symptoms = 'Hypertension, High Blood Pressure, Heart Failure' 
    WHERE LOWER(medicine_name) LIKE '%telmisartan%' OR LOWER(composition) LIKE '%telmisartan%';

UPDATE medicines SET symptoms = 'Bacterial Infection, Sore Throat, Bronchitis, Respiratory Infection' 
    WHERE LOWER(medicine_name) LIKE '%amoxicillin%' OR LOWER(medicine_name) LIKE '%azithromycin%';

UPDATE medicines SET symptoms = 'Pain, Swelling, Inflammation, Joint Pain, Muscle Pain' 
    WHERE LOWER(medicine_name) LIKE '%ibuprofen%' OR LOWER(composition) LIKE '%ibuprofen%';

-- 6. Link existing orders to their subscription's prescription if available (Data backfill)
UPDATE orders o
SET prescription_id = s.prescription_id
FROM subscriptions s
WHERE o.subscription_id = s.id AND o.prescription_id IS NULL AND s.prescription_id IS NOT NULL;
