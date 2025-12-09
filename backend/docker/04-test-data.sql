-- Test data for reconciliation
SET search_path TO testresort;

-- Insert test payments
INSERT INTO payments (user_id, external_id, amount, currency, payment_date, method, last4, guest_name, reservation_id, reconciled, source, created_at, updated_at)
VALUES
  (1, 'pi_001', 178.50, 'USD', NOW() - INTERVAL '1 day', 'Card', '4242', 'Sarah Thompson', 'RV-1245', true, 'rfbooks', NOW(), NOW()),
  (1, 'pi_002', 642.00, 'USD', NOW() - INTERVAL '2 days', 'ACH', NULL, 'Mark & Jenna Lewis', 'CAB-87', true, 'rfbooks', NOW(), NOW()),
  (1, 'pi_003', 82.00, 'USD', NOW() - INTERVAL '3 days', 'Card', '1111', 'Daniel H.', 'TENT-331', false, 'rfbooks', NOW(), NOW()),
  (1, 'pi_004', 245.75, 'USD', NOW() - INTERVAL '4 days', 'Card', '5678', 'Jessica Martinez', 'CAB-92', false, 'rfbooks', NOW(), NOW()),
  (1, 'pi_005', 156.00, 'USD', NOW() - INTERVAL '5 days', 'ACH', NULL, 'Robert Chen', 'RV-1250', false, 'rfbooks', NOW(), NOW()),
  (1, 'pi_006', 320.00, 'USD', NOW() - INTERVAL '6 days', 'Card', '9012', 'Emily & James Wilson', 'TENT-335', false, 'rfbooks', NOW(), NOW())
ON CONFLICT (external_id) DO NOTHING;

-- Insert test Plaid transactions
INSERT INTO plaid_transactions (user_id, transaction_id, account_id, amount, date, name, merchant_name, pending, created_at)
VALUES
  (1, 'tx_001', 'acc_test', 178.50, (NOW() - INTERVAL '1 day')::date, 'Visa Settlement · Sarah T.', 'Sarah Thompson', false, NOW()),
  (1, 'tx_002', 'acc_test', 642.00, (NOW() - INTERVAL '2 days')::date, 'ACH Deposit · Lewis Family', 'Lewis Family', false, NOW()),
  (1, 'tx_003', 'acc_test', 245.75, (NOW() - INTERVAL '4 days')::date, 'Online Payment Receipt', 'Online Payment', false, NOW()),
  (1, 'tx_004', 'acc_test', 156.00, (NOW() - INTERVAL '5 days')::date, 'ACH Transfer - Chen', 'Robert Chen', false, NOW()),
  (1, 'tx_005', 'acc_test', 320.00, (NOW() - INTERVAL '6 days')::date, 'Card Payment - Wilson', 'Wilson Family', false, NOW()),
  (1, 'tx_006', 'acc_test', 99.50, (NOW() - INTERVAL '7 days')::date, 'Mystery Transaction', 'Unknown', false, NOW())
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
  ('default-user', CURRENT_DATE - 1, 15000.00, NULL, 'Payroll', 'ACH', NULL, 'PAY-001', 'Staff Salaries', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 3000.00, 'Health Insurance Co', 'Benefits', 'ACH', NULL, 'INV-H789', 'Employee Health Insurance', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 1, 2000.00, NULL, 'Payroll Taxes', 'ACH', NULL, 'TAX-001', 'Payroll Tax Payment', NOW(), NOW()),
  
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
  ('default-user', CURRENT_DATE - 5, 800.00, 'Various', 'Software', 'Card', NULL, NULL, 'Software Subscriptions', NOW(), NOW()),
  ('default-user', CURRENT_DATE - 7, 700.00, 'Various', 'Miscellaneous', 'Card', NULL, NULL, 'Misc Expenses', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Add more payment data for income tracking
INSERT INTO payments (user_id, external_id, amount, currency, payment_date, method, last4, guest_name, reservation_id, reconciled, source, created_at, updated_at)
VALUES
  ('default-user', 'pi_room_001', 1500.00, 'USD', CURRENT_DATE - 1, 'Card', '4242', 'Smith Family', 'ROOM-101', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_room_002', 2000.00, 'USD', CURRENT_DATE - 2, 'Card', '5555', 'Johnson Family', 'ROOM-203', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_room_003', 1800.00, 'USD', CURRENT_DATE - 3, 'ACH', NULL, 'Williams Family', 'ROOM-105', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_room_004', 2500.00, 'USD', CURRENT_DATE - 5, 'Card', '6789', 'Brown Family', 'CABIN-5', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_room_005', 3200.00, 'USD', CURRENT_DATE - 7, 'Card', '1234', 'Davis Family', 'SUITE-301', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_fb_001', 3500.00, 'USD', CURRENT_DATE - 1, 'Card', '9876', 'Restaurant Sales', 'REST-DAILY', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_fb_002', 1800.00, 'USD', CURRENT_DATE - 2, 'Card', '5432', 'Bar Sales', 'BAR-DAILY', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_fb_003', 5000.00, 'USD', CURRENT_DATE - 3, 'ACH', NULL, 'Catering Event', 'EVENT-001', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_fb_004', 2200.00, 'USD', CURRENT_DATE - 5, 'Card', '1111', 'Room Service', 'RS-DAILY', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_act_001', 800.00, 'USD', CURRENT_DATE - 2, 'Card', '2222', 'Guided Hiking Tour', 'ACT-HIKE', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_act_002', 1200.00, 'USD', CURRENT_DATE - 3, 'Card', '3333', 'Kayak Rentals', 'ACT-KAYAK', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_act_003', 950.00, 'USD', CURRENT_DATE - 4, 'Card', '4444', 'Mountain Biking', 'ACT-BIKE', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_act_004', 2500.00, 'USD', CURRENT_DATE - 6, 'Card', '5555', 'Spa Treatments', 'SPA-DAILY', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_other_001', 1200.00, 'USD', CURRENT_DATE - 1, 'Card', '6666', 'Gift Shop Sales', 'GIFT-DAILY', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_other_002', 500.00, 'USD', CURRENT_DATE - 4, 'Card', '7777', 'Pet Fees', 'PET-FEES', true, 'rfbooks', NOW(), NOW()),
  ('default-user', 'pi_other_003', 300.00, 'USD', CURRENT_DATE - 7, 'Card', '8888', 'Late Checkout Fees', 'LATE-FEES', true, 'rfbooks', NOW(), NOW())
ON CONFLICT (external_id) DO NOTHING;
