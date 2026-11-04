-- Development seed data — never run in qa or main.
-- Applied manually after Flyway migrations.

-- Three identity users, one per role
INSERT INTO identity_users (username, password_hash, role) VALUES
    ('admin',   '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'ADMIN'),
    ('trainer1','$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'TRAINER'),
    ('member1', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'MEMBER');

-- Access code for member1 (id=3)
INSERT INTO identity_access_codes (user_id, code) VALUES
    (3, '0426');

-- Member profile for member1
INSERT INTO members_members (user_id, document_number, full_name, contact) VALUES
    (3, '12345678', 'Juan Pérez', '+57 300 123 4567');

-- A basic plan
INSERT INTO plans_plans (name, price, duration_days) VALUES
    ('Basic', 50000.00, 30);

-- Active membership for member1 on the Basic plan
INSERT INTO billing_memberships (member_id, plan_id, locked_price, start_date, end_date, status) VALUES
    (1, 1, 50000.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'ACTIVE');

-- One payment
INSERT INTO billing_payments (membership_id, amount, method, recorded_date) VALUES
    (1, 50000.00, 'CASH', CURRENT_DATE);

-- Trainer profile for trainer1
INSERT INTO training_trainers (user_id, full_name) VALUES
    (2, 'Carlos Rodríguez');

-- Assign member1 to trainer1
INSERT INTO training_trainer_members (trainer_id, member_id) VALUES
    (1, 1);