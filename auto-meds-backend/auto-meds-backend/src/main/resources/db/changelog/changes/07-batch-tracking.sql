--liquibase formatted sql

-- ============================================================================
-- Auto-Meds Changelog: 07-batch-tracking.sql
-- Description: Batch number column on medicines catalog for CDSCO compliance & traceability
-- ============================================================================

--changeset automeds:07-medicines-batch-tracking
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS batch_number VARCHAR(50);
--rollback ALTER TABLE medicines DROP COLUMN IF EXISTS batch_number;
