-- V007 — Create trainer-member assignment table
-- Module: training
-- Docs: 09-modules/training/decisions.md (DEC-01)
-- Purpose: explicit BR-04 enforcement — a trainer can only assign routines
-- to members assigned to them. Stored separately from routines so a member
-- with no routines yet is still assignable.

CREATE TABLE training_trainer_members (
    id          BIGSERIAL   PRIMARY KEY,
    trainer_id  BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_training_trainer_members_training_trainers_trainer_id
        FOREIGN KEY (trainer_id) REFERENCES training_trainers (id) ON DELETE CASCADE,
    CONSTRAINT uq_training_trainer_members_pair UNIQUE (trainer_id, member_id)
);

CREATE INDEX idx_training_trainer_members_member_id ON training_trainer_members (member_id);