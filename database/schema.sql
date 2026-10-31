-- ============================================================
-- Brightloop Billing – Database Schema
-- MySQL 8+ compatible
-- ============================================================

CREATE DATABASE IF NOT EXISTS brightloop_billing
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE brightloop_billing;

-- -----------------------------------------------
-- Users (admin, staff, customer)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','STAFF','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Taxes
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS taxes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    rate       DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Products
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    type       VARCHAR(100),
    sale_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes      TEXT,
    tax_id     BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_tax FOREIGN KEY (tax_id) REFERENCES taxes(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Product Variants
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    name        VARCHAR(200) NOT NULL,
    price_addon DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_var_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Plans
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS plans (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    price           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    billing_period  ENUM('DAILY','WEEKLY','MONTHLY','YEARLY') NOT NULL DEFAULT 'MONTHLY',
    min_qty         INT NOT NULL DEFAULT 1,
    available_from  DATE NOT NULL,
    available_to    DATE NOT NULL,
    can_auto_close  TINYINT(1) NOT NULL DEFAULT 0,
    can_close       TINYINT(1) NOT NULL DEFAULT 1,
    can_pause       TINYINT(1) NOT NULL DEFAULT 1,
    can_renew       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Subscriptions
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS subscriptions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_no     VARCHAR(30) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    plan_id             BIGINT NOT NULL,
    status              ENUM('DRAFT','QUOTATION','CONFIRMED','ACTIVE','PAUSED','CLOSED') NOT NULL DEFAULT 'DRAFT',
    start_date          DATE NOT NULL,
    expiry_date         DATE NOT NULL,
    next_bill_date      DATE,
    payment_terms_days  INT NOT NULL DEFAULT 15,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_sub_plan     FOREIGN KEY (plan_id)     REFERENCES plans(id)
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Subscription Lines
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS subscription_lines (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    product_id      BIGINT,
    variant_id      BIGINT,
    description     VARCHAR(300) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_rate        DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_sl_sub  FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_sl_prod FOREIGN KEY (product_id)      REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT fk_sl_var  FOREIGN KEY (variant_id)      REFERENCES product_variants(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Invoices
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_no       VARCHAR(30) NOT NULL UNIQUE,
    billing_key      VARCHAR(200) NOT NULL UNIQUE,  -- prevents duplicates: subId@billingDate
    subscription_id  BIGINT NOT NULL,
    customer_id      BIGINT NOT NULL,
    invoice_date     DATE NOT NULL,
    due_date         DATE NOT NULL,
    subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status           ENUM('DRAFT','CONFIRMED','PAID','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    sent             TINYINT(1) NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_sub  FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),
    CONSTRAINT fk_inv_cust FOREIGN KEY (customer_id)     REFERENCES users(id)
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Invoice Lines (snapshot at time of invoicing)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_lines (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id  BIGINT NOT NULL,
    description VARCHAR(300) NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_rate    DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    line_total  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_il_inv FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Payments
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id     BIGINT NOT NULL,
    amount         DECIMAL(12,2) NOT NULL,
    method         ENUM('UPI','CARD','CASH','BANK_TRANSFER','CHEQUE') NOT NULL DEFAULT 'CASH',
    payment_date   DATE NOT NULL,
    note           TEXT,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_inv FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- Invoice sequence counter
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS sequences (
    seq_name  VARCHAR(50) PRIMARY KEY,
    next_val  BIGINT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

INSERT INTO sequences (seq_name, next_val) VALUES ('invoice', 1), ('subscription', 1)
  ON DUPLICATE KEY UPDATE seq_name = seq_name;
