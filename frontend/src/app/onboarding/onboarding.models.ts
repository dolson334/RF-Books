export interface ChartOfAccount {
  id?: number;
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  description?: string;
}

export type AccountType = 
  | 'ASSET' 
  | 'LIABILITY' 
  | 'EQUITY' 
  | 'REVENUE' 
  | 'EXPENSE';

export interface ProductService {
  id?: number;
  name: string;
  description?: string;
  type: 'PRODUCT' | 'SERVICE';
  defaultPrice?: number;
  unitOfMeasure?: string;
  revenueAccountId?: number;
}

export interface OnboardingProgress {
  bankConnected: boolean;
  chartOfAccountsCreated: boolean;
  productsServicesCreated: boolean;
  completed: boolean;
}
