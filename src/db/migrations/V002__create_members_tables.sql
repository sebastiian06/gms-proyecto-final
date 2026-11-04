-- V002 — Create members module tables
-- Module: members
-- Docs: 06-data/models.md, 09-modules/members/data-model.md
-- BR: BR-08 (members are never deleted, only deactivated)

CREATE TABLE members_members (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    document_number  VARCHAR(50)  NOT NULL,
    full_name        VARCHAR(200) NOT NULL,
    contact          VARCHAR(200) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_members_members_document_number UNIQUE (document_number)
);

-- user_id references identity_users, but no FK crosses the module boundary.
-- See 06-data/database-conventions.md: validated by IdentityFacade at write time.
CREATE INDEX idx_members_members_user_id ON members_members (user_id);
CREATE INDEX idx_members_members_active  ON members_members (active);