--liquibase formatted sql

-- ============================================================================
-- Auto-Meds Changelog: 09-seed-initial-data.sql
-- Description: Seed initial Admin user, demo Patient, and sample medicines catalog
-- ============================================================================

--changeset automeds:09-seed-users
INSERT INTO users (name, email, password, role, phone, address, city, state, pincode, active, created_at, updated_at)
VALUES 
('System Admin', 'admin@automeds.com', '$2a$10$fK4Vjtxal1kcX1aAeOR8tOSk9UNfZTX7BLS.tkyAHhPTa.u4Y2lV2', 'ADMIN', '9999999999', 'Admin Head Office', 'Metropolis', 'Central', '100001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Demo Patient', 'patient@automeds.com', '$2a$10$Cg9m1Mb35vVJLtT7xRJ.R.XxFqyF/nPxPC3rfXP43jmnWbqvha0ie', 'PATIENT', '9876543210', '123 Health Ave, Suite 4B', 'Greenville', 'Stateville', '560001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO carts (patient_id, created_at, updated_at)
SELECT id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'patient@automeds.com'
ON CONFLICT (patient_id) DO NOTHING;

--rollback DELETE FROM users WHERE email IN ('admin@automeds.com', 'patient@automeds.com');

--changeset automeds:09-seed-medicines
INSERT INTO medicines (medicine_name, brand_name, composition, strength, category, price, stock_quantity, reserved_quantity, reorder_threshold, suggested_reorder_pack_size, requires_prescription, description, manufacturer, expiry_date, batch_number, active, symptoms)
VALUES
('Metformin 500mg', 'ABC Pharma', 'Metformin', '500mg', 'Diabetes', 100.00, 50, 0, 10, 50, 1, 'First-line medication for type 2 diabetes.', 'ABC Pharmaceuticals', CURRENT_TIMESTAMP + INTERVAL '24 months', 'BATCH-MET-01', 1, 'Diabetes, Blood Sugar, Type 2 Diabetes, High Glucose'),
('Metformin 500mg', 'XYZ Pharma', 'Metformin', '500mg', 'Diabetes', 80.00, 50, 0, 10, 50, 1, 'Metformin hydrochloride 500mg sustained release.', 'XYZ Healthcare', CURRENT_TIMESTAMP + INTERVAL '18 months', 'BATCH-MET-02', 1, 'Diabetes, Blood Sugar, Type 2 Diabetes, High Glucose'),
('Amlodipine 5mg', 'Sun Pharma', 'Amlodipine', '5mg', 'Cardiovascular', 45.00, 80, 0, 10, 50, 1, 'Amlodipine besylate tablets 5mg.', 'Sun Pharma Industries', CURRENT_TIMESTAMP + INTERVAL '30 months', 'BATCH-AML-01', 1, 'Hypertension, High Blood Pressure, Heart, Chest Pain'),
('Atorvastatin 10mg', 'Cipla', 'Atorvastatin', '10mg', 'Cholesterol', 110.00, 60, 0, 10, 50, 1, 'Statin medication for cholesterol management.', 'Cipla Ltd', CURRENT_TIMESTAMP + INTERVAL '15 months', 'BATCH-ATO-01', 1, 'High Cholesterol, Triglycerides, Heart Health'),
('Amoxicillin 500mg', 'GSK', 'Amoxicillin', '500mg', 'Antibiotics', 120.00, 40, 0, 10, 50, 1, 'Broad-spectrum penicillin antibiotic.', 'GlaxoSmithKline', CURRENT_TIMESTAMP + INTERVAL '20 months', 'BATCH-AMX-01', 1, 'Bacterial Infection, Sore Throat, Bronchitis, Respiratory'),
('Ibuprofen 400mg', 'Abbott', 'Ibuprofen', '400mg', 'Pain Relief', 35.00, 100, 0, 10, 50, 0, 'Nonsteroidal anti-inflammatory drug (NSAID).', 'Abbott Healthcare', CURRENT_TIMESTAMP + INTERVAL '24 months', 'BATCH-IBU-01', 1, 'Pain, Swelling, Inflammation, Joint Pain, Muscle Pain')
ON CONFLICT DO NOTHING;

--rollback DELETE FROM medicines WHERE batch_number IN ('BATCH-MET-01', 'BATCH-MET-02', 'BATCH-AML-01', 'BATCH-ATO-01', 'BATCH-AMX-01', 'BATCH-IBU-01');
