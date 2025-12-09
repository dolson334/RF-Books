import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PlaidService } from '../reconciliation/plaid.service';
import { OnboardingService } from './onboarding.service';
import { ChartOfAccount, ProductService, AccountType } from './onboarding.models';

declare const Plaid: any;

@Component({
  selector: 'rf-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.scss'],
})
export class OnboardingComponent implements OnInit {
  currentStep = signal<1 | 2 | 3 | 4>(1);
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);
  isEditMode = signal<boolean>(false);
  
  // Step 1: Chart of Accounts
  accounts = signal<ChartOfAccount[]>([]);
  accountTypes: AccountType[] = ['ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'];

  // Step 2: Products & Services
  productsServices = signal<ProductService[]>([]);

  // Step 3: Bank Connection (Optional)
  plaidReady = signal<boolean>(false);
  bankConnected = signal<boolean>(false);
  private linkToken: string | null = null;

  constructor(
    private plaidService: PlaidService,
    private onboardingService: OnboardingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if onboarding is already complete (edit mode)
    const onboardingComplete = localStorage.getItem('rfbooks_onboarding_complete');
    if (onboardingComplete === 'true') {
      this.isEditMode.set(true);
      this.loadExistingData();
    } else {
      this.loadDefaultChartOfAccounts();
      this.loadDefaultProductsServices();
    }
    this.loadPlaidScriptIfNeeded();
  }

  private loadExistingData(): void {
    // Load existing chart of accounts
    this.onboardingService.getChartOfAccounts().subscribe({
      next: accounts => {
        if (accounts.length > 0) {
          this.accounts.set(accounts);
        } else {
          this.loadDefaultChartOfAccounts();
        }
      },
      error: () => this.loadDefaultChartOfAccounts()
    });

    // Load existing products & services
    this.onboardingService.getProductsServices().subscribe({
      next: items => {
        if (items.length > 0) {
          this.productsServices.set(items);
        } else {
          this.loadDefaultProductsServices();
        }
      },
      error: () => this.loadDefaultProductsServices()
    });
  }

  // ========== STEP 1: CHART OF ACCOUNTS ==========

  private loadPlaidScriptIfNeeded(): void {
    if ((window as any).Plaid) {
      this.initLinkToken();
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://cdn.plaid.com/link/v2/stable/link-initialize.js';
    script.async = true;
    script.onload = () => this.initLinkToken();
    script.onerror = () => {
      this.error.set('Unable to load Plaid Link script.');
    };
    document.body.appendChild(script);
  }

  private initLinkToken(): void {
    this.isLoading.set(true);
    this.plaidService.createLinkToken().subscribe({
      next: res => {
        this.linkToken = res.link_token;
        this.plaidReady.set(true);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Failed to create Plaid link token.');
        this.isLoading.set(false);
      },
    });
  }

  launchPlaid(): void {
    if (!this.linkToken) return;

    const handler = Plaid.create({
      token: this.linkToken,
      onSuccess: (publicToken: string, metadata: any) => {
        const institutionName = metadata?.institution?.name;
        this.completeExchange(publicToken, institutionName);
      },
      onExit: () => {},
    });

    handler.open();
  }

  private completeExchange(publicToken: string, institutionName?: string): void {
    this.isLoading.set(true);
    this.plaidService.exchangePublicToken({ publicToken, institutionName }).subscribe({
      next: () => {
        localStorage.setItem('rfbooks_bank_connected', 'true');
        this.bankConnected.set(true);
        this.isLoading.set(false);
        this.error.set(null);
      },
      error: () => {
        this.error.set('Failed to connect bank account.');
        this.isLoading.set(false);
      },
    });
  }

  // ========== STEP 1: CHART OF ACCOUNTS ==========

  private loadDefaultChartOfAccounts(): void {
    this.accounts.set([
      { accountNumber: '1000', accountName: 'Cash', accountType: 'ASSET', description: 'Checking and savings accounts' },
      { accountNumber: '1200', accountName: 'Accounts Receivable', accountType: 'ASSET', description: 'Money owed by customers' },
      { accountNumber: '2000', accountName: 'Accounts Payable', accountType: 'LIABILITY', description: 'Money owed to vendors' },
      { accountNumber: '3000', accountName: 'Equity', accountType: 'EQUITY', description: 'Owner\'s equity' },
      { accountNumber: '4000', accountName: 'Revenue', accountType: 'REVENUE', description: 'Income from sales' },
      { accountNumber: '5000', accountName: 'Operating Expenses', accountType: 'EXPENSE', description: 'General expenses' },
    ]);
  }

  addAccount(): void {
    this.accounts.update(accounts => [
      ...accounts,
      { accountNumber: '', accountName: '', accountType: 'ASSET', description: '' }
    ]);
  }

  removeAccount(index: number): void {
    this.accounts.update(accounts => accounts.filter((_, i) => i !== index));
  }

  saveChartOfAccounts(): void {
    this.isLoading.set(true);
    this.onboardingService.saveChartOfAccounts(this.accounts()).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.error.set(null);
        this.currentStep.set(2);
      },
      error: () => {
        this.error.set('Failed to save chart of accounts.');
        this.isLoading.set(false);
      },
    });
  }

  // ========== STEP 2: PRODUCTS & SERVICES ==========

  private loadDefaultProductsServices(): void {
    this.productsServices.set([
      { name: 'RV Site Rental', type: 'SERVICE', defaultPrice: 45.00, unitOfMeasure: 'night' },
      { name: 'Cabin Rental', type: 'SERVICE', defaultPrice: 150.00, unitOfMeasure: 'night' },
      { name: 'Tent Site', type: 'SERVICE', defaultPrice: 25.00, unitOfMeasure: 'night' },
      { name: 'Firewood Bundle', type: 'PRODUCT', defaultPrice: 8.00, unitOfMeasure: 'bundle' },
    ]);
  }

  addProductService(): void {
    this.productsServices.update(items => [
      ...items,
      { name: '', type: 'SERVICE', defaultPrice: 0, unitOfMeasure: '' }
    ]);
  }

  removeProductService(index: number): void {
    this.productsServices.update(items => items.filter((_, i) => i !== index));
  }

  saveProductsServices(): void {
    this.isLoading.set(true);
    this.onboardingService.saveProductsServices(this.productsServices()).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.error.set(null);
        this.currentStep.set(3);
      },
      error: () => {
        this.error.set('Failed to save products & services.');
        this.isLoading.set(false);
      },
    });
  }

  // ========== STEP 3: BANK CONNECTION (OPTIONAL) ==========

  skipBankConnection(): void {
    this.currentStep.set(4);
  }

  connectBankAndFinish(): void {
    if (this.bankConnected()) {
      this.currentStep.set(4);
    }
  }

  // ========== STEP 4: COMPLETE ==========

  completeOnboarding(): void {
    this.isLoading.set(true);
    this.onboardingService.completeOnboarding().subscribe({
      next: () => {
        localStorage.setItem('rfbooks_onboarding_complete', 'true');
        this.router.navigate(['/recon']);
      },
      error: () => {
        this.error.set('Failed to complete onboarding.');
        this.isLoading.set(false);
      },
    });
  }

  // Navigation helpers
  goToStep(step: 1 | 2 | 3 | 4): void {
    this.currentStep.set(step);
    this.error.set(null);
  }
}
