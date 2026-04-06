import { Component, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  ReconciliationSummary,
  BankTransactionSummary,
  Expense,
  Income,
  MatchSuggestion,
} from './reconciliation.models';
import { ReconciliationService } from './reconciliation.service';
import { PlaidService } from './plaid.service';
import { Router } from '@angular/router';
import { ToastService } from '../shared/toast.service';

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
  summary = signal<ReconciliationSummary | null>(null);
  lastRunTime = signal<string>('');
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
  currentBalance = signal<number | null>(null);

  // Auto-match suggestions
  suggestions = signal<MatchSuggestion[]>([]);
  isAutoMatching = signal<boolean>(false);
  autoMatchResult = signal<string | null>(null);

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

  readonly unreconciledExpenses = computed(() =>
    this.expenses().filter(e => !e.reconciled && !e.resolved)
  );

  readonly unreconciledIncome = computed(() =>
    this.income().filter(i => !i.reconciled && !i.resolved)
  );

  readonly reconciledExpenses = computed(() =>
    this.expenses().filter(e => e.reconciled)
  );

  readonly reconciledIncome = computed(() =>
    this.income().filter(i => i.reconciled)
  );

  readonly resolvedExpenses = computed(() =>
    this.expenses().filter(e => e.resolved && !e.reconciled)
  );

  readonly resolvedIncome = computed(() =>
    this.income().filter(i => i.resolved && !i.reconciled)
  );

  readonly debitTransactions = computed(() =>
    this.bankTransactions().filter(tx => tx.amount < 0)
  );

  readonly creditTransactions = computed(() =>
    this.bankTransactions().filter(tx => tx.amount > 0)
  );

  readonly reconciliationProgress = computed(() => {
    const s = this.summary();
    if (!s || s.totalPayments === 0) return 0;
    return Math.round((s.matchedCount / (s.totalPayments + s.totalBankTransactions)) * 200);
  });

  readonly expenseSuggestions = computed(() =>
    this.suggestions().filter(s => s.expenseId != null)
  );

  readonly incomeSuggestions = computed(() =>
    this.suggestions().filter(s => s.incomeId != null)
  );

  constructor(
    private reconService: ReconciliationService,
    private plaidService: PlaidService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.checkConnectionStatus();
    this.loadLatestReconciliation();
    this.loadExpenses();
    this.loadIncome();
    this.loadBankTransactions();
    this.loadBalance();
  }

  loadExpenses(): void {
    this.reconService.getExpenses().subscribe({
      next: expenses => this.expenses.set(expenses),
      error: () => this.toast.error('Failed to load expenses')
    });
  }

  loadIncome(): void {
    this.reconService.getIncome().subscribe({
      next: income => this.income.set(income),
      error: () => this.toast.error('Failed to load income')
    });
  }

  loadBalance(): void {
    this.plaidService.getBalance().subscribe({
      next: res => this.currentBalance.set(res.balance),
      error: () => this.currentBalance.set(null)
    });
  }

  async loadBankTransactions(): Promise<void> {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 90);

    const formatDate = (date: Date) => date.toISOString().split('T')[0];

    try {
      const transactions = await firstValueFrom(
        this.plaidService.getTransactions(formatDate(startDate), formatDate(endDate))
      );

      const [expenseMatches, incomeMatches] = await Promise.all([
        firstValueFrom(this.reconService.getManualExpenseMatches()),
        firstValueFrom(this.reconService.getManualIncomeMatches())
      ]);

      const matchedTxIds = new Set([
        ...(expenseMatches || []).map((m: any) => m.transactionId),
        ...(incomeMatches || []).map((m: any) => m.transactionId)
      ]);

      const sortedTransactions = transactions
        .filter(tx => !matchedTxIds.has(tx.transactionId))
        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

      let runningBalance = this.currentBalance() ?? 10000;

      const unmatchedTransactions: BankTransactionSummary[] = sortedTransactions.map((tx, index) => {
        const txBalance = runningBalance;
        runningBalance -= tx.amount;

        return {
          id: index,
          transactionId: tx.transactionId,
          transactionDate: tx.date,
          description: tx.name,
          amount: tx.amount,
          runningBalance: txBalance,
          currency: 'USD' as const,
          source: 'plaid' as const
        };
      });

      this.bankTransactions.set(unmatchedTransactions);
    } catch (err) {
      this.toast.error('Failed to load bank transactions');
    }
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

  checkConnectionStatus(): void {
    this.plaidService.getConnectionStatus().subscribe({
      next: status => {
        this.bankConnected.set(status.connected);
        this.connectionError.set(false);
      },
      error: () => {
        this.connectionError.set(true);
        this.bankConnected.set(false);
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
        this.loadBankTransactions();
      },
      error: (err) => {
        this.toast.error('Failed to run reconciliation');
        this.isLoading.set(false);
      }
    });
  }

  goToOnboarding(): void {
    this.router.navigate(['/onboarding']);
  }

  setTab(tab: 'income' | 'expenses'): void {
    this.activeTab.set(tab);
    this.clearExpenseSelection();
    this.clearIncomeSelection();
  }

  // --- Auto-match ---

  smartMatch(): void {
    this.isAutoMatching.set(true);
    this.autoMatchResult.set(null);
    this.reconService.generateSuggestions().subscribe({
      next: suggestions => {
        this.suggestions.set(suggestions);
        this.isAutoMatching.set(false);
        if (suggestions.length === 0) {
          this.autoMatchResult.set('No matches found');
        }
      },
      error: err => {
        this.toast.error('Auto-match failed');
        this.isAutoMatching.set(false);
        this.autoMatchResult.set('Auto-match failed');
      }
    });
  }

  acceptSuggestion(suggestion: MatchSuggestion): void {
    this.reconService.acceptSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestions.update(list => list.filter(s => s.id !== suggestion.id));
        this.refreshAll();
      },
      error: () => this.toast.error('Failed to accept suggestion')
    });
  }

  rejectSuggestion(suggestion: MatchSuggestion): void {
    this.reconService.rejectSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestions.update(list => list.filter(s => s.id !== suggestion.id));
      },
      error: () => this.toast.error('Failed to reject suggestion')
    });
  }

  acceptAllSuggestions(): void {
    this.isAutoMatching.set(true);
    this.reconService.autoMatchAll().subscribe({
      next: result => {
        this.isAutoMatching.set(false);
        this.autoMatchResult.set(`${result.accepted} matches accepted`);
        this.suggestions.set([]);
        this.refreshAll();
      },
      error: err => {
        this.toast.error('Failed to accept all suggestions');
        this.isAutoMatching.set(false);
      }
    });
  }

  getConfidenceClass(score: number): string {
    if (score >= 70) return 'confidence-high';
    if (score >= 40) return 'confidence-medium';
    return 'confidence-low';
  }

  // --- Expense matching ---

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
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to unmatch expense')
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
        this.refreshAll();
      },
      error: (err) => {
        this.toast.error('Failed to create expense match');
        this.isMatchingExpense.set(false);
      }
    });
  }

  // --- Income matching ---

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
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to unmatch income')
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
        this.refreshAll();
      },
      error: (err) => {
        this.toast.error('Failed to create income match');
        this.isMatchingIncome.set(false);
      }
    });
  }

  private refreshAll(): void {
    this.loadExpenses();
    this.loadIncome();
    this.loadBankTransactions();
    this.loadLatestReconciliation();
  }

  resolveExpense(expense: Expense): void {
    this.reconService.resolveExpense(expense.id).subscribe({
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to resolve expense')
    });
  }

  unresolveExpense(expense: Expense): void {
    this.reconService.unresolveExpense(expense.id).subscribe({
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to unresolve expense')
    });
  }

  resolveIncome(income: Income): void {
    this.reconService.resolveIncome(income.id).subscribe({
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to resolve income')
    });
  }

  unresolveIncome(income: Income): void {
    this.reconService.unresolveIncome(income.id).subscribe({
      next: () => this.refreshAll(),
      error: () => this.toast.error('Failed to unresolve income')
    });
  }
}
