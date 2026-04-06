-- DEPRECATED: This file is no longer used for Docker initialization.
-- Schema is now managed by:
--   - backend/src/main/resources/rfbooks_schema.sql (Docker init)
--   - backend/src/main/resources/db/migration/ (Flyway migrations)
-- Kept for reference only.

-- RF Books Database Schema
-- Schema-based multitenancy: This schema will be created in each tenant's schema
-- Note: search_path should be set before running this script

-- ============================================
-- EXPENSE / INCOME TABLES
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

CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_reconciled ON expenses(reconciled);
CREATE UNIQUE INDEX IF NOT EXISTS idx_expenses_user_external_id ON expenses(user_id, external_id) WHERE external_id IS NOT NULL;

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

CREATE INDEX IF NOT EXISTS idx_income_user_id ON income(user_id);
CREATE INDEX IF NOT EXISTS idx_income_date ON income(income_date);
CREATE INDEX IF NOT EXISTS idx_income_reconciled ON income(reconciled);
CREATE UNIQUE INDEX IF NOT EXISTS idx_income_user_external_id ON income(user_id, external_id) WHERE external_id IS NOT NULL;

-- ============================================
-- PLAID / BANK CONNECTION TABLES
-- ============================================

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

CREATE INDEX IF NOT EXISTS idx_plaid_user_id ON plaid_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_plaid_active ON plaid_connections(active);

-- Cached Plaid transactions
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

CREATE INDEX IF NOT EXISTS idx_plaid_tx_user_id ON plaid_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_plaid_tx_date ON plaid_transactions(date);
CREATE INDEX IF NOT EXISTS idx_plaid_tx_transaction_id ON plaid_transactions(transaction_id);

-- ============================================
-- RECONCILIATION TABLES
-- ============================================

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

CREATE INDEX IF NOT EXISTS idx_recon_user_id ON reconciliation_runs(user_id);
CREATE INDEX IF NOT EXISTS idx_recon_run_at ON reconciliation_runs(run_at DESC);

-- Manual expense-to-transaction matches
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

CREATE INDEX IF NOT EXISTS idx_match_exp_user_id ON manual_match_expenses(user_id);

-- Manual income-to-transaction matches
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

CREATE INDEX IF NOT EXISTS idx_match_inc_user_id ON manual_match_income(user_id);

-- Match suggestions (auto-match engine results)
CREATE TABLE IF NOT EXISTS match_suggestions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    item_type VARCHAR(10) NOT NULL,
    item_id BIGINT NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    confidence_score INTEGER NOT NULL,
    match_reasons TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_suggestions_user ON match_suggestions(user_id, status);

-- ============================================
-- ONBOARDING TABLES
-- ============================================

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

CREATE INDEX IF NOT EXISTS idx_coa_user_id ON chart_of_accounts(user_id);

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

CREATE INDEX IF NOT EXISTS idx_ps_user_id ON products_services(user_id);

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

-- ============================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_expenses_updated_at') THEN
        CREATE TRIGGER update_expenses_updated_at BEFORE UPDATE ON expenses
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_income_updated_at') THEN
        CREATE TRIGGER update_income_updated_at BEFORE UPDATE ON income
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_plaid_connections_updated_at') THEN
        CREATE TRIGGER update_plaid_connections_updated_at BEFORE UPDATE ON plaid_connections
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_chart_of_accounts_updated_at') THEN
        CREATE TRIGGER update_chart_of_accounts_updated_at BEFORE UPDATE ON chart_of_accounts
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_products_services_updated_at') THEN
        CREATE TRIGGER update_products_services_updated_at BEFORE UPDATE ON products_services
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_onboarding_progress_updated_at') THEN
        CREATE TRIGGER update_onboarding_progress_updated_at BEFORE UPDATE ON onboarding_progress
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;
