--liquibase formatted sql

--changeset automeds:04-orders-resilience
ALTER TABLE orders 
    ADD COLUMN IF NOT EXISTS prescription_id BIGINT REFERENCES prescriptions(id),
    ADD COLUMN IF NOT EXISTS is_bridge_supply BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS dispensing_notes VARCHAR(500);

--rollback ALTER TABLE orders DROP COLUMN IF EXISTS prescription_id;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS is_bridge_supply;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS dispensing_notes;

--changeset automeds:04-prescriptions-ocr
ALTER TABLE prescriptions 
    ADD COLUMN IF NOT EXISTS ocr_data TEXT;

--rollback ALTER TABLE prescriptions DROP COLUMN IF EXISTS ocr_data;

--changeset automeds:04-subscriptions-bridge-snooze
ALTER TABLE subscriptions 
    ADD COLUMN IF NOT EXISTS is_bridge_supply BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS last_snooze_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS snooze_count INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE subscriptions DROP COLUMN IF EXISTS is_bridge_supply;
--rollback ALTER TABLE subscriptions DROP COLUMN IF EXISTS last_snooze_date;
--rollback ALTER TABLE subscriptions DROP COLUMN IF EXISTS snooze_count;

--changeset automeds:04-medicines-symptoms
ALTER TABLE medicines 
    ADD COLUMN IF NOT EXISTS symptoms VARCHAR(255);

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

--rollback ALTER TABLE medicines DROP COLUMN IF EXISTS symptoms;

--changeset automeds:04-link-orders-prescription
UPDATE orders o
SET prescription_id = s.prescription_id
FROM subscriptions s
WHERE o.subscription_id = s.id AND o.prescription_id IS NULL AND s.prescription_id IS NOT NULL;

