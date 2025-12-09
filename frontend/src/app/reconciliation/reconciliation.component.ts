import { Component, computed, signal, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Payment,
  ReconciliationMatch,
  ReconciliationSummary,
  ReconciliationStatus,
  BankTransactionSummary,
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

  isLoading = signal<boolean>(false);
  matches = signal<ReconciliationMatch[]>([]);
  summary = signal<ReconciliationSummary | null>(null);
  lastRunTime = signal<string>('');

  // Onboarding status
  showConfigWarning = signal<boolean>(false);
  missingItems = signal<string[]>([]);
  showReconciliationIssues = signal<boolean>(false);

  // Manual matching
  selectedPayment = signal<Payment | null>(null);
  selectedTransaction = signal<BankTransactionSummary | null>(null);
  isMatching = signal<boolean>(false);

  readonly unmatchedCount = computed(() => {
    const s = this.summary();
    return s ? s.unmatchedPaymentCount + s.unmatchedBankCount : 0;
  });

  readonly matchedCount = computed(() => {
    const s = this.summary();
    return s ? s.matchedCount : 0;
  });

  readonly hasIssues = computed(() => {
    const s = this.summary();
    return s ? s.hasIssues : false;
  });

  readonly unmatchedPayments = computed(() => {
    return this.matches()
      .filter(m => m.status === 'UNMATCHED_PAYMENT' && m.payment)
      .map(m => m.payment!);
  });

  readonly unmatchedBankTransactions = computed(() => {
    return this.matches()
      .filter(m => m.status === 'UNMATCHED_BANK_TRANSACTION' && m.bankTransaction)
      .map(m => m.bankTransaction!);
  });

  constructor(
    private reconService: ReconciliationService,
    private plaidService: PlaidService,
    private router: Router,
    private onboardingStatus: OnboardingStatusService
  ) {}

  ngOnInit(): void {
    // Check backend connection status
    this.checkConnectionStatus();
    
    // Check onboarding status from backend
    this.onboardingStatus.checkStatus();
    
    // Check for missing configuration
    this.checkMissingConfiguration();
    
    // Load latest reconciliation results
    this.loadLatestReconciliation();
    
    // Auto-refresh every 5 minutes
    setInterval(() => this.loadLatestReconciliation(), 5 * 60 * 1000);
  }

  loadLatestReconciliation(): void {
    this.isLoading.set(true);
    this.reconService.getLatestSummary().subscribe({
      next: summary => {
        if (summary) {
          this.summary.set(summary);
          this.lastRunTime.set(this.formatTimestamp(summary.runAt));
          this.showReconciliationIssues.set(summary.hasIssues);
          
          // Load details if needed
          if (summary.hasIssues) {
            this.loadDetails();
          }
        } else {
          // No reconciliation runs yet
          this.summary.set(null);
          this.lastRunTime.set('Never');
          this.showReconciliationIssues.set(false);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  loadDetails(): void {
    this.reconService.getLatestDetails().subscribe({
      next: matches => {
        this.matches.set(matches);
      },
      error: () => {
        console.error('Failed to load reconciliation details');
      }
    });
  }

  checkMissingConfiguration(): void {
    const missingConfig = this.onboardingStatus.missingConfig();
    if (missingConfig && (missingConfig.chartOfAccounts || missingConfig.productsServices)) {
      this.showConfigWarning.set(true);
      this.missingItems.set(this.onboardingStatus.getMissingItems());
    } else {
      this.showConfigWarning.set(false);
      this.missingItems.set([]);
    }
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    
    return date.toLocaleString();
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

  runNow(): void {
    this.isLoading.set(true);
    this.reconService.runNow().subscribe({
      next: summary => {
        if (summary) {
          this.summary.set(summary);
          this.lastRunTime.set(this.formatTimestamp(summary.runAt));
          this.showReconciliationIssues.set(summary.hasIssues);
          
          // Load details if there are issues
          if (summary.hasIssues) {
            this.loadDetails();
          }
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to run reconciliation', err);
        this.isLoading.set(false);
      }
    });
  }

  goToOnboarding(): void {
    this.router.navigate(['/recon/onboarding']);
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  badgeClass(status: ReconciliationStatus): string {
    switch (status) {
      case 'MATCHED':
        return 'badge matched';
      case 'MANUAL_MATCH':
        return 'badge manual-match';
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

  selectPayment(payment: Payment): void {
    this.selectedPayment.set(payment);
  }

  selectTransaction(tx: BankTransactionSummary): void {
    this.selectedTransaction.set(tx);
  }

  clearSelection(): void {
    this.selectedPayment.set(null);
    this.selectedTransaction.set(null);
  }

  createManualMatch(): void {
    const payment = this.selectedPayment();
    const transaction = this.selectedTransaction();
    
    if (!payment || !transaction) return;

    this.isMatching.set(true);
    this.reconService.createManualMatch(payment.externalId, transaction.id.toString()).subscribe({
      next: () => {
        this.clearSelection();
        this.loadLatestReconciliation();
        this.runNow(); // Re-run reconciliation to see the match
      },
      error: (err) => {
        console.error('Failed to create manual match', err);
        this.isMatching.set(false);
      }
    });
  }
}
