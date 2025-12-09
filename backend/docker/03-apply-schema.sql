-- Apply schema to test resort
-- This runs after init-db.sql

-- ============================================
-- Apply schema to testresort
-- ============================================
\echo 'Applying schema to testresort...'
SET search_path TO testresort, public;

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
    description TEXT,
    parent_account_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_account_id) REFERENCES chart_of_accounts(id) ON DELETE SET NULL,
    UNIQUE(user_id, account_number)
);

CREATE INDEX IF NOT EXISTS idx_coa_user_id ON chart_of_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_coa_type ON chart_of_accounts(account_type);

-- Products and Services
CREATE TABLE IF NOT EXISTS products_services (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    cost DECIMAL(10, 2),
    sku VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX IF NOT EXISTS idx_ps_user_id ON products_services(user_id);
CREATE INDEX IF NOT EXISTS idx_ps_type ON products_services(type);

-- Plaid Bank Connections
CREATE TABLE IF NOT EXISTS plaid_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255),
    connected_at TIMESTAMP NOT NULL,
    last_synced_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_plaid_user_id ON plaid_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_plaid_active ON plaid_connections(active);

-- Reconciliation Run Results
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
CREATE INDEX IF NOT EXISTS idx_recon_status ON reconciliation_runs(status);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER update_onboarding_progress_updated_at BEFORE UPDATE ON onboarding_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chart_of_accounts_updated_at BEFORE UPDATE ON chart_of_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_services_updated_at BEFORE UPDATE ON products_services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plaid_connections_updated_at BEFORE UPDATE ON plaid_connections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

\echo 'Schema applied to client_default successfully.'

-- ============================================
-- Apply schema to client_testresort
-- ============================================
\echo 'Applying schema to client_testresort...'
SET search_path TO client_testresort, public;

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
    description TEXT,
    parent_account_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_account_id) REFERENCES chart_of_accounts(id) ON DELETE SET NULL,
    UNIQUE(user_id, account_number)
);

CREATE INDEX IF NOT EXISTS idx_coa_user_id ON chart_of_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_coa_type ON chart_of_accounts(account_type);

-- Products and Services
CREATE TABLE IF NOT EXISTS products_services (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    cost DECIMAL(10, 2),
    sku VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX IF NOT EXISTS idx_ps_user_id ON products_services(user_id);
CREATE INDEX IF NOT EXISTS idx_ps_type ON products_services(type);

-- Plaid Bank Connections
CREATE TABLE IF NOT EXISTS plaid_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255),
    connected_at TIMESTAMP NOT NULL,
    last_synced_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_plaid_user_id ON plaid_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_plaid_active ON plaid_connections(active);

-- Reconciliation Run Results
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
CREATE INDEX IF NOT EXISTS idx_recon_status ON reconciliation_runs(status);

-- Apply triggers (function already exists globally)
CREATE TRIGGER update_onboarding_progress_updated_at BEFORE UPDATE ON onboarding_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chart_of_accounts_updated_at BEFORE UPDATE ON chart_of_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_services_updated_at BEFORE UPDATE ON products_services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plaid_connections_updated_at BEFORE UPDATE ON plaid_connections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

\echo 'Schema applied to client_testresort successfully.'

-- Reset search path
SET search_path TO public;
\echo 'All schemas applied successfully.'
