-- V008 — Add RECEPTIONIST role to identity_users
-- The role column had a CHECK constraint listing ADMIN, TRAINER, MEMBER.
-- This migration drops it and recreates it with RECEPTIONIST added.

ALTER TABLE identity_users DROP CONSTRAINT IF EXISTS ck_identity_users_role;

ALTER TABLE identity_users ADD CONSTRAINT ck_identity_users_role
    CHECK (role IN ('ADMIN', 'RECEPTIONIST', 'TRAINER', 'MEMBER'));