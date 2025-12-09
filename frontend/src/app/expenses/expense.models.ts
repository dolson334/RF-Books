export interface Expense {
  id?: number;
  expenseDate: string;
  amount: number;
  vendorName?: string;
  category?: string;
  accountId?: number;
  paymentMethod?: string;
  referenceNumber?: string;
  description?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExpenseCategory {
  value: string;
  label: string;
}

export const EXPENSE_CATEGORIES: ExpenseCategory[] = [
  { value: 'utilities', label: 'Utilities' },
  { value: 'maintenance', label: 'Maintenance & Repairs' },
  { value: 'supplies', label: 'Supplies' },
  { value: 'food_beverage', label: 'Food & Beverage' },
  { value: 'marketing', label: 'Marketing & Advertising' },
  { value: 'insurance', label: 'Insurance' },
  { value: 'payroll', label: 'Payroll' },
  { value: 'taxes', label: 'Taxes' },
  { value: 'rent', label: 'Rent' },
  { value: 'professional_services', label: 'Professional Services' },
  { value: 'other', label: 'Other' }
];

export const PAYMENT_METHODS = [
  { value: 'cash', label: 'Cash' },
  { value: 'check', label: 'Check' },
  { value: 'card', label: 'Credit Card' },
  { value: 'ach', label: 'ACH/Bank Transfer' },
  { value: 'other', label: 'Other' }
];
