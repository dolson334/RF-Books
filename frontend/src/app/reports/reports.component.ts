import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReportService } from './report.service';
import { FinancialReport, ReportPeriod } from './report.models';

@Component({
  selector: 'rf-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {
  report = signal<FinancialReport | null>(null);
  isLoading = signal<boolean>(false);
  selectedPeriod = signal<ReportPeriod>('month');
  
  periods: { value: ReportPeriod; label: string }[] = [
    { value: 'week', label: 'Last 7 Days' },
    { value: 'month', label: 'Last 30 Days' },
    { value: 'quarter', label: 'Last 90 Days' },
    { value: 'year', label: 'Last Year' }
  ];

  readonly netIncomeClass = computed(() => {
    const net = this.report()?.summary.netIncome || 0;
    return net >= 0 ? 'positive' : 'negative';
  });

  readonly maxTrendValue = computed(() => {
    if (!this.report()?.trends) return 0;
    return Math.max(
      ...this.report()!.trends.map(t => Math.max(t.income, t.expenses)),
      0
    );
  });

  constructor(private reportService: ReportService, private router: Router) {}

  ngOnInit(): void {
    this.loadReport();
  }

  loadReport(): void {
    this.isLoading.set(true);
    this.reportService.getFinancialReport({ period: this.selectedPeriod() }).subscribe({
      next: (report) => {
        this.report.set(report);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load report', err);
        this.isLoading.set(false);
      }
    });
  }

  onPeriodChange(): void {
    this.loadReport();
  }

  getMaxValue(values: number[]): number {
    return Math.max(...values, 0);
  }

  getBarHeight(value: number, maxValue: number): number {
    return maxValue > 0 ? (value / maxValue) * 100 : 0;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }

  formatPercent(value: number): string {
    return `${value.toFixed(1)}%`;
  }

  getDonutSegment(percentage: number): string {
    const circumference = 502.65;
    const dashLength = (percentage * circumference) / 100;
    return `${dashLength} ${circumference}`;
  }

  getDonutOffset(index: number): number {
    if (!this.report()) return 0;
    const circumference = 502.65;
    const previousPercentages = this.report()!.expensesByCategory
      .slice(0, index)
      .reduce((sum, cat) => sum + cat.percentage, 0);
    return -(circumference * previousPercentages) / 100;
  }

  openReport(reportType: string): void {
    if (reportType === 'profit-loss') {
      this.router.navigate(['/reports/profit-loss']);
    } else {
      // TODO: Implement other reports
      alert(`${reportType} report coming soon with drill-down capabilities!`);
    }
  }
}
