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

-- Payments (deprecated - now handled through income table)
-- CREATE TABLE IF NOT EXISTS payments (
--     id BIGSERIAL PRIMARY KEY,
--     user_id VARCHAR(255) NOT NULL,
--     external_id VARCHAR(255) NOT NULL UNIQUE,
--     amount DECIMAL(10, 2) NOT NULL,
--     currency VARCHAR(3) NOT NULL DEFAULT 'USD',
--     payment_date TIMESTAMP NOT NULL,
--     method VARCHAR(50),
--     last4 VARCHAR(4),
--     guest_name VARCHAR(255),
--     reservation_id VARCHAR(100),
--     reconciled BOOLEAN DEFAULT FALSE,
--     source VARCHAR(50) DEFAULT 'rfbooks',
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
-- 
-- CREATE INDEX idx_payments_user_date ON payments(user_id, payment_date);
-- CREATE INDEX idx_payments_external_id ON payments(external_id);

-- Reconciliation Runs - stores summary of reconciliation state at each refresh
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

-- Manual Matches (deprecated - now handled through manual_match_income)
-- CREATE TABLE IF NOT EXISTS manual_matches (
--     id BIGSERIAL PRIMARY KEY,
--     user_id VARCHAR(255) NOT NULL,
--     payment_id VARCHAR(255) NOT NULL,
--     transaction_id VARCHAR(255) NOT NULL,
--     matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     matched_by VARCHAR(255),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     CONSTRAINT uk_manual_match_payment UNIQUE (user_id, payment_id),
--     CONSTRAINT uk_manual_match_transaction UNIQUE (user_id, transaction_id)
-- );
-- 
-- CREATE INDEX idx_manual_match_user_id ON manual_matches(user_id);
-- CREATE INDEX idx_manual_match_payment ON manual_matches(payment_id);
-- CREATE INDEX idx_manual_match_transaction ON manual_matches(transaction_id);

-- Manual Expense Matches (user-confirmed expense-to-transaction matches)
CREATE TABLE IF NOT EXISTS manual_match_expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expense_id BIGINT NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    matched_at TIMESTAMP NOT NULL,
    matched_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, expense_id),
    UNIQUE(user_id, transaction_id)
);

CREATE INDEX idx_manual_match_expense_user_id ON manual_match_expenses(user_id);
CREATE INDEX idx_manual_match_expense_expense_id ON manual_match_expenses(expense_id);
CREATE INDEX idx_manual_match_expense_transaction ON manual_match_expenses(transaction_id);

-- Manual Income Matches (user-confirmed income-to-transaction matches)
CREATE TABLE IF NOT EXISTS manual_match_income (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    income_id BIGINT NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    matched_at TIMESTAMP NOT NULL,
    matched_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, income_id),
    UNIQUE(user_id, transaction_id)
);

CREATE INDEX idx_manual_match_income_user_id ON manual_match_income(user_id);
CREATE INDEX idx_manual_match_income_income_id ON manual_match_income(income_id);
CREATE INDEX idx_manual_match_income_transaction ON manual_match_income(transaction_id);

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
    reconciled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expense_user ON expenses(user_id);
CREATE INDEX idx_expense_date ON expenses(expense_date);
CREATE INDEX idx_expense_vendor ON expenses(vendor_name);

-- Income
CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    income_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    source VARCHAR(255),
    category VARCHAR(100),
    account_id BIGINT,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    description TEXT,
    notes TEXT,
    reconciled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_income_user ON income(user_id);
CREATE INDEX idx_income_date ON income(income_date);
CREATE INDEX idx_income_source ON income(source);

-- ============================================
-- INSERT TEST DATA
-- ============================================

-- Insert test payments (deprecated - use income table instead)
-- INSERT INTO payments (user_id, external_id, amount, currency, payment_date, method, last4, guest_name, reservation_id, reconciled, source, created_at, updated_at)
-- VALUES
--   ('default-user', 'pi_001', 178.50, 'USD', NOW() - INTERVAL '1 day', 'Card', '4242', 'Sarah Thompson', 'RV-1245', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_002', 642.00, 'USD', NOW() - INTERVAL '2 days', 'ACH', NULL, 'Mark & Jenna Lewis', 'CAB-87', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_003', 82.00, 'USD', NOW() - INTERVAL '3 days', 'Card', '1111', 'Daniel H.', 'TENT-331', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_004', 245.75, 'USD', NOW() - INTERVAL '4 days', 'Card', '5678', 'Jessica Martinez', 'CAB-92', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_005', 156.00, 'USD', NOW() - INTERVAL '5 days', 'ACH', NULL, 'Robert Chen', 'RV-1250', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_006', 320.00, 'USD', NOW() - INTERVAL '6 days', 'Card', '9012', 'Emily & James Wilson', 'TENT-335', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_007', 189.00, 'USD', NOW() - INTERVAL '7 days', 'Card', '3456', 'Michael Johnson', 'RV-1255', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_008', 95.50, 'USD', NOW() - INTERVAL '8 days', 'Card', '7890', 'Amanda Brooks', 'TENT-340', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_009', 412.00, 'USD', NOW() - INTERVAL '9 days', 'ACH', NULL, 'David & Lisa Parker', 'CAB-95', false, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_010', 275.25, 'USD', NOW() - INTERVAL '10 days', 'Card', '2468', 'Christopher Lee', 'RV-1260', false, 'rfbooks', NOW(), NOW())
-- ON CONFLICT (external_id) DO NOTHING;

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

-- Insert test expenses for P&L report
INSERT INTO expenses (user_id, expense_date, amount, vendor_name, category, payment_method, account_id, reference_number, notes, created_at, updated_at)
VALUES
  -- Food Costs
  ('default-user', CURRENT_DATE - 1, 1200.00, 'Sysco', 'Food Costs', 'ACH', NULL, 'INV-12345', 'Fresh Produce', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 2, 2500.00, 'US Foods', 'Food Costs', 'ACH', NULL, 'INV-12346', 'Meat & Seafood', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 800.00, 'Local Farm', 'Food Costs', 'Check', NULL, 'CHK-1001', 'Dairy Products', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 1850.00, 'Restaurant Depot', 'Food Costs', 'Card', NULL, NULL, 'Bulk Food Items', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 10, 1650.00, 'Costco Business', 'Food Costs', 'Card', NULL, NULL, 'Dry Goods', NOW(), NOW()),
  
  -- Beverage Costs
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Wine Co', 'Beverage Costs', 'ACH', NULL, 'INV-W123', 'Wine Selection', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 1500.00, 'Beer Depot', 'Beverage Costs', 'Card', NULL, NULL, 'Craft Beer', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 6, 1500.00, 'Liquor Warehouse', 'Beverage Costs', 'ACH', NULL, 'INV-L456', 'Spirits', NOW(), NOW()),
  
  -- Activity Supplies
  ('default-user', CURRENT_DATE - 2, 800.00, 'Outdoor Supply', 'Activity Supplies', 'Card', NULL, NULL, 'Kayak Equipment', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 4, 600.00, 'REI', 'Activity Supplies', 'Card', NULL, NULL, 'Hiking Gear', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 600.00, 'Bike Shop', 'Activity Supplies', 'Card', NULL, NULL, 'Bike Maintenance', NOW(), NOW()),
  
  -- Payroll & Benefits
  ('default-user', CURRENT_DATE - 1, 15000.00, 'Payroll', 'Payroll', 'ACH', NULL, 'PAY-001', 'Staff Salaries', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 3000.00, 'Health Insurance Co', 'Benefits', 'ACH', NULL, 'INV-H789', 'Employee Health Insurance', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Tax Authority', 'Payroll Taxes', 'ACH', NULL, 'TAX-001', 'Payroll Tax Payment', NOW(), NOW()),
  
  -- Marketing & Advertising
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Google', 'Marketing', 'Card', NULL, NULL, 'Google Ads Campaign', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 1500.00, 'Meta', 'Marketing', 'Card', NULL, NULL, 'Facebook & Instagram Ads', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 1500.00, 'Local Magazine', 'Marketing', 'Check', NULL, 'CHK-1002', 'Print Advertising', NOW(), NOW()),
  
  -- Utilities
  ('default-user', CURRENT_DATE - 1, 2000.00, 'Power Company', 'Utilities', 'ACH', NULL, 'ELEC-123', 'Electric Bill', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 1200.00, 'Water Utility', 'Utilities', 'ACH', NULL, 'WATER-456', 'Water & Sewer', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 800.00, 'Gas Company', 'Utilities', 'ACH', NULL, 'GAS-789', 'Natural Gas', NOW(), NOW()),
  
  -- Maintenance & Repairs
  ('default-user', CURRENT_DATE - 2, 2500.00, 'HVAC Pro', 'Maintenance', 'Card', NULL, NULL, 'HVAC Repair', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 4, 1800.00, 'Plumber Inc', 'Maintenance', 'Card', NULL, NULL, 'Plumbing Service', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 6, 1700.00, 'Green Thumb', 'Maintenance', 'Card', NULL, NULL, 'Landscaping', NOW(), NOW()),
  
  -- Insurance
  ('default-user', CURRENT_DATE - 1, 2000.00, 'State Farm', 'Insurance', 'ACH', NULL, 'POL-12345', 'Property Insurance', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 1000.00, 'Allstate', 'Insurance', 'ACH', NULL, 'POL-67890', 'Liability Insurance', NOW(), NOW()),
  
  -- Office Supplies
  ('default-user', CURRENT_DATE - 2, 800.00, 'Office Depot', 'Office Supplies', 'Card', NULL, NULL, 'Office Supply Order', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 600.00, 'Staples', 'Office Supplies', 'Card', NULL, NULL, 'Printer Supplies', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 600.00, 'Amazon', 'Office Supplies', 'Card', NULL, NULL, 'Paper Products', NOW(), NOW()),
  
  -- Professional Services
  ('default-user', CURRENT_DATE - 1, 1500.00, 'CPA Firm', 'Professional Services', 'Check', NULL, 'CHK-1003', 'Accounting Services', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 1500.00, 'Law Office', 'Professional Services', 'Check', NULL, 'CHK-1004', 'Legal Consultation', NOW(), NOW()),
  
  -- Other Expenses
  ('default-user', CURRENT_DATE - 3, 500.00, 'Bank', 'Bank Fees', 'ACH', NULL, NULL, 'Monthly Bank Fees', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 800.00, 'Software Vendors', 'Software', 'Card', NULL, NULL, 'Software Subscriptions', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 700.00, 'Various Vendors', 'Miscellaneous', 'Card', NULL, NULL, 'Misc Expenses', NOW(), NOW());

-- Add more payment data (deprecated - use income table)
-- INSERT INTO payments (user_id, external_id, amount, currency, payment_date, method, last4, guest_name, reservation_id, reconciled, source, created_at, updated_at)
-- VALUES
--   ('default-user', 'pi_room_001', 1500.00, 'USD', CURRENT_DATE - 1, 'Card', '4242', 'Smith Family', 'ROOM-101', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_room_002', 2000.00, 'USD', CURRENT_DATE - 2, 'Card', '5555', 'Johnson Family', 'ROOM-203', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_room_003', 1800.00, 'USD', CURRENT_DATE - 3, 'ACH', NULL, 'Williams Family', 'ROOM-105', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_room_004', 2500.00, 'USD', CURRENT_DATE - 5, 'Card', '6789', 'Brown Family', 'CABIN-5', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_room_005', 3200.00, 'USD', CURRENT_DATE - 7, 'Card', '1234', 'Davis Family', 'SUITE-301', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_fb_001', 3500.00, 'USD', CURRENT_DATE - 1, 'Card', '9876', 'Restaurant Sales', 'REST-DAILY', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_fb_002', 1800.00, 'USD', CURRENT_DATE - 2, 'Card', '5432', 'Bar Sales', 'BAR-DAILY', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_fb_003', 5000.00, 'USD', CURRENT_DATE - 3, 'ACH', NULL, 'Catering Event', 'EVENT-001', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_fb_004', 2200.00, 'USD', CURRENT_DATE - 5, 'Card', '1111', 'Room Service', 'RS-DAILY', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_act_001', 800.00, 'USD', CURRENT_DATE - 2, 'Card', '2222', 'Guided Hiking Tour', 'ACT-HIKE', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_act_002', 1200.00, 'USD', CURRENT_DATE - 3, 'Card', '3333', 'Kayak Rentals', 'ACT-KAYAK', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_act_003', 950.00, 'USD', CURRENT_DATE - 4, 'Card', '4444', 'Mountain Biking', 'ACT-BIKE', true, 'rfbooks', NOW(), NOW()),
--   ('default-user', 'pi_act_004', 2500.00, 'USD', CURRENT_DATE - 6, 'Card', '5555', 'Spa Treatments', 'SPA-DAILY', true, 'rfbooks', NOW(), NOW())
-- ON CONFLICT (external_id) DO NOTHING;

-- Insert test income records
INSERT INTO income (user_id, income_date, amount, source, category, payment_method, account_id, reference_number, description, notes, reconciled, created_at, updated_at)
VALUES
  -- Room Revenue
  ('default-user', CURRENT_DATE - 1, 1500.00, 'Smith Family', 'room_revenue', 'Card', NULL, 'ROOM-101', 'Cabin Rental - 3 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 2, 2000.00, 'Johnson Family', 'room_revenue', 'Card', NULL, 'ROOM-203', 'Suite Rental - 4 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 1800.00, 'Williams Family', 'room_revenue', 'ACH', NULL, 'ROOM-105', 'RV Site Rental - 7 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 2500.00, 'Brown Family', 'room_revenue', 'Card', NULL, 'CABIN-5', 'Premium Cabin - 5 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 3200.00, 'Davis Family', 'room_revenue', 'Card', NULL, 'SUITE-301', 'Executive Suite - 5 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 8, 1200.00, 'Miller Family', 'room_revenue', 'Card', NULL, 'TENT-12', 'Tent Site - 5 nights', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 10, 1750.00, 'Anderson Family', 'room_revenue', 'ACH', NULL, 'RV-45', 'RV Full Hookup - 6 nights', NULL, false, NOW(), NOW()),
  
  -- Food & Beverage
  ('default-user', CURRENT_DATE - 1, 3500.00, 'Restaurant Sales', 'food_beverage', 'Card', NULL, 'REST-DAILY', 'Daily Restaurant Revenue', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 2, 1800.00, 'Bar Sales', 'food_beverage', 'Card', NULL, 'BAR-DAILY', 'Daily Bar Revenue', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 5000.00, 'Corporate Event', 'food_beverage', 'ACH', NULL, 'EVENT-001', 'Catering - Corporate Retreat', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 2200.00, 'Room Service', 'food_beverage', 'Card', NULL, 'RS-DAILY', 'Daily Room Service Revenue', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 6, 1500.00, 'Coffee Shop', 'food_beverage', 'Card', NULL, 'COFFEE-DAILY', 'Coffee Shop Sales', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 8, 2800.00, 'Wedding Reception', 'food_beverage', 'Check', NULL, 'EVENT-002', 'Wedding Catering Service', NULL, false, NOW(), NOW()),
  
  -- Activities & Recreation
  ('default-user', CURRENT_DATE - 2, 800.00, 'Tour Group', 'activities', 'Card', NULL, 'ACT-HIKE', 'Guided Hiking Tours', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 1200.00, 'Equipment Rentals', 'activities', 'Card', NULL, 'ACT-KAYAK', 'Kayak Rentals', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 4, 950.00, 'Bike Tours', 'activities', 'Card', NULL, 'ACT-BIKE', 'Mountain Biking Tours', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 6, 2500.00, 'Spa Services', 'activities', 'Card', NULL, 'SPA-DAILY', 'Spa Treatments & Massages', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 650.00, 'Fishing Charters', 'activities', 'Cash', NULL, 'FISH-001', 'Fishing Guide Services', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 9, 1100.00, 'Horseback Riding', 'activities', 'Card', NULL, 'HORSE-001', 'Trail Riding Tours', NULL, false, NOW(), NOW()),
  
  -- Merchandise Sales
  ('default-user', CURRENT_DATE - 1, 450.00, 'Gift Shop', 'merchandise', 'Card', NULL, 'SHOP-DAILY', 'Gift Shop Sales', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 325.00, 'Clothing Sales', 'merchandise', 'Card', NULL, 'MERCH-001', 'Resort Branded Apparel', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 280.00, 'Sundries', 'merchandise', 'Cash', NULL, 'SHOP-002', 'Convenience Items', NULL, false, NOW(), NOW()),
  
  -- Equipment Rentals
  ('default-user', CURRENT_DATE - 2, 380.00, 'Bike Rentals', 'rentals', 'Card', NULL, 'RENT-BIKE', 'Mountain Bike Rentals', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 4, 520.00, 'Water Equipment', 'rentals', 'Card', NULL, 'RENT-WATER', 'Paddleboard & Kayak Rentals', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 6, 290.00, 'Camping Gear', 'rentals', 'Card', NULL, 'RENT-CAMP', 'Tent & Camping Equipment', NULL, false, NOW(), NOW()),
  
  -- Parking & Fees
  ('default-user', CURRENT_DATE - 1, 175.00, 'Parking Fees', 'parking', 'Cash', NULL, 'PARK-DAILY', 'Daily Parking Revenue', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 3, 125.00, 'Late Checkout', 'late_fees', 'Card', NULL, 'LATE-001', 'Late Checkout Fees', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 5, 200.00, 'Cancellation', 'cancellation_fees', 'Card', NULL, 'CANCEL-001', 'Cancellation Fee - Non-refundable', NULL, false, NOW(), NOW()),
  
  -- Deposits
  ('default-user', CURRENT_DATE - 2, 1500.00, 'Future Booking', 'deposits', 'ACH', NULL, 'DEP-001', 'Deposit for Future Reservation', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 2000.00, 'Event Deposit', 'deposits', 'Check', NULL, 'DEP-002', 'Wedding Event Deposit', NULL, false, NOW(), NOW()),
  
  -- Other Revenue
  ('default-user', CURRENT_DATE - 4, 350.00, 'Pet Fees', 'other_revenue', 'Card', NULL, 'PET-001', 'Pet Stay Fees', NULL, false, NOW(), NOW()),
  ('default-user', CURRENT_DATE - 8, 425.00, 'Laundry Service', 'other_revenue', 'Card', NULL, 'LAUNDRY-001', 'Guest Laundry Services', NULL, false, NOW(), NOW());

\echo 'RF Books database initialized successfully with test data!'
