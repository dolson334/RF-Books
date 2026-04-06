import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ReportService } from '../reports/report.service';
import { ReconciliationService } from '../reconciliation/reconciliation.service';
import {
  FinancialSummary,
  CategoryBreakdown,
  MonthlyTrend,
} from '../reports/report.models';

@Component({
  selector: 'rf-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  summary = signal<FinancialSummary | null>(null);
  incomeCategories = signal<CategoryBreakdown[]>([]);
  expenseCategories = signal<CategoryBreakdown[]>([]);
  monthlyTrend = signal<MonthlyTrend[]>([]);
  pendingSuggestions = signal<number>(0);
  isLoading = signal<boolean>(true);

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
    private reconciliationService: ReconciliationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  async loadDashboard(): Promise<void> {
    this.isLoading.set(true);
    try {
      const [summary, incCat, expCat, trend, suggestions] = await Promise.all([
        firstValueFrom(this.reportService.getSummary('MONTH')),
        firstValueFrom(
          this.reportService.getIncomeByCategory(
            this.monthStart(),
            this.monthEnd()
          )
        ),
        firstValueFrom(
          this.reportService.getExpensesByCategory(
            this.monthStart(),
            this.monthEnd()
          )
        ),
        firstValueFrom(this.reportService.getMonthlyTrend(6)),
        firstValueFrom(this.reconciliationService.getSuggestions()).catch(
          () => []
        ),
      ]);

      this.summary.set(summary);
      this.incomeCategories.set(incCat);
      this.expenseCategories.set(expCat);
      this.monthlyTrend.set(trend);
      this.pendingSuggestions.set(
        suggestions.filter((s: any) => s.status === 'PENDING').length
      );
    } finally {
      this.isLoading.set(false);
    }
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

  private monthStart(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
  }

  private monthEnd(): string {
    const now = new Date();
    const last = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    return `${last.getFullYear()}-${String(last.getMonth() + 1).padStart(2, '0')}-${String(last.getDate()).padStart(2, '0')}`;
  }
}
