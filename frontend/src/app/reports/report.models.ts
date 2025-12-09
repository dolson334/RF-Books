export interface ReportSummary {
  totalIncome: number;
  totalExpenses: number;
  netIncome: number;
  reconciliationRate: number;
  period: string;
}

export interface CategoryBreakdown {
  category: string;
  amount: number;
  percentage: number;
  count: number;
}

export interface TrendData {
  date: string;
  income: number;
  expenses: number;
  net: number;
}

export interface VendorSpending {
  vendorName: string;
  totalAmount: number;
  transactionCount: number;
}

export interface MonthlyComparison {
  month: string;
  income: number;
  expenses: number;
  net: number;
}

export interface PaymentMethodBreakdown {
  method: string;
  amount: number;
  percentage: number;
}

export interface FinancialReport {
  summary: ReportSummary;
  expensesByCategory: CategoryBreakdown[];
  incomeBySource: CategoryBreakdown[];
  trends: TrendData[];
  topVendors: VendorSpending[];
  monthlyComparison: MonthlyComparison[];
  paymentMethods: PaymentMethodBreakdown[];
}

export type ReportPeriod = 'week' | 'month' | 'quarter' | 'year' | 'custom';

export interface ReportFilter {
  period: ReportPeriod;
  startDate?: string;
  endDate?: string;
}
