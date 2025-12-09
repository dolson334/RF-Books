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

export type ReconciliationStatus =
  | 'MATCHED'
  | 'MULTIPLE_MATCHES'
  | 'UNMATCHED_PAYMENT'
  | 'UNMATCHED_BANK_TRANSACTION';

export interface BankTransactionSummary {
  id: number;
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
