-- V003 — Create plans module tables
-- Module: plans
-- Docs: 06-data/models.md, 09-modules/plans/data-model.md
-- BR: BR-05 (retired plans cannot be assigned to a new membership)

CREATE TABLE plans_plans (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    price           NUMERIC(10,2) NOT NULL,
    duration_days   INTEGER      NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_plans_plans_price_positive       CHECK (price >= 0),
    CONSTRAINT ck_plans_plans_duration_positive    CHECK (duration_days > 0)
);

-- Only one active plan per name
CREATE UNIQUE INDEX uq_plans_plans_name_active
    ON plans_plans (name)
    WHERE active = TRUE;