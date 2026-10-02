-- ============================================================================
-- Auto-Meds Migration V2: V2__inventory_reservation_and_indexes.sql
-- Description: Inventory soft-locking, reorder thresholds, and composite indexes.
-- ============================================================================

-- 1. Ensure reservation and reorder columns exist on medicines
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS reserved_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS reorder_threshold INTEGER NOT NULL DEFAULT 10;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS suggested_reorder_pack_size INTEGER NOT NULL DEFAULT 50;

-- 2. Ensure reservation status and date columns exist on subscriptions
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS reservation_status VARCHAR(30) DEFAULT 'NONE';
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS reservation_date TIMESTAMP;

-- 3. Ensure ShedLock table exists for distributed schedulers
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

-- 4. High-Performance Composite Indexes (Senior Transcript §3.3)
CREATE INDEX IF NOT EXISTS idx_subscriptions_status_refill 
    ON subscriptions(status, next_refill_date);

CREATE INDEX IF NOT EXISTS idx_subscriptions_reservation 
    ON subscriptions(status, reservation_status);

CREATE INDEX IF NOT EXISTS idx_medicines_comp_strength 
    ON medicines(composition, strength);

CREATE INDEX IF NOT EXISTS idx_medicines_active_name 
    ON medicines(active, medicine_name);

CREATE INDEX IF NOT EXISTS idx_orders_patient_date 
    ON orders(patient_id, order_date DESC);
