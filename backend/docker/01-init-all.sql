-- DEPRECATED: This file is no longer used for Docker initialization.
-- Schema is now managed by:
--   - backend/src/main/resources/rfbooks_schema.sql (Docker init)
--   - backend/src/main/resources/rfbooks_test_data.sql (Docker init test data)
--   - backend/src/main/resources/db/migration/ (Flyway migrations)
-- Kept for reference only.

-- RF Books Database Initialization
-- Creates testresort schema and applies tables + test data

CREATE SCHEMA IF NOT EXISTS testresort;
GRANT ALL PRIVILEGES ON SCHEMA testresort TO postgres;
SET search_path TO testresort, public;

-- ============================================
-- TABLES
-- ============================================

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expense_date DATE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    vendor_name VARCHAR(255),
    category VARCHAR(100),
    account_id BIGINT,
    payment_method VARCHAR(100),
    reference_number VARCHAR(100),
    description TEXT,
    notes TEXT,
    reconciled BOOLEAN DEFAULT FALSE,
    resolved BOOLEAN DEFAULT FALSE,
    external_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    income_date DATE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    source VARCHAR(255),
    category VARCHAR(100),
    account_id BIGINT,
    payment_method VARCHAR(100),
    reference_number VARCHAR(100),
    description TEXT,
    notes TEXT,
    reconciled BOOLEAN DEFAULT FALSE,
    resolved BOOLEAN DEFAULT FALSE,
    external_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plaid_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255),
    sync_cursor TEXT,
    connected_at TIMESTAMP NOT NULL,
    last_synced_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, item_id)
);

CREATE TABLE IF NOT EXISTS plaid_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(500),
    merchant_name VARCHAR(500),
    category VARCHAR(255),
    pending BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reconciliation_runs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    run_at TIMESTAMP NOT NULL,
    start_date VARCHAR(50) NOT NULL,
    end_date VARCHAR(50) NOT NULL,
    matched_count INTEGER DEFAULT 0,
    unmatched_payment_count INTEGER DEFAULT 0,
    unmatched_bank_count INTEGER DEFAULT 0,
    total_payments INTEGER DEFAULT 0,
    total_bank_transactions INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    error_message VARCHAR(1000),
    results_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS manual_match_expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expense_id BIGINT NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    confidence_score INTEGER,
    match_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    matched_at TIMESTAMP NOT NULL,
    matched_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, expense_id),
    UNIQUE(user_id, transaction_id)
);

CREATE TABLE IF NOT EXISTS manual_match_income (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    income_id BIGINT NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    confidence_score INTEGER,
    match_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    matched_at TIMESTAMP NOT NULL,
    matched_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, income_id),
    UNIQUE(user_id, transaction_id)
);

CREATE TABLE IF NOT EXISTS match_suggestions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expense_id BIGINT,
    income_id BIGINT,
    transaction_id VARCHAR(255) NOT NULL,
    confidence_score INTEGER NOT NULL,
    match_reasons TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chart_of_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, account_number)
);

CREATE TABLE IF NOT EXISTS products_services (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    default_price DECIMAL(12, 2),
    unit_of_measure VARCHAR(50),
    description TEXT,
    revenue_account_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS onboarding_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    bank_connected BOOLEAN DEFAULT FALSE,
    chart_of_accounts_created BOOLEAN DEFAULT FALSE,
    products_services_created BOOLEAN DEFAULT FALSE,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_expenses_updated_at BEFORE UPDATE ON expenses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_income_updated_at BEFORE UPDATE ON income
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_plaid_connections_updated_at BEFORE UPDATE ON plaid_connections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_chart_of_accounts_updated_at BEFORE UPDATE ON chart_of_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_products_services_updated_at BEFORE UPDATE ON products_services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_onboarding_progress_updated_at BEFORE UPDATE ON onboarding_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- TEST DATA
-- ============================================

INSERT INTO plaid_transactions (user_id, transaction_id, account_id, amount, date, name, merchant_name, pending)
VALUES
  ('default-user', 'tx_i_001', 'acc_test', 1500.00, CURRENT_DATE - 1, 'Room Rental - Smith', 'Smith Family', false),
  ('default-user', 'tx_i_002', 'acc_test', 3500.00, CURRENT_DATE - 1, 'Restaurant Daily Sales', 'Restaurant', false),
  ('default-user', 'tx_i_003', 'acc_test', 2000.00, CURRENT_DATE - 2, 'Suite Rental - Johnson', 'Johnson Family', false),
  ('default-user', 'tx_i_004', 'acc_test', 1800.00, CURRENT_DATE - 3, 'ACH Deposit Williams', 'Williams Family', false),
  ('default-user', 'tx_i_005', 'acc_test', 5000.00, CURRENT_DATE - 3, 'Catering Event Payment', 'Corporate Event', false),
  ('default-user', 'tx_i_006', 'acc_test', 2500.00, CURRENT_DATE - 5, 'Cabin Rental - Brown', 'Brown Family', false),
  ('default-user', 'tx_i_007', 'acc_test', 3200.00, CURRENT_DATE - 7, 'Executive Suite - Davis', 'Davis Family', false),
  ('default-user', 'tx_i_999', 'acc_test', 2750.00, CURRENT_DATE - 4, 'Unmatched Credit', 'Unknown Source', false),
  ('default-user', 'tx_e_001', 'acc_test', -1200.00, CURRENT_DATE - 1, 'SYSCO Food Service', 'Sysco', false),
  ('default-user', 'tx_e_002', 'acc_test', -2000.00, CURRENT_DATE - 1, 'Wine Company ACH', 'Wine Co', false),
  ('default-user', 'tx_e_003', 'acc_test', -15000.00, CURRENT_DATE - 1, 'Payroll ACH Transfer', 'Payroll', false),
  ('default-user', 'tx_e_004', 'acc_test', -2500.00, CURRENT_DATE - 2, 'US Foods Delivery', 'US Foods', false),
  ('default-user', 'tx_e_005', 'acc_test', -2000.00, CURRENT_DATE - 1, 'Electric Bill ACH', 'Power Company', false),
  ('default-user', 'tx_e_006', 'acc_test', -1500.00, CURRENT_DATE - 3, 'Beer Depot Purchase', 'Beer Depot', false),
  ('default-user', 'tx_e_007', 'acc_test', -2500.00, CURRENT_DATE - 2, 'HVAC Repair Invoice', 'HVAC Pro', false),
  ('default-user', 'tx_e_008', 'acc_test', -1850.00, CURRENT_DATE - 7, 'Restaurant Depot', 'Restaurant Depot', false),
  ('default-user', 'tx_e_999', 'acc_test', -1450.00, CURRENT_DATE - 3, 'Unmatched Debit', 'Unknown Vendor', false)
ON CONFLICT (transaction_id) DO NOTHING;

INSERT INTO expenses (user_id, expense_date, amount, vendor_name, category, payment_method, reference_number, notes)
VALUES
  ('default-user', CURRENT_DATE - 1, 1200.00, 'Sysco', 'Food Costs', 'ACH', 'INV-12345', 'Fresh Produce'),
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Wine Co', 'Beverage Costs', 'ACH', 'INV-W123', 'Wine Selection'),
  ('default-user', CURRENT_DATE - 1, 15000.00, 'Payroll', 'Payroll', 'ACH', 'PAY-001', 'Staff Salaries'),
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Power Company', 'Utilities', 'ACH', 'ELEC-123', 'Electric Bill'),
  ('default-user', CURRENT_DATE - 2, 2500.00, 'US Foods', 'Food Costs', 'ACH', 'INV-12346', 'Meat & Seafood'),
  ('default-user', CURRENT_DATE - 2, 2500.00, 'HVAC Pro', 'Maintenance', 'Card', NULL, 'HVAC Repair'),
  ('default-user', CURRENT_DATE - 3, 1500.00, 'Beer Depot', 'Beverage Costs', 'Card', NULL, 'Craft Beer'),
  ('default-user', CURRENT_DATE - 7, 1850.00, 'Restaurant Depot', 'Food Costs', 'Card', NULL, 'Bulk Food Items');

INSERT INTO income (user_id, income_date, amount, source, category, payment_method, reference_number, description)
VALUES
  ('default-user', CURRENT_DATE - 1, 1500.00, 'Smith Family', 'Room Revenue', 'Card', 'ROOM-101', 'Cabin Rental - 3 nights'),
  ('default-user', CURRENT_DATE - 1, 3500.00, 'Restaurant Sales', 'Food & Beverage', 'Card', 'REST-DAILY', 'Daily Restaurant Revenue'),
  ('default-user', CURRENT_DATE - 2, 2000.00, 'Johnson Family', 'Room Revenue', 'Card', 'ROOM-203', 'Suite Rental - 4 nights'),
  ('default-user', CURRENT_DATE - 3, 1800.00, 'Williams Family', 'Room Revenue', 'ACH', 'ROOM-105', 'RV Site Rental'),
  ('default-user', CURRENT_DATE - 3, 5000.00, 'Corporate Event', 'Food & Beverage', 'ACH', 'EVENT-001', 'Catering - Corporate Retreat'),
  ('default-user', CURRENT_DATE - 5, 2500.00, 'Brown Family', 'Room Revenue', 'Card', 'CABIN-5', 'Premium Cabin - 5 nights'),
  ('default-user', CURRENT_DATE - 7, 3200.00, 'Davis Family', 'Room Revenue', 'Card', 'SUITE-301', 'Executive Suite - 5 nights');
