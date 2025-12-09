import { Component, computed, signal, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ReconciliationMatch,
  ReconciliationSummary,
  ReconciliationStatus,
  BankTransactionSummary,
  Expense,
  Income,
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

  // Expense reconciliation
  expenses = signal<Expense[]>([]);
  selectedExpense = signal<Expense | null>(null);
  selectedExpenseTransaction = signal<BankTransactionSummary | null>(null);
  isMatchingExpense = signal<boolean>(false);

  // Income reconciliation
  income = signal<Income[]>([]);
  selectedIncome = signal<Income | null>(null);
  selectedIncomeTransaction = signal<BankTransactionSummary | null>(null);
  isMatchingIncome = signal<boolean>(false);

  // Bank transactions
  bankTransactions = signal<BankTransactionSummary[]>([]);

  // View toggle
  showMatched = signal<boolean>(false);

  // Tab selection
  activeTab = signal<'income' | 'expenses'>('income');

  readonly unmatchedCount = computed(() => {
    const s = this.summary();
    return s ? s.unmatchedBankCount : 0;
  });

  readonly matchedCount = computed(() => {
    const s = this.summary();
    return s ? s.matchedCount : 0;
  });

  readonly hasIssues = computed(() => {
    const s = this.summary();
    return s ? s.hasIssues : false;
  });

  readonly unreconciledExpenses = computed(() => {
    return this.expenses().filter(e => !e.reconciled);
  });

  readonly unreconciledIncome = computed(() => {
    return this.income().filter(i => !i.reconciled);
  });

  readonly reconciledExpenses = computed(() => {
    return this.expenses().filter(e => e.reconciled);
  });

  readonly reconciledIncome = computed(() => {
    return this.income().filter(i => i.reconciled);
  });

  readonly debitTransactions = computed(() => {
    return this.bankTransactions().filter(tx => tx.amount < 0);
  });

  readonly creditTransactions = computed(() => {
    return this.bankTransactions().filter(tx => tx.amount > 0);
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
    
    // Load expenses and income
    this.loadExpenses();
    this.loadIncome();
    
    // Load bank transactions
    this.loadBankTransactions();
    
    // Auto-refresh every 5 minutes
    setInterval(() => {
      this.loadLatestReconciliation();
      this.loadBankTransactions();
    }, 5 * 60 * 1000);
  }

  loadExpenses(): void {
    this.reconService.getExpenses().subscribe({
      next: expenses => this.expenses.set(expenses),
      error: err => console.error('Failed to load expenses', err)
    });
  }

  loadIncome(): void {
    this.reconService.getIncome().subscribe({
      next: income => this.income.set(income),
      error: err => console.error('Failed to load income', err)
    });
  }

  loadBankTransactions(): void {
    // Get transactions for last 90 days
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 90);
    
    const formatDate = (date: Date) => date.toISOString().split('T')[0];
    
    this.plaidService.getTransactions(formatDate(startDate), formatDate(endDate)).subscribe({
      next: transactions => {
        // Convert PlaidTransaction to BankTransactionSummary and filter out matched ones
        const manualExpenseMatches = this.reconService.getManualExpenseMatches();
        const manualIncomeMatches = this.reconService.getManualIncomeMatches();
        
        Promise.all([manualExpenseMatches.toPromise(), manualIncomeMatches.toPromise()]).then(([expenseMatches, incomeMatches]) => {
          const matchedTxIds = new Set([
            ...(expenseMatches || []).map((m: any) => m.transactionId),
            ...(incomeMatches || []).map((m: any) => m.transactionId)
          ]);
          
          // Filter and sort transactions by date (newest first for display)
          const sortedTransactions = transactions
            .filter(tx => !matchedTxIds.has(tx.transactionId))
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
          
          // Note: Plaid doesn't provide running balance per transaction
          // We calculate it for reference, but it's an approximation
          // Starting from an assumed balance (ideally fetch current balance from Plaid /accounts/balance/get)
          let runningBalance = 10000; // TODO: Fetch actual current balance from Plaid API
          
          const unmatchedTransactions = sortedTransactions.map((tx, index) => {
            const txBalance = runningBalance;
            // Subtract next transaction to calculate previous balance
            // (working backwards since sorted newest first)
            runningBalance -= tx.amount;
            
            return {
              id: index,
              transactionId: tx.transactionId,
              transactionDate: tx.date,
              description: tx.name,
              amount: tx.amount,
              runningBalance: txBalance, // Balance after this transaction
              currency: 'USD' as const,
              source: 'plaid' as const
            };
          });
          
          this.bankTransactions.set(unmatchedTransactions);
        });
      },
      error: err => console.error('Failed to load bank transactions', err)
    });
  }

  loadLatestReconciliation(): void {
    this.isLoading.set(true);
    this.reconService.getLatestSummary().subscribe({
      next: summary => {
        if (summary) {
          this.summary.set(summary);
          this.lastRunTime.set(this.formatTimestamp(summary.runAt));
          this.showReconciliationIssues.set(summary.hasIssues);
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
      case 'UNMATCHED_BANK_TRANSACTION':
        return 'badge unmatched-bank';
      default:
        return 'badge';
    }
  }

  setTab(tab: 'income' | 'expenses'): void {
    this.activeTab.set(tab);
    this.clearExpenseSelection();
    this.clearIncomeSelection();
  }

  // Expense matching methods
  selectExpense(expense: Expense): void {
    this.selectedExpense.set(expense);
  }

  selectExpenseTransaction(tx: BankTransactionSummary): void {
    this.selectedExpenseTransaction.set(tx);
  }

  clearExpenseSelection(): void {
    this.selectedExpense.set(null);
    this.selectedExpenseTransaction.set(null);
  }

  unmatchExpense(expenseId: number): void {
    this.reconService.deleteManualExpenseMatch(expenseId).subscribe({
      next: () => {
        this.loadExpenses();
        this.loadBankTransactions();
        this.loadLatestReconciliation();
      },
      error: err => console.error('Failed to unmatch expense', err)
    });
  }

  createExpenseMatch(): void {
    const expense = this.selectedExpense();
    const transaction = this.selectedExpenseTransaction();
    
    if (!expense || !transaction || !transaction.transactionId) return;

    this.isMatchingExpense.set(true);
    this.reconService.createManualExpenseMatch(expense.id, transaction.transactionId).subscribe({
      next: () => {
        this.isMatchingExpense.set(false);
        this.clearExpenseSelection();
        this.loadExpenses();
        this.runNow(); // Re-run to update unmatched transactions
      },
      error: (err) => {
        console.error('Failed to create expense match', err);
        this.isMatchingExpense.set(false);
      }
    });
  }

  // Income matching methods
  selectIncome(income: Income): void {
    this.selectedIncome.set(income);
  }

  selectIncomeTransaction(tx: BankTransactionSummary): void {
    this.selectedIncomeTransaction.set(tx);
  }

  clearIncomeSelection(): void {
    this.selectedIncome.set(null);
    this.selectedIncomeTransaction.set(null);
  }

  unmatchIncome(incomeId: number): void {
    this.reconService.deleteManualIncomeMatch(incomeId).subscribe({
      next: () => {
        this.loadIncome();
        this.loadBankTransactions();
        this.loadLatestReconciliation();
      },
      error: err => console.error('Failed to unmatch income', err)
    });
  }

  createIncomeMatch(): void {
    const income = this.selectedIncome();
    const transaction = this.selectedIncomeTransaction();
    
    if (!income || !transaction || !transaction.transactionId) return;

    this.isMatchingIncome.set(true);
    this.reconService.createManualIncomeMatch(income.id, transaction.transactionId).subscribe({
      next: () => {
        this.isMatchingIncome.set(false);
        this.clearIncomeSelection();
        this.loadIncome();
        this.runNow(); // Re-run to update unmatched transactions
      },
      error: (err) => {
        console.error('Failed to create income match', err);
        this.isMatchingIncome.set(false);
      }
    });
  }
}
