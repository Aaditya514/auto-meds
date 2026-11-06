--liquibase formatted sql

--changeset automeds:05-audit-logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT,
    actor_name VARCHAR(150),
    actor_role VARCHAR(50),
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    details TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at DESC);

--rollback DROP TABLE IF EXISTS audit_logs CASCADE;

--changeset automeds:05-orders-payment-slip
ALTER TABLE orders 
    ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_gateway_response TEXT,
    ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS dispensing_slip_code VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_orders_transaction_id ON orders(transaction_id);

--rollback ALTER TABLE orders DROP COLUMN IF EXISTS transaction_id;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS payment_gateway_response;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS paid_at;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS dispensing_slip_code;

--changeset automeds:05-users-allergies-conditions
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS allergies VARCHAR(255),
    ADD COLUMN IF NOT EXISTS chronic_conditions VARCHAR(255);

--rollback ALTER TABLE users DROP COLUMN IF EXISTS allergies;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS chronic_conditions;

--changeset automeds:05-seed-audit-init
INSERT INTO audit_logs (actor_name, actor_role, action, resource_type, details)
VALUES ('SYSTEM', 'SYSTEM_ADMIN', 'COMPLIANCE_ENGINE_INITIALIZED', 'SYSTEM', 'Auto-Meds Phase 5 Regulatory Compliance Audit Engine Activated');

