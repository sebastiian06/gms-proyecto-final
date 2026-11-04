-- V006 — Create training module tables
-- Module: training
-- Docs: 06-data/models.md, 09-modules/training/data-model.md
-- BR: BR-04 (trainer can only assign routines to their own members)

CREATE TABLE training_trainers (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    full_name   VARCHAR(200) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_training_trainers_user_id ON training_trainers (user_id);

CREATE TABLE training_routines (
    id          BIGSERIAL    PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    trainer_id  BIGINT       NOT NULL,
    name        VARCHAR(200) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_training_routines_training_trainers_trainer_id
        FOREIGN KEY (trainer_id) REFERENCES training_trainers (id) ON DELETE RESTRICT
);

CREATE INDEX idx_training_routines_member_id  ON training_routines (member_id);
CREATE INDEX idx_training_routines_trainer_id ON training_routines (trainer_id);

CREATE TABLE training_exercises (
    id             BIGSERIAL    PRIMARY KEY,
    routine_id     BIGINT       NOT NULL,
    name           VARCHAR(200) NOT NULL,
    sets           INTEGER      NOT NULL,
    repetitions    INTEGER      NOT NULL,
    display_order  INTEGER      NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_training_exercises_training_routines_routine_id
        FOREIGN KEY (routine_id) REFERENCES training_routines (id) ON DELETE CASCADE,
    CONSTRAINT ck_training_exercises_sets_positive        CHECK (sets > 0),
    CONSTRAINT ck_training_exercises_repetitions_positive CHECK (repetitions > 0),
    CONSTRAINT ck_training_exercises_display_order        CHECK (display_order >= 0),
    CONSTRAINT uq_training_exercises_routine_order UNIQUE (routine_id, display_order)
);

CREATE INDEX idx_training_exercises_routine_id ON training_exercises (routine_id);