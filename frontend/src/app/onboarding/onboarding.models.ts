export interface ChartOfAccount {
  id?: number;
  accountNumber: string;
  accountName: string;
  accountType: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
  description?: string;
}

export interface ProductServiceItem {
  id?: number;
  name: string;
  type: 'PRODUCT' | 'SERVICE';
  defaultPrice?: number;
  unitOfMeasure?: string;
  description?: string;
  revenueAccountId?: number;
}

export interface OnboardingProgress {
  bankConnected: boolean;
  chartOfAccountsCreated: boolean;
  productsServicesCreated: boolean;
  completed: boolean;
}

export const DEFAULT_CHART_OF_ACCOUNTS: ChartOfAccount[] = [
  { accountNumber: '1000', accountName: 'Cash', accountType: 'ASSET', description: 'Checking and savings accounts' },
  { accountNumber: '1100', accountName: 'Accounts Receivable', accountType: 'ASSET', description: 'Money owed by guests and clients' },
  { accountNumber: '1200', accountName: 'Inventory', accountType: 'ASSET', description: 'Food, beverage, and supply inventory' },
  { accountNumber: '1500', accountName: 'Fixed Assets', accountType: 'ASSET', description: 'Property, buildings, equipment' },
  { accountNumber: '2000', accountName: 'Accounts Payable', accountType: 'LIABILITY', description: 'Amounts owed to vendors' },
  { accountNumber: '2100', accountName: 'Credit Card Payable', accountType: 'LIABILITY', description: 'Outstanding credit card balances' },
  { accountNumber: '2200', accountName: 'Payroll Liabilities', accountType: 'LIABILITY', description: 'Wages, taxes, and benefits owed' },
  { accountNumber: '2500', accountName: 'Loan Payable', accountType: 'LIABILITY', description: 'Business loans and mortgages' },
  { accountNumber: '3000', accountName: 'Owner Equity', accountType: 'EQUITY', description: 'Owner investment and retained earnings' },
  { accountNumber: '3100', accountName: 'Retained Earnings', accountType: 'EQUITY', description: 'Accumulated net income' },
  { accountNumber: '4000', accountName: 'Room Revenue', accountType: 'REVENUE', description: 'Cabin, suite, and RV site rentals' },
  { accountNumber: '4100', accountName: 'Food & Beverage Revenue', accountType: 'REVENUE', description: 'Restaurant and bar sales' },
  { accountNumber: '4200', accountName: 'Event Revenue', accountType: 'REVENUE', description: 'Catering and event income' },
  { accountNumber: '4300', accountName: 'Other Revenue', accountType: 'REVENUE', description: 'Gift shop, activities, misc income' },
  { accountNumber: '5000', accountName: 'Food Costs', accountType: 'EXPENSE', description: 'Cost of food purchased' },
  { accountNumber: '5100', accountName: 'Beverage Costs', accountType: 'EXPENSE', description: 'Cost of beverages purchased' },
  { accountNumber: '5200', accountName: 'Payroll', accountType: 'EXPENSE', description: 'Wages, salaries, and benefits' },
  { accountNumber: '5300', accountName: 'Utilities', accountType: 'EXPENSE', description: 'Electric, water, gas, internet' },
  { accountNumber: '5400', accountName: 'Maintenance', accountType: 'EXPENSE', description: 'Repairs and property maintenance' },
  { accountNumber: '5500', accountName: 'Marketing', accountType: 'EXPENSE', description: 'Advertising and promotion' },
];
