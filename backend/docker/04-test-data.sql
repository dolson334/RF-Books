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
