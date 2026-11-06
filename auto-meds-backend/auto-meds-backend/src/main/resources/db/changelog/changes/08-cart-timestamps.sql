--liquibase formatted sql

-- ============================================================================
-- Auto-Meds Changelog: 08-cart-timestamps.sql
-- Description: Add created_at and updated_at timestamp columns to carts table
-- ============================================================================

--changeset automeds:08-carts-timestamps
ALTER TABLE carts ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE carts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE cart ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE cart ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

--rollback ALTER TABLE carts DROP COLUMN IF EXISTS created_at;
--rollback ALTER TABLE carts DROP COLUMN IF EXISTS updated_at;
--rollback ALTER TABLE cart DROP COLUMN IF EXISTS created_at;
--rollback ALTER TABLE cart DROP COLUMN IF EXISTS updated_at;
