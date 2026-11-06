--liquibase formatted sql

--changeset automeds:02-composite-indexes
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

--rollback DROP INDEX IF EXISTS idx_subscriptions_status_refill;
--rollback DROP INDEX IF EXISTS idx_subscriptions_reservation;
--rollback DROP INDEX IF EXISTS idx_medicines_comp_strength;
--rollback DROP INDEX IF EXISTS idx_medicines_active_name;
--rollback DROP INDEX IF EXISTS idx_orders_patient_date;

