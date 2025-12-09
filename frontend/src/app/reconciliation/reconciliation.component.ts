import { Component, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Payment,
  ReconciliationMatch,
  ReconciliationStatus,
} from './reconciliation.models';
import { ReconciliationService } from './reconciliation.service';
import { PlaidService } from './plaid.service';
import { Router } from '@angular/router';
import { OnboardingStatusService } from '../services/onboarding-status.service';

@Component({
  selector: 'rf-reconciliation-center',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reconciliation.component.html',
  styleUrls: ['./reconciliation.component.scss'],
})
export class ReconciliationComponent implements OnInit {
  bankConnected = signal<boolean>(false);
  connectionError = signal<boolean>(false);
  from = signal<string>('');
  to = signal<string>('');

  isLoading = signal<boolean>(false);
  payments = signal<Payment[]>([]);
  matches = signal<ReconciliationMatch[]>([]);

  // Onboarding status
  showConfigWarning = signal<boolean>(false);
  missingItems = signal<string[]>([]);

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

  constructor(
    private reconService: ReconciliationService,
    private plaidService: PlaidService,
    private router: Router,
    private onboardingStatus: OnboardingStatusService
  ) {}

  ngOnInit(): void {
    const today = new Date();
    const weekAgo = new Date();
    weekAgo.setDate(today.getDate() - 7);
    this.from.set(this.toLocalInputValue(weekAgo));
    this.to.set(this.toLocalInputValue(today));

    // Check backend connection status
    this.checkConnectionStatus();
    
    // Check onboarding status
    this.checkOnboardingStatus();
  }

  checkOnboardingStatus(): void {
    this.onboardingStatus.checkStatus();
    
    // Wait a moment for status to populate, then check
    setTimeout(() => {
      if (this.onboardingStatus.hasMissingRequiredConfig()) {
        this.showConfigWarning.set(true);
        this.missingItems.set(this.onboardingStatus.getMissingItems());
      }
    }, 500);
  }

  dismissConfigWarning(): void {
    this.showConfigWarning.set(false);
  }

  goToCompleteSetup(): void {
    this.router.navigate(['/settings']);
  }

  checkConnectionStatus(): void {
    this.plaidService.getConnectionStatus().subscribe({
      next: status => {
        this.bankConnected.set(status.connected);
        this.connectionError.set(false);
        // Sync with localStorage
        localStorage.setItem('rfbooks_bank_connected', String(status.connected));
      },
      error: () => {
        this.connectionError.set(true);
        this.bankConnected.set(false);
        localStorage.setItem('rfbooks_bank_connected', 'false');
      },
    });
  }

  goToOnboarding(): void {
    this.router.navigate(['/recon/onboarding']);
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
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
        this.connectionError.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        // Check if error is due to missing token
        if (err.status === 400 || err.status === 500) {
          this.connectionError.set(true);
          this.bankConnected.set(false);
          localStorage.setItem('rfbooks_bank_connected', 'false');
        }
      },
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
