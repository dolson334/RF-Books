-- RF Books Test Data
-- Inserted after schema creation for local development only.
-- Mounted into Docker's /docker-entrypoint-initdb.d/ as 02-rfbooks-test-data.sql

SET search_path TO testresort, public;

-- ============================================
-- PLAID TRANSACTIONS (bank-side)
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

-- ============================================
-- EXPENSES
-- ============================================

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

-- ============================================
-- INCOME
-- ============================================

INSERT INTO income (user_id, income_date, amount, source, category, payment_method, reference_number, description)
VALUES
  ('default-user', CURRENT_DATE - 1, 1500.00, 'Smith Family', 'Room Revenue', 'Card', 'ROOM-101', 'Cabin Rental - 3 nights'),
  ('default-user', CURRENT_DATE - 1, 3500.00, 'Restaurant Sales', 'Food & Beverage', 'Card', 'REST-DAILY', 'Daily Restaurant Revenue'),
  ('default-user', CURRENT_DATE - 2, 2000.00, 'Johnson Family', 'Room Revenue', 'Card', 'ROOM-203', 'Suite Rental - 4 nights'),
  ('default-user', CURRENT_DATE - 3, 1800.00, 'Williams Family', 'Room Revenue', 'ACH', 'ROOM-105', 'RV Site Rental'),
  ('default-user', CURRENT_DATE - 3, 5000.00, 'Corporate Event', 'Food & Beverage', 'ACH', 'EVENT-001', 'Catering - Corporate Retreat'),
  ('default-user', CURRENT_DATE - 5, 2500.00, 'Brown Family', 'Room Revenue', 'Card', 'CABIN-5', 'Premium Cabin - 5 nights'),
  ('default-user', CURRENT_DATE - 7, 3200.00, 'Davis Family', 'Room Revenue', 'Card', 'SUITE-301', 'Executive Suite - 5 nights');
