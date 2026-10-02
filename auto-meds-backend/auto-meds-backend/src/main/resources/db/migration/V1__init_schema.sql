-- ============================================================================
-- Auto-Meds Baseline Schema: V1__init_schema.sql
-- Description: Core enterprise schema initialization with constraints and relations.
-- ============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    active INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Medicines Catalog
CREATE TABLE IF NOT EXISTS medicines (
    id BIGSERIAL PRIMARY KEY,
    medicine_name VARCHAR(150) NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    composition VARCHAR(150) NOT NULL,
    strength VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    price NUMERIC(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    reorder_threshold INTEGER NOT NULL DEFAULT 10,
    suggested_reorder_pack_size INTEGER NOT NULL DEFAULT 50,
    requires_prescription INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    manufacturer VARCHAR(100),
    expiry_date TIMESTAMP,
    active INTEGER NOT NULL DEFAULT 1
);

-- 3. Prescriptions Table
CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    doctor_visit_date TIMESTAMP,
    expiry_date TIMESTAMP,
    rejection_reason VARCHAR(255),
    clarification_notes VARCHAR(255),
    verified_by_id BIGINT REFERENCES users(id),
    verification_date TIMESTAMP
);

-- 4. Subscriptions Table
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    medicine_id BIGINT REFERENCES medicines(id),
    prescription_id BIGINT REFERENCES prescriptions(id),
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    quantity INTEGER,
    start_date TIMESTAMP,
    next_refill_date TIMESTAMP,
    next_dispatch_date TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reservation_status VARCHAR(30) DEFAULT 'NONE',
    reservation_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES users(id),
    subscription_id BIGINT REFERENCES subscriptions(id),
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10, 2) NOT NULL,
    delivery_address VARCHAR(255),
    payment_method VARCHAR(50),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    order_type VARCHAR(30) NOT NULL DEFAULT 'ONE_TIME',
    expected_delivery_date TIMESTAMP
);

-- 6. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    medicine_id BIGINT NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL
);

-- 7. Cart Table
CREATE TABLE IF NOT EXISTS cart (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

-- 8. Cart Items Table
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    medicine_id BIGINT NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    price_at_addition NUMERIC(10, 2) NOT NULL,
    CONSTRAINT unique_cart_medicine UNIQUE (cart_id, medicine_id)
);

-- 9. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Refresh Tokens Table (Dual-Token Security Gateway)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. ShedLock Distributed Scheduler Lock Table
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
