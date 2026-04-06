export interface FinancialSummary {
  totalIncome: number;
  totalExpenses: number;
  netIncome: number;
  incomeCount: number;
  expenseCount: number;
  reconciledCount: number;
  unreconciledCount: number;
  reconciliationRate: number;
  period: string;
  startDate: string;
  endDate: string;
}

export interface CategoryBreakdown {
  category: string;
  total: number;
  count: number;
  percentage: number;
}

export interface MonthlyTrend {
  month: string;
  income: number;
  expenses: number;
  net: number;
}
