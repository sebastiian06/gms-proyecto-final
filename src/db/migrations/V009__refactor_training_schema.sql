-- V009 — Refactor training schema for Model A
-- Model A: each trainer owns a set of members. Each member has exactly one trainer.
--          Each member has at most one active routine.
--          A routine can be assigned to many members.

-- 1. Remove member_id from training_routines (routines are no longer 1:1 with members)
ALTER TABLE training_routines DROP COLUMN IF EXISTS member_id;

-- 2. Create routine assignments table (routine_id, member_id)
CREATE TABLE training_routine_assignments (
    id          BIGSERIAL    PRIMARY KEY,
    routine_id  BIGINT       NOT NULL,
    member_id   BIGINT       NOT NULL,
    assigned_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_training_routine_assignments_routines
        FOREIGN KEY (routine_id) REFERENCES training_routines (id) ON DELETE CASCADE,
    CONSTRAINT uq_training_routine_assignments_member UNIQUE (member_id)
);

CREATE INDEX idx_training_routine_assignments_routine_id
    ON training_routine_assignments (routine_id);

-- 3. Ensure one trainer per member (was: unique pair trainer+member)
ALTER TABLE training_trainer_members
    DROP CONSTRAINT IF EXISTS uq_training_trainer_members_pair;

ALTER TABLE training_trainer_members
    ADD CONSTRAINT uq_training_trainer_members_member UNIQUE (member_id);