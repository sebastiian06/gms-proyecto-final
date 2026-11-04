-- V010 — Add is_system flag to identity_users
-- The system admin (created by AdminSeeder) cannot be deactivated or deleted
-- by any other user.

ALTER TABLE identity_users
    ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark the existing admin as system if it exists
UPDATE identity_users SET is_system = TRUE WHERE username = 'admin';