export type CurrencyCode = string;

export interface Payment {
  id: number;
  externalId: string;
  amount: number;
  currency: CurrencyCode;
  paymentDate: string; // ISO string
  method?: string | null;
  last4?: string | null;
  guestName?: string | null;
  reservationId?: string | null;
  reconciled: boolean;
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
