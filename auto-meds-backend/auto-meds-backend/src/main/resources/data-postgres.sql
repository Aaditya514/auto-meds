-- Clean up existing demo accounts
DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE patient_id IN (1, 2, 3));
DELETE FROM carts WHERE patient_id IN (1, 2, 3);
DELETE FROM users WHERE email IN ('admin@automeds.com', 'patient@automeds.com', 'aaditya.aanand@intellectdesign.com');

-- Seed Initial Admin (Password: admin123)
INSERT INTO users (id, name, email, password, role, phone, address, city, state, pincode, created_at)
VALUES (1, 'System Admin', 'admin@automeds.com', '$2a$10$MSYFCF3WLusC.GHKCFwRyuPCjhI65GCc6p82UaWpx7DQ4FgOiobFu', 'ADMIN', '9999999999', 'Admin Head Office', 'Metropolis', 'Central', '100001', CURRENT_TIMESTAMP);

-- Seed Initial Patient (Password: patient123)
INSERT INTO users (id, name, email, password, role, phone, address, city, state, pincode, created_at)
VALUES (2, 'Aaditya Aanand', 'patient@automeds.com', '$2a$10$yUz5ArwMHRoL78.aqp7dfucyOBVhIfLFL/ih3FEsCkWfYm7pvk7sW', 'PATIENT', '9876543210', '123 Health Ave, Suite 4B', 'Greenville', 'Stateville', '560001', CURRENT_TIMESTAMP);

-- Seed Personal Patient Account for Aaditya Aanand (Password: patient123)
INSERT INTO users (id, name, email, password, role, phone, address, city, state, pincode, created_at)
VALUES (3, 'Aaditya Aanand', 'aaditya.aanand@intellectdesign.com', '$2a$10$yUz5ArwMHRoL78.aqp7dfucyOBVhIfLFL/ih3FEsCkWfYm7pvk7sW', 'PATIENT', '9876543210', '123 Health Ave, Suite 4B', 'Chennai', 'Tamil Nadu', '600001', CURRENT_TIMESTAMP);

-- Initial Patient Carts Creation
INSERT INTO carts (id, patient_id, created_at, updated_at)
VALUES (1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO carts (id, patient_id, created_at, updated_at)
VALUES (2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Sample Medicines demonstrating Alternatives
-- ID 1: Metformin 500mg - ABC Pharma (OUT OF STOCK)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (1, 'Metformin 500mg', 'ABC Pharma', 'Metformin', '500mg', 'Diabetes', 100.00, 0, 1, 'First-line medication for the treatment of type 2 diabetes.', 'ABC Pharmaceuticals', CURRENT_TIMESTAMP + INTERVAL '24 months', 1);

-- ID 2: Metformin 500mg - XYZ Pharma (IN STOCK Alternative 1)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (2, 'Metformin 500mg', 'XYZ Pharma', 'Metformin', '500mg', 'Diabetes', 80.00, 50, 1, 'Metformin hydrochloride 500mg sustained release formulation.', 'XYZ Healthcare', CURRENT_TIMESTAMP + INTERVAL '18 months', 1);

-- ID 3: Metformin 500mg - PQR Pharma (IN STOCK Alternative 2)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (3, 'Metformin 500mg', 'PQR Pharma', 'Metformin', '500mg', 'Diabetes', 75.00, 25, 1, 'Metformin hydrochloride tablets 500mg.', 'PQR Labs', CURRENT_TIMESTAMP + INTERVAL '20 months', 1);

-- ID 4: Metformin 1000mg - ABC Pharma (DIFFERENT STRENGTH - Should NOT match as 500mg alternative)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (4, 'Metformin 1000mg', 'ABC Pharma', 'Metformin', '1000mg', 'Diabetes', 140.00, 40, 1, 'Higher dosage metformin 1000mg formulation.', 'ABC Pharmaceuticals', CURRENT_TIMESTAMP + INTERVAL '24 months', 1);

-- ID 5: Amlodipine 5mg - ABC Pharma (IN STOCK)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (5, 'Amlodipine 5mg', 'ABC Pharma', 'Amlodipine', '5mg', 'Cardiovascular', 50.00, 100, 1, 'Calcium channel blocker used to treat high blood pressure.', 'ABC Pharmaceuticals', CURRENT_TIMESTAMP + INTERVAL '12 months', 1);

-- ID 6: Amlodipine 5mg - Sun Pharma (IN STOCK Alternative)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (6, 'Amlodipine 5mg', 'Sun Pharma', 'Amlodipine', '5mg', 'Cardiovascular', 45.00, 80, 1, 'Amlodipine besylate tablets 5mg.', 'Sun Pharma Industries', CURRENT_TIMESTAMP + INTERVAL '30 months', 1);

-- ID 7: Atorvastatin 10mg - Cipla (IN STOCK)
INSERT INTO medicines (id, medicine_name, brand_name, composition, strength, category, price, stock_quantity, requires_prescription, description, manufacturer, expiry_date, active)
VALUES (7, 'Atorvastatin 10mg', 'Cipla', 'Atorvastatin', '10mg', 'Cholesterol', 110.00, 60, 1, 'Statin medication used to prevent cardiovascular disease.', 'Cipla Ltd', CURRENT_TIMESTAMP + INTERVAL '15 months', 1);
