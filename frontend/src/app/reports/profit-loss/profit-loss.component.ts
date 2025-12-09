import { Component, OnInit, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProfitLossService, PLLineItem, ProfitLossReport } from './profit-loss.service';

interface Transaction {
  date: string;
  description: string;
  amount: number;
  category?: string;
  vendor?: string;
}

@Component({
  selector: 'rf-profit-loss',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profit-loss.component.html',
  styleUrls: ['./profit-loss.component.scss']
})
export class ProfitLossComponent implements OnInit, AfterViewInit {
  startDate = signal<string>('');
  endDate = signal<string>('');
  isLoading = signal<boolean>(false);
  reportData = signal<PLLineItem[]>([]);
  summary = signal<ProfitLossReport['summary'] | null>(null);

  constructor(private router: Router, private profitLossService: ProfitLossService) {}

  ngOnInit(): void {
    // Set default date range (last 30 days)
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);
    
    this.startDate.set(start.toISOString().split('T')[0]);
    this.endDate.set(end.toISOString().split('T')[0]);
    
    this.loadReport();
  }

  ngAfterViewInit(): void {
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  loadReport(): void {
    this.isLoading.set(true);
    
    this.profitLossService.getProfitLossReport(this.startDate(), this.endDate()).subscribe({
      next: (report) => {
        this.reportData.set(report.lineItems);
        this.summary.set(report.summary);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load P&L report', err);
        this.isLoading.set(false);
      }
    });
  }

  toggleExpand(item: PLLineItem): void {
    if (item.children) {
      item.expanded = !item.expanded;
    }
  }

  toggleTransactions(item: PLLineItem, event: Event): void {
    event.stopPropagation();
    if (item.transactions) {
      item.showTransactions = !item.showTransactions;
    }
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

  getCurrentDate(): string {
    return new Date().toLocaleDateString();
  }

  exportReport(): void {
    alert('Export to PDF/Excel coming soon!');
  }

  backToDashboard(): void {
    this.router.navigate(['/reports']);
  }
}
