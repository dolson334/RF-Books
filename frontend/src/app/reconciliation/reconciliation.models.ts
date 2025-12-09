export type CurrencyCode = string;
export type DataSource = 'rfbooks' | 'plaid';

export interface Payment {
  id: number;
  externalId: string;
  amount: number;
  currency: CurrencyCode;
  paymentDate: string;
  method?: string | null;
  last4?: string | null;
  guestName?: string | null;
  reservationId?: string | null;
  reconciled: boolean;
  source: DataSource;
}

export interface Expense {
  id: number;
  expenseDate: string;
  amount: number;
  vendorName?: string;
  category?: string;
  paymentMethod?: string;
  referenceNumber?: string;
  description?: string;
  reconciled: boolean;
}

export interface Income {
  id: number;
  incomeDate: string;
  amount: number;
  source?: string;
  category?: string;
  paymentMethod?: string;
  referenceNumber?: string;
  description?: string;
  reconciled: boolean;
}

export type ReconciliationStatus =
  | 'MATCHED'
  | 'MANUAL_MATCH'
  | 'MULTIPLE_MATCHES'
  | 'UNMATCHED_PAYMENT'
  | 'UNMATCHED_BANK_TRANSACTION';

export interface BankTransactionSummary {
  id: number;
  transactionId?: string;
  amount: number;
  currency: CurrencyCode;
  transactionDate: string;
  description?: string | null;
  source: DataSource;
}

export interface PlaidTransaction {
  transactionId: string;
  accountId: string;
  amount: number;
  date: string;
  name: string;
  merchantName?: string;
  category?: string[];
  pending: boolean;
}

export interface ReconciliationMatch {
  id: number;
  status: ReconciliationStatus;
  differenceAmount: number;
  reason?: string | null;
  payment?: Payment | null;
  bankTransaction?: BankTransactionSummary | null;
  createdAt: string;
}

export interface ReconciliationSummary {
  id: number;
  runAt: string;
  startDate: string;
  endDate: string;
  matchedCount: number;
  unmatchedPaymentCount: number;
  unmatchedBankCount: number;
  totalPayments: number;
  totalBankTransactions: number;
  status: 'COMPLETED' | 'FAILED' | 'NO_BANK_CONNECTION';
  errorMessage?: string;
  hasIssues: boolean;
}
