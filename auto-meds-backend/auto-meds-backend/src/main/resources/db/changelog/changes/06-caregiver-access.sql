--liquibase formatted sql

--changeset automeds:06-caregiver-access
CREATE TABLE IF NOT EXISTS caregiver_access (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caregiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permissions VARCHAR(100) NOT NULL DEFAULT 'NOTIFICATIONS,PAY_ON_BEHALF',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    relationship_label VARCHAR(50),
    notify_phone VARCHAR(20),
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    revoked_at TIMESTAMP,
    CONSTRAINT uk_caregiver_patient UNIQUE (patient_id, caregiver_id)
);

CREATE INDEX IF NOT EXISTS idx_caregiver_access_patient ON caregiver_access(patient_id);
CREATE INDEX IF NOT EXISTS idx_caregiver_access_caregiver ON caregiver_access(caregiver_id);
CREATE INDEX IF NOT EXISTS idx_caregiver_access_phone ON caregiver_access(notify_phone);

--rollback DROP TABLE IF EXISTS caregiver_access CASCADE;

