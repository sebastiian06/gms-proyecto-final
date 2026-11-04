-- V001 — Create identity module tables
-- Module: identity
-- Docs: 06-data/models.md, 09-modules/identity/data-model.md
-- BR: BR-07 (access code uniqueness among active codes)

-- Users: authentication subjects (ADMIN, TRAINER, MEMBER)
CREATE TABLE identity_users (
    id              BIGSERIAL    PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_users_username UNIQUE (username),
    CONSTRAINT ck_identity_users_role
        CHECK (role IN ('ADMIN', 'TRAINER', 'MEMBER'))
);

-- Access codes: the credential used at the gym kiosk
CREATE TABLE identity_access_codes (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    code            VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_identity_access_codes_identity_users_user_id
        FOREIGN KEY (user_id) REFERENCES identity_users (id) ON DELETE CASCADE
);

-- BR-07: an access code is unique among active access codes only
CREATE UNIQUE INDEX uq_identity_access_codes_code_active
    ON identity_access_codes (code)
    WHERE active = TRUE;

-- Query indexes
CREATE INDEX idx_identity_access_codes_user_id
    ON identity_access_codes (user_id);