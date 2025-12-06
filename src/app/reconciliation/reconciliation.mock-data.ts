import {
  Payment,
  ReconciliationMatch,
  BankTransactionSummary,
} from './reconciliation.models';

const today = new Date();
const daysAgo = (n: number) => {
  const d = new Date();
  d.setDate(today.getDate() - n);
  return d.toISOString();
};

export const MOCK_PAYMENTS: Payment[] = [
  {
    id: 1,
    externalId: 'pi_001',
    amount: 178.5,
    currency: 'USD',
    paymentDate: daysAgo(1),
    method: 'Card',
    last4: '4242',
    guestName: 'Sarah Thompson',
    reservationId: 'RV-1245',
    reconciled: true,
  },
  {
    id: 2,
    externalId: 'pi_002',
    amount: 642.0,
    currency: 'USD',
    paymentDate: daysAgo(2),
    method: 'ACH',
    last4: null,
    guestName: 'Mark & Jenna Lewis',
    reservationId: 'CAB-87',
    reconciled: true,
  },
  {
    id: 3,
    externalId: 'pi_003',
    amount: 82.0,
    currency: 'USD',
    paymentDate: daysAgo(3),
    method: 'Card',
    last4: '1111',
    guestName: 'Daniel H.',
    reservationId: 'TENT-331',
    reconciled: false,
  },
];

const MOCK_BANK_TRANSACTIONS: BankTransactionSummary[] = [
  {
    id: 101,
    amount: 178.5,
    currency: 'USD',
    transactionDate: daysAgo(1),
    description: 'Visa Settlement · Sarah T.',
  },
  {
    id: 102,
    amount: 642.0,
    currency: 'USD',
    transactionDate: daysAgo(2),
    description: 'ACH Deposit · Lewis Family',
  },
];

export const MOCK_MATCHES: ReconciliationMatch[] = [
  {
    id: 1001,
    status: 'MATCHED',
    differenceAmount: 0,
    reason: 'Matched by date + amount',
    payment: MOCK_PAYMENTS[0],
    bankTransaction: MOCK_BANK_TRANSACTIONS[0],
    createdAt: daysAgo(0),
  },
  {
    id: 1002,
    status: 'MATCHED',
    differenceAmount: 0,
    reason: 'Matched by date + amount',
    payment: MOCK_PAYMENTS[1],
    bankTransaction: MOCK_BANK_TRANSACTIONS[1],
    createdAt: daysAgo(0),
  },
  {
    id: 1003,
    status: 'UNMATCHED_PAYMENT',
    differenceAmount: MOCK_PAYMENTS[2].amount,
    reason: 'No bank transaction found in date window',
    payment: MOCK_PAYMENTS[2],
    bankTransaction: null,
    createdAt: daysAgo(0),
  },
];
