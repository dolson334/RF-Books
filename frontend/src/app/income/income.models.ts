export interface Income {
  id?: number;
  incomeDate: string;
  amount: number;
  source?: string;
  category?: string;
  accountId?: number;
  paymentMethod?: string;
  referenceNumber?: string;
  description?: string;
  notes?: string;
  reconciled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface IncomeCategory {
  value: string;
  label: string;
}

export const INCOME_CATEGORIES: IncomeCategory[] = [
  { value: 'room_revenue', label: 'Room Revenue' },
  { value: 'food_beverage', label: 'Food & Beverage' },
  { value: 'activities', label: 'Activities & Recreation' },
  { value: 'merchandise', label: 'Merchandise Sales' },
  { value: 'rentals', label: 'Equipment Rentals' },
  { value: 'parking', label: 'Parking Fees' },
  { value: 'late_fees', label: 'Late Fees' },
  { value: 'cancellation_fees', label: 'Cancellation Fees' },
  { value: 'deposits', label: 'Deposits' },
  { value: 'other_revenue', label: 'Other Revenue' }
];

export const PAYMENT_METHODS = [
  { value: 'cash', label: 'Cash' },
  { value: 'check', label: 'Check' },
  { value: 'card', label: 'Credit Card' },
  { value: 'ach', label: 'ACH/Bank Transfer' },
  { value: 'other', label: 'Other' }
];
