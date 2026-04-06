import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OnboardingService } from './onboarding.service';
import { PlaidService } from '../reconciliation/plaid.service';
import { ToastService } from '../shared/toast.service';
import {
  ChartOfAccount,
  ProductServiceItem,
  OnboardingProgress,
  DEFAULT_CHART_OF_ACCOUNTS
} from './onboarding.models';

declare const Plaid: any;

@Component({
  selector: 'rf-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.scss'],
})
export class OnboardingComponent implements OnInit {
  currentStep = signal<number>(1);
  isLoading = signal<boolean>(false);

  // Step 1: Bank
  bankConnected = signal<boolean>(false);
  plaidReady = signal<boolean>(false);
  private linkToken: string | null = null;

  // Step 2: Chart of Accounts
  accounts = signal<ChartOfAccount[]>([]);

  // Step 3: Products & Services
  products = signal<ProductServiceItem[]>([]);

  // New item forms
  newAccount: ChartOfAccount = { accountNumber: '', accountName: '', accountType: 'EXPENSE' };
  newProduct: ProductServiceItem = { name: '', type: 'SERVICE' };

  steps = ['Connect Bank', 'Chart of Accounts', 'Products & Services', 'Complete'];

  stepStatus = computed(() => {
    return {
      bankDone: this.bankConnected(),
      accountsDone: this.accounts().length > 0,
      productsDone: this.products().length > 0,
    };
  });

  constructor(
    private onboardingService: OnboardingService,
    private plaidService: PlaidService,
    private toastService: ToastService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadProgress();
  }

  private loadProgress(): void {
    this.isLoading.set(true);
    this.onboardingService.getProgress().subscribe({
      next: (progress) => {
        this.bankConnected.set(progress.bankConnected);

        // Load existing data
        this.onboardingService.getChartOfAccounts().subscribe({
          next: (accts) => {
            if (accts.length > 0) {
              this.accounts.set(accts);
            } else {
              this.accounts.set([...DEFAULT_CHART_OF_ACCOUNTS]);
            }
          },
        });

        this.onboardingService.getProductsServices().subscribe({
          next: (ps) => this.products.set(ps),
        });

        // Check bank status via Plaid
        this.plaidService.getConnectionStatus().subscribe({
          next: (status) => {
            this.bankConnected.set(status.connected);
            this.loadPlaidScriptIfNeeded();
          },
        });

        this.isLoading.set(false);
      },
      error: () => {
        // First time — no progress yet, load defaults
        this.accounts.set([...DEFAULT_CHART_OF_ACCOUNTS]);
        this.loadPlaidScriptIfNeeded();
        this.isLoading.set(false);
      },
    });
  }

  // --- Step navigation ---
  goToStep(step: number): void {
    this.currentStep.set(step);
  }

  nextStep(): void {
    if (this.currentStep() < 4) {
      this.currentStep.set(this.currentStep() + 1);
    }
  }

  prevStep(): void {
    if (this.currentStep() > 1) {
      this.currentStep.set(this.currentStep() - 1);
    }
  }

  // --- Step 1: Bank Connection ---
  private loadPlaidScriptIfNeeded(): void {
    if ((window as any).Plaid) {
      this.initLinkToken();
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://cdn.plaid.com/link/v2/stable/link-initialize.js';
    script.async = true;
    script.onload = () => this.initLinkToken();
    document.body.appendChild(script);
  }

  private initLinkToken(): void {
    this.plaidService.createLinkToken().subscribe({
      next: (res) => {
        this.linkToken = res.link_token;
        this.plaidReady.set(true);
      },
    });
  }

  launchPlaid(): void {
    if (!this.linkToken) return;
    const handler = Plaid.create({
      token: this.linkToken,
      onSuccess: (publicToken: string, metadata: any) => {
        const institutionName = metadata?.institution?.name;
        this.isLoading.set(true);
        this.plaidService.exchangePublicToken({ publicToken, institutionName }).subscribe({
          next: () => {
            this.bankConnected.set(true);
            this.isLoading.set(false);
            this.toastService.show('Bank connected successfully!', 'success');
          },
          error: () => {
            this.toastService.show('Failed to connect bank account.', 'error');
            this.isLoading.set(false);
          },
        });
      },
    });
    handler.open();
  }

  skipBank(): void {
    this.nextStep();
  }

  // --- Step 2: Chart of Accounts ---
  addAccount(): void {
    if (!this.newAccount.accountNumber || !this.newAccount.accountName) return;
    this.accounts.set([...this.accounts(), { ...this.newAccount }]);
    this.newAccount = { accountNumber: '', accountName: '', accountType: 'EXPENSE' };
  }

  removeAccount(index: number): void {
    const updated = this.accounts().filter((_, i) => i !== index);
    this.accounts.set(updated);
  }

  saveAccounts(): void {
    this.isLoading.set(true);
    this.onboardingService.saveChartOfAccounts(this.accounts()).subscribe({
      next: () => {
        this.toastService.show('Chart of accounts saved!', 'success');
        this.isLoading.set(false);
        this.nextStep();
      },
      error: () => {
        this.toastService.show('Failed to save chart of accounts.', 'error');
        this.isLoading.set(false);
      },
    });
  }

  // --- Step 3: Products & Services ---
  addProduct(): void {
    if (!this.newProduct.name) return;
    this.products.set([...this.products(), { ...this.newProduct }]);
    this.newProduct = { name: '', type: 'SERVICE' };
  }

  removeProduct(index: number): void {
    const updated = this.products().filter((_, i) => i !== index);
    this.products.set(updated);
  }

  saveProducts(): void {
    this.isLoading.set(true);
    this.onboardingService.saveProductsServices(this.products()).subscribe({
      next: () => {
        this.toastService.show('Products & services saved!', 'success');
        this.isLoading.set(false);
        this.nextStep();
      },
      error: () => {
        this.toastService.show('Failed to save products & services.', 'error');
        this.isLoading.set(false);
      },
    });
  }

  skipProducts(): void {
    this.nextStep();
  }

  // --- Step 4: Complete ---
  finishOnboarding(): void {
    this.isLoading.set(true);
    this.onboardingService.completeOnboarding().subscribe({
      next: () => {
        this.toastService.show('Onboarding complete! Welcome to RF Books.', 'success');
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.toastService.show('Failed to complete onboarding.', 'error');
        this.isLoading.set(false);
      },
    });
  }
}
