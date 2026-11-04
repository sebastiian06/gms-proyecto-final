-- V004 — Create billing module tables
-- Module: billing
-- Docs: 06-data/models.md, 09-modules/billing/data-model.md
-- BR: BR-01, BR-02, BR-06

CREATE TABLE billing_memberships (
    id            BIGSERIAL     PRIMARY KEY,
    member_id     BIGINT        NOT NULL,
    plan_id       BIGINT        NOT NULL,
    locked_price  NUMERIC(10,2) NOT NULL,
    start_date    DATE          NOT NULL,
    end_date      DATE          NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_billing_memberships_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_billing_memberships_dates
        CHECK (end_date >= start_date),
    CONSTRAINT ck_billing_memberships_locked_price_positive
        CHECK (locked_price >= 0)
);

-- BR-01: at most one active membership per member
CREATE UNIQUE INDEX uq_billing_memberships_member_active
    ON billing_memberships (member_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_billing_memberships_member_id ON billing_memberships (member_id);
CREATE INDEX idx_billing_memberships_plan_id   ON billing_memberships (plan_id);

CREATE TABLE billing_payments (
    id             BIGSERIAL     PRIMARY KEY,
    membership_id  BIGINT        NOT NULL,
    amount         NUMERIC(10,2) NOT NULL,
    method         VARCHAR(20)   NOT NULL,
    recorded_date  DATE          NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_billing_payments_billing_memberships_membership_id
        FOREIGN KEY (membership_id) REFERENCES billing_memberships (id) ON DELETE CASCADE,
    CONSTRAINT ck_billing_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_billing_payments_method
        CHECK (method IN ('CASH', 'TRANSFER', 'CARD'))
);

CREATE INDEX idx_billing_payments_membership_id ON billing_payments (membership_id);