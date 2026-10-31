-- ============================================================
-- Brightloop Billing – Seed / Demo Data
-- Run AFTER schema.sql
-- ============================================================

USE brightloop_billing;

-- Passwords are BCrypt hashes:
--   Admin@1234  → below hash
--   Staff@1234  → below hash
--   Cust@12345  → below hash

INSERT INTO users (name, email, password_hash, role) VALUES
  ('Asha Admin',   'admin@brightloop.io', '$2a$10$7EqJtq98hPqEX7fNZaFWoO9Nv1jJe0Xb1PTjmEZS8zMkT4JxIoR.i', 'ADMIN'),
  ('Purnava Staff','staff@brightloop.io', '$2a$10$iSN7HRwJR5Kx1mY0aBCZJ.pFZ1wK4e0t9YKu0R5n.k6bLoKZHmFGa', 'STAFF'),
  ('Acme Retail',  'cust@acme.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p36ogYoKrAA1hdS5GCz1ea', 'CUSTOMER'),
  ('Globex Corp',  'ops@globex.com',      '$2a$10$6eB1yt7F5kq0P9HeTLLpxuTlN2M8q3qPLT5MFR5DLf9TqR6yVQUDe', 'CUSTOMER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO taxes (name, rate) VALUES
  ('GST 18%', 18.00),
  ('GST 5%',   5.00)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Products (tax_id=1 = GST 18%, tax_id=2 = GST 5%)
INSERT INTO products (name, type, sale_price, cost_price, notes, tax_id) VALUES
  ('Brightloop CRM',    'Software', 1200.00, 400.00, 'Core CRM seat',       1),
  ('Analytics Add-on',  'Add-on',    400.00, 120.00, 'Advanced analytics',  1),
  ('Priority Support',  'Service',   250.00,  80.00, 'Priority SLA support', 2)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Variants for Brightloop CRM (product_id=1)
INSERT INTO product_variants (product_id, name, price_addon) VALUES
  (1, 'Brand: Odoo', 560.00),
  (1, 'Brand: Zoho', 300.00)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Plans
INSERT INTO plans (name, price, billing_period, min_qty, available_from, available_to, can_auto_close, can_close, can_pause, can_renew) VALUES
  ('Pro Monthly',      500.00, 'MONTHLY', 1, '2026-01-01', '2027-12-31', 0, 1, 1, 1),
  ('Enterprise Yearly',5000.00,'YEARLY',  5, '2026-01-01', '2027-12-31', 1, 0, 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Subscriptions (customer_id 3 = Acme, 4 = Globex; plan_id 1 = Pro Monthly, 2 = Enterprise Yearly)
INSERT INTO subscriptions (subscription_no, customer_id, plan_id, status, start_date, expiry_date, next_bill_date, payment_terms_days) VALUES
  ('SUB-0001', 3, 1, 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 70 DAY), DATE_ADD(CURDATE(), INTERVAL 295 DAY), CURDATE(), 15),
  ('SUB-0002', 4, 1, 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_ADD(CURDATE(), INTERVAL 330 DAY), CURDATE(), 15),
  ('SUB-0003', 4, 2, 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 355 DAY), CURDATE(), 30),
  ('SUB-0004', 3, 1, 'DRAFT',  CURDATE(),                            DATE_ADD(CURDATE(), INTERVAL 365 DAY), CURDATE(), 15)
ON DUPLICATE KEY UPDATE subscription_no = VALUES(subscription_no);

-- Subscription lines (we need the subscription IDs – use subquery)
INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, 1, 1, 'Brightloop CRM (Brand: Odoo)', 12, 1760.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0001'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, NULL, NULL, 'Plan fee – Pro Monthly', 1, 500.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0001'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, 2, NULL, 'Analytics Add-on', 4, 400.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0002'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, NULL, NULL, 'Plan fee – Pro Monthly', 1, 500.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0002'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, 1, NULL, 'Brightloop CRM', 6, 1200.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0003'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, NULL, NULL, 'Plan fee – Enterprise Yearly', 1, 5000.00, 18.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0003'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO subscription_lines (subscription_id, product_id, variant_id, description, quantity, unit_price, tax_rate)
SELECT s.id, 3, NULL, 'Priority Support', 2, 250.00, 5.00
FROM subscriptions s WHERE s.subscription_no = 'SUB-0004'
ON DUPLICATE KEY UPDATE description = VALUES(description);

UPDATE sequences SET next_val = 5 WHERE seq_name = 'subscription';
