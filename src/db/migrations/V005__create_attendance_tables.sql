-- V005 — Create attendance module tables
-- Module: attendance
-- Docs: 06-data/models.md, 09-modules/attendance/data-model.md
-- BR: BR-03 (entry rejected when membership is not active)

CREATE TABLE attendance_attendance_records (
    id          BIGSERIAL   PRIMARY KEY,
    member_id   BIGINT      NOT NULL,
    timestamp   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    direction   VARCHAR(3)  NOT NULL,
    CONSTRAINT ck_attendance_direction
        CHECK (direction IN ('IN', 'OUT'))
);

CREATE INDEX idx_attendance_member_timestamp
    ON attendance_attendance_records (member_id, timestamp DESC);