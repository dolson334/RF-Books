import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ReportService } from './report.service';
import {
  FinancialSummary,
  CategoryBreakdown,
  MonthlyTrend,
} from './report.models';
import { INCOME_CATEGORIES } from '../income/income.models';
import { EXPENSE_CATEGORIES } from '../expenses/expense.models';

@Component({
  selector: 'rf-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss'],
})
export class ReportsComponent implements OnInit {
  summary = signal<FinancialSummary | null>(null);
  incomeCategories = signal<CategoryBreakdown[]>([]);
  expenseCategories = signal<CategoryBreakdown[]>([]);
  monthlyTrend = signal<MonthlyTrend[]>([]);
  isLoading = signal<boolean>(true);

  // Period selection
  periodType = signal<'MONTH' | 'QUARTER' | 'YEAR'>('MONTH');
  selectedDate = signal<string>(this.currentMonthDate());

  // Drill-down
  drillCategory = signal<string | null>(null);
  drillType = signal<'income' | 'expense'>('income');
  drillItems = signal<any[]>([]);
  isDrillLoading = signal<boolean>(false);

  readonly netPositive = computed(() => {
    const s = this.summary();
    return s ? s.netIncome >= 0 : true;
  });

  readonly maxTrendValue = computed(() => {
    const trends = this.monthlyTrend();
    if (trends.length === 0) return 1;
    return Math.max(...trends.map(t => Math.max(t.income, t.expenses)), 1);
  });

  constructor(
    private reportService: ReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReport();
  }

  async loadReport(): Promise<void> {
    this.isLoading.set(true);
    this.drillCategory.set(null);
    try {
      const period = this.periodType();
      const date = this.selectedDate();

      const range = this.getDateRange(period, date);

      const [summary, incCat, expCat, trend] = await Promise.all([
        firstValueFrom(this.reportService.getSummary(period, date)),
        firstValueFrom(this.reportService.getIncomeByCategory(range.start, range.end)),
        firstValueFrom(this.reportService.getExpensesByCategory(range.start, range.end)),
        firstValueFrom(this.reportService.getMonthlyTrend(12)),
      ]);

      this.summary.set(summary);
      this.incomeCategories.set(incCat);
      this.expenseCategories.set(expCat);
      this.monthlyTrend.set(trend);
    } finally {
      this.isLoading.set(false);
    }
  }

  setPeriod(type: 'MONTH' | 'QUARTER' | 'YEAR'): void {
    this.periodType.set(type);
    this.loadReport();
  }

  onDateChange(value: string): void {
    this.selectedDate.set(value + '-01');
    this.loadReport();
  }

  async drillDown(category: string, type: 'income' | 'expense'): Promise<void> {
    if (this.drillCategory() === category && this.drillType() === type) {
      this.drillCategory.set(null);
      return;
    }
    this.drillType.set(type);
    this.drillCategory.set(category);
    this.isDrillLoading.set(true);
    try {
      const range = this.getDateRange(this.periodType(), this.selectedDate());
      const url = type === 'income' ? '/api/income' : '/api/expenses';
      const resp = await fetch(
        `${url}?resortAlias=&startDate=${range.start}&endDate=${range.end}&category=${encodeURIComponent(category)}`
      );
      const items = await resp.json();
      this.drillItems.set(items);
    } finally {
      this.isDrillLoading.set(false);
    }
  }

  exportCsv(): void {
    const summary = this.summary();
    if (!summary) return;

    const lines: string[] = [];
    lines.push(`RF Books - P&L Report,${summary.period}`);
    lines.push(`Period,${summary.startDate} to ${summary.endDate}`);
    lines.push('');
    lines.push('INCOME');
    lines.push('Category,Amount,Count,Percentage');
    for (const c of this.incomeCategories()) {
      lines.push(`${c.category},${c.total},${c.count},${c.percentage}%`);
    }
    lines.push(`Total Income,${summary.totalIncome},,`);
    lines.push('');
    lines.push('EXPENSES');
    lines.push('Category,Amount,Count,Percentage');
    for (const c of this.expenseCategories()) {
      lines.push(`${c.category},${c.total},${c.count},${c.percentage}%`);
    }
    lines.push(`Total Expenses,${summary.totalExpenses},,`);
    lines.push('');
    lines.push(`NET INCOME,${summary.netIncome},,`);

    const blob = new Blob([lines.join('\n')], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rfbooks-pl-${summary.period}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }

  formatMonth(ym: string): string {
    const [year, month] = ym.split('-');
    const date = new Date(+year, +month - 1);
    return date.toLocaleDateString('en-US', { month: 'short' });
  }

  barHeight(value: number): number {
    const max = this.maxTrendValue();
    return Math.max((value / max) * 100, 2);
  }

  getCategoryColor(index: number): string {
    const colors = [
      '#3b82f6', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b',
      '#ef4444', '#ec4899', '#6366f1', '#14b8a6', '#f97316',
    ];
    return colors[index % colors.length];
  }

  getCategoryLabel(value: string, type: 'income' | 'expense'): string {
    const list = type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    const found = list.find(c => c.value === value);
    return found ? found.label : value;
  }

  currentMonthInput(): string {
    const d = this.selectedDate();
    return d.substring(0, 7);
  }

  private currentMonthDate(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
  }

  private getDateRange(period: string, date: string): { start: string; end: string } {
    const d = new Date(date + 'T00:00:00');
    const year = d.getFullYear();
    const month = d.getMonth();

    switch (period) {
      case 'QUARTER': {
        const qStart = Math.floor(month / 3) * 3;
        const start = new Date(year, qStart, 1);
        const end = new Date(year, qStart + 3, 0);
        return { start: this.fmt(start), end: this.fmt(end) };
      }
      case 'YEAR':
        return { start: `${year}-01-01`, end: `${year}-12-31` };
      default: { // MONTH
        const start = new Date(year, month, 1);
        const end = new Date(year, month + 1, 0);
        return { start: this.fmt(start), end: this.fmt(end) };
      }
    }
  }

  private fmt(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
