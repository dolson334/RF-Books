-- Initialize RF Books database with testresort schema and test data
-- This runs only on first container creation

-- Create test resort schema
CREATE SCHEMA IF NOT EXISTS testresort;
GRANT ALL PRIVILEGES ON SCHEMA testresort TO postgres;

-- Set search path for this session
SET search_path TO testresort, public;

-- ============================================
-- CREATE ALL TABLES
-- ============================================

-- Onboarding Progress tracking
CREATE TABLE IF NOT EXISTS onboarding_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    bank_connected BOOLEAN DEFAULT FALSE,
    chart_of_accounts_created BOOLEAN DEFAULT FALSE,
    products_services_created BOOLEAN DEFAULT FALSE,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Chart of Accounts
CREATE TABLE IF NOT EXISTS chart_of_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, account_number)
);

CREATE INDEX idx_coa_user_id ON chart_of_accounts(user_id);
CREATE INDEX idx_coa_type ON chart_of_accounts(account_type);

-- Products and Services
CREATE TABLE IF NOT EXISTS products_services (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2),
    cost BIGINT,
    sku VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_products_services_user_id ON products_services(user_id);
CREATE INDEX idx_products_services_type ON products_services(type);

-- Plaid Connections
CREATE TABLE IF NOT EXISTS plaid_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Plaid Transactions
CREATE TABLE IF NOT EXISTS plaid_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    account_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(500) NOT NULL,
    merchant_name VARCHAR(500),
    pending BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plaid_tx_user_date ON plaid_transactions(user_id, date);
CREATE INDEX idx_plaid_tx_transaction_id ON plaid_transactions(transaction_id);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_date TIMESTAMP NOT NULL,
    method VARCHAR(50),
    last4 VARCHAR(4),
    guest_name VARCHAR(255),
    reservation_id VARCHAR(100),
    reconciled BOOLEAN DEFAULT FALSE,
    source VARCHAR(50) DEFAULT 'rfbooks',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_user_date ON payments(user_id, payment_date);
CREATE INDEX idx_payments_external_id ON payments(external_id);

-- Reconciliation Runs
CREATE TABLE IF NOT EXISTS reconciliation_runs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    run_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATE,
    end_date DATE,
    matched_count INTEGER DEFAULT 0,
    unmatched_payment_count INTEGER DEFAULT 0,
    unmatched_bank_count INTEGER DEFAULT 0,
    total_payments INTEGER DEFAULT 0,
    total_bank_transactions INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    error_message TEXT,
    results_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recon_run_user_date ON reconciliation_runs(user_id, run_at DESC);

-- Manual Matches
CREATE TABLE IF NOT EXISTS manual_matches (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    matched_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_manual_match_payment UNIQUE (user_id, payment_id),
    CONSTRAINT uk_manual_match_transaction UNIQUE (user_id, transaction_id)
);

CREATE INDEX idx_manual_match_user ON manual_matches(user_id);
CREATE INDEX idx_manual_match_payment ON manual_matches(payment_id);
CREATE INDEX idx_manual_match_transaction ON manual_matches(transaction_id);

-- Expenses
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expense_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    vendor_name VARCHAR(255),
    category VARCHAR(100),
    account_id BIGINT,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    description TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expense_user ON expenses(user_id);
CREATE INDEX idx_expense_date ON expenses(expense_date);
CREATE INDEX idx_expense_vendor ON expenses(vendor_name);

-- ============================================
-- INSERT TEST DATA
-- ============================================

-- Insert test payments
INSERT INTO payments (user_id, external_id, amount, currency, payment_date, method, last4, guest_name, reservation_id, reconciled, source, created_at, updated_at)
VALUES
  ('default-user', 'pi_001', 178.50, 'USD', NOW() - INTERVAL '1 day', 'Card', '4242', 'Sarah Thompson', 'RV-1245', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_002', 642.00, 'USD', NOW() - INTERVAL '2 days', 'ACH', NULL, 'Mark & Jenna Lewis', 'CAB-87', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_003', 82.00, 'USD', NOW() - INTERVAL '3 days', 'Card', '1111', 'Daniel H.', 'TENT-331', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_004', 245.75, 'USD', NOW() - INTERVAL '4 days', 'Card', '5678', 'Jessica Martinez', 'CAB-92', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_005', 156.00, 'USD', NOW() - INTERVAL '5 days', 'ACH', NULL, 'Robert Chen', 'RV-1250', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_006', 320.00, 'USD', NOW() - INTERVAL '6 days', 'Card', '9012', 'Emily & James Wilson', 'TENT-335', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_007', 189.00, 'USD', NOW() - INTERVAL '7 days', 'Card', '3456', 'Michael Johnson', 'RV-1255', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_008', 95.50, 'USD', NOW() - INTERVAL '8 days', 'Card', '7890', 'Amanda Brooks', 'TENT-340', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_009', 412.00, 'USD', NOW() - INTERVAL '9 days', 'ACH', NULL, 'David & Lisa Parker', 'CAB-95', false, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_010', 275.25, 'USD', NOW() - INTERVAL '10 days', 'Card', '2468', 'Christopher Lee', 'RV-1260', false, 'rfbooks', NOW(), NOW())
ON CONFLICT (external_id) DO NOTHING;

-- Insert test Plaid transactions
INSERT INTO plaid_transactions (user_id, transaction_id, account_id, amount, date, name, merchant_name, pending, created_at)
VALUES
  ('default-user', 'tx_001', 'acc_test', 178.50, (NOW() - INTERVAL '1 day')::date, 'Visa Settlement · Sarah T.', 'Sarah Thompson', false, NOW()),
  ('default-user', 'tx_002', 'acc_test', 642.00, (NOW() - INTERVAL '2 days')::date, 'ACH Deposit · Lewis Family', 'Lewis Family', false, NOW()),
  ('default-user', 'tx_003', 'acc_test', 245.75, (NOW() - INTERVAL '4 days')::date, 'Online Payment Receipt', 'Online Payment', false, NOW()),
  ('default-user', 'tx_004', 'acc_test', 156.00, (NOW() - INTERVAL '5 days')::date, 'ACH Transfer - Chen', 'Robert Chen', false, NOW()),
  ('default-user', 'tx_005', 'acc_test', 320.00, (NOW() - INTERVAL '6 days')::date, 'Card Payment - Wilson', 'Wilson Family', false, NOW()),
  ('default-user', 'tx_006', 'acc_test', 99.50, (NOW() - INTERVAL '7 days')::date, 'Mystery Transaction', 'Unknown', false, NOW()),
  ('default-user', 'tx_007', 'acc_test', 125.00, (NOW() - INTERVAL '8 days')::date, 'Bank Fee - Monthly Service', 'Bank of America', false, NOW()),
  ('default-user', 'tx_008', 'acc_test', 450.00, (NOW() - INTERVAL '9 days')::date, 'Wire Transfer In', 'External Account', false, NOW()),
  ('default-user', 'tx_009', 'acc_test', 67.80, (NOW() - INTERVAL '10 days')::date, 'ATM Withdrawal', 'Chase ATM', false, NOW()),
  ('default-user', 'tx_010', 'acc_test', 299.99, (NOW() - INTERVAL '11 days')::date, 'Check Deposit #1234', 'Mobile Deposit', false, NOW())
ON CONFLICT (transaction_id) DO NOTHING;

\echo 'RF Books database initialized successfully with test data!'
