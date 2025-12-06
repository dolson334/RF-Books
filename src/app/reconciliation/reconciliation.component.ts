import { Component, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Payment,
  ReconciliationMatch,
  ReconciliationStatus,
} from './reconciliation.models';
import { ReconciliationService } from './reconciliation.service';

@Component({
  selector: 'rf-reconciliation-center',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reconciliation.component.html',
  styleUrls: ['./reconciliation.component.scss'],
})
export class ReconciliationComponent implements OnInit {
  from = signal<string>('');
  to = signal<string>('');

  isLoading = signal<boolean>(false);
  payments = signal<Payment[]>([]);
  matches = signal<ReconciliationMatch[]>([]);

  readonly unmatchedCount = computed(
    () =>
      this.matches().filter(
        m =>
          m.status === 'UNMATCHED_PAYMENT' ||
          m.status === 'UNMATCHED_BANK_TRANSACTION',
      ).length,
  );

  readonly matchedCount = computed(
    () => this.matches().filter(m => m.status === 'MATCHED').length,
  );

  readonly multipleMatchCount = computed(
    () => this.matches().filter(m => m.status === 'MULTIPLE_MATCHES').length,
  );

  constructor(private reconService: ReconciliationService) {}

  ngOnInit(): void {
    const today = new Date();
    const weekAgo = new Date();
    weekAgo.setDate(today.getDate() - 7);
    this.from.set(this.toLocalInputValue(weekAgo));
    this.to.set(this.toLocalInputValue(today));
  }

  private toLocalInputValue(date: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    const yyyy = date.getFullYear();
    const mm = pad(date.getMonth() + 1);
    const dd = pad(date.getDate());
    const hh = pad(date.getHours());
    const min = pad(date.getMinutes());
    return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
  }

  private toIsoString(local: string): string {
    const d = new Date(local);
    return d.toISOString();
  }

  loadPayments(): void {
    this.isLoading.set(true);
    this.reconService.getPayments().subscribe({
      next: payments => {
        this.payments.set(payments);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  runReconciliation(): void {
    if (!this.from() || !this.to()) return;
    this.isLoading.set(true);
    const fromIso = this.toIsoString(this.from());
    const toIso = this.toIsoString(this.to());
    this.reconService.runReconciliation(fromIso, toIso).subscribe({
      next: matches => {
        this.matches.set(matches);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  badgeClass(status: ReconciliationStatus): string {
    switch (status) {
      case 'MATCHED':
        return 'badge matched';
      case 'MULTIPLE_MATCHES':
        return 'badge multi';
      case 'UNMATCHED_PAYMENT':
        return 'badge unmatched-payment';
      case 'UNMATCHED_BANK_TRANSACTION':
        return 'badge unmatched-bank';
      default:
        return 'badge';
    }
  }
}
