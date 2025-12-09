import { Component, OnInit, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface PLLineItem {
  name: string;
  amount: number;
  percentage?: number;
  isCategory?: boolean;
  isSubtotal?: boolean;
  isTotal?: boolean;
  children?: PLLineItem[];
  expanded?: boolean;
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

  constructor(private router: Router) {}

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
    
    // TODO: Replace with actual API call
    setTimeout(() => {
      this.reportData.set([
        {
          name: 'Income',
          amount: 125000,
          isCategory: true,
          expanded: true,
          children: [
            { name: 'Room Revenue', amount: 85000, percentage: 68 },
            { name: 'Food & Beverage', amount: 25000, percentage: 20 },
            { name: 'Activities & Tours', amount: 10000, percentage: 8 },
            { name: 'Other Income', amount: 5000, percentage: 4 }
          ]
        },
        {
          name: 'Total Income',
          amount: 125000,
          isSubtotal: true
        },
        {
          name: 'Cost of Goods Sold',
          amount: 15000,
          isCategory: true,
          expanded: true,
          children: [
            { name: 'Food Costs', amount: 8000, percentage: 53.3 },
            { name: 'Beverage Costs', amount: 5000, percentage: 33.3 },
            { name: 'Activity Supplies', amount: 2000, percentage: 13.3 }
          ]
        },
        {
          name: 'Total COGS',
          amount: 15000,
          isSubtotal: true
        },
        {
          name: 'Gross Profit',
          amount: 110000,
          isSubtotal: true
        },
        {
          name: 'Operating Expenses',
          amount: 45000,
          isCategory: true,
          expanded: true,
          children: [
            { name: 'Payroll & Benefits', amount: 20000, percentage: 44.4 },
            { name: 'Marketing & Advertising', amount: 5000, percentage: 11.1 },
            { name: 'Utilities', amount: 4000, percentage: 8.9 },
            { name: 'Maintenance & Repairs', amount: 6000, percentage: 13.3 },
            { name: 'Insurance', amount: 3000, percentage: 6.7 },
            { name: 'Office Supplies', amount: 2000, percentage: 4.4 },
            { name: 'Professional Services', amount: 3000, percentage: 6.7 },
            { name: 'Other Expenses', amount: 2000, percentage: 4.4 }
          ]
        },
        {
          name: 'Total Operating Expenses',
          amount: 45000,
          isSubtotal: true
        },
        {
          name: 'Net Income',
          amount: 65000,
          isTotal: true
        }
      ]);
      this.isLoading.set(false);
    }, 500);
  }

  toggleExpand(item: PLLineItem): void {
    if (item.children) {
      item.expanded = !item.expanded;
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
