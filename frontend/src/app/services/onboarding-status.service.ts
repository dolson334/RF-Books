import { Injectable, signal } from '@angular/core';
import { OnboardingService } from '../onboarding/onboarding.service';
import { OnboardingProgress } from '../onboarding/onboarding.models';

export interface MissingConfig {
  chartOfAccounts: boolean;
  productsServices: boolean;
  bank: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class OnboardingStatusService {
  missingConfig = signal<MissingConfig | null>(null);
  isCheckingStatus = signal<boolean>(false);

  constructor(private onboardingService: OnboardingService) {}

  checkStatus(): void {
    this.isCheckingStatus.set(true);
    this.onboardingService.getProgress().subscribe({
      next: progress => {
        const missing: MissingConfig = {
          chartOfAccounts: !progress.chartOfAccountsCreated,
          productsServices: !progress.productsServicesCreated,
          bank: !progress.bankConnected
        };

        // Only set if something is actually missing
        if (missing.chartOfAccounts || missing.productsServices) {
          this.missingConfig.set(missing);
          localStorage.setItem('rfbooks_onboarding_incomplete', 'true');
          sessionStorage.setItem('rfbooks_missing_config', JSON.stringify(missing));
        } else {
          this.missingConfig.set(null);
          localStorage.setItem('rfbooks_onboarding_incomplete', 'false');
          localStorage.setItem('rfbooks_onboarding_complete', 'true');
        }

        this.isCheckingStatus.set(false);
      },
      error: () => {
        this.isCheckingStatus.set(false);
      },
    });
  }

  getMissingConfigFromSession(): MissingConfig | null {
    const stored = sessionStorage.getItem('rfbooks_missing_config');
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch {
        return null;
      }
    }
    return null;
  }

  hasMissingRequiredConfig(): boolean {
    const missing = this.missingConfig() || this.getMissingConfigFromSession();
    return !!(missing && (missing.chartOfAccounts || missing.productsServices));
  }

  getMissingItems(): string[] {
    const missing = this.missingConfig() || this.getMissingConfigFromSession();
    if (!missing) return [];

    const items: string[] = [];
    if (missing.chartOfAccounts) items.push('Chart of Accounts');
    if (missing.productsServices) items.push('Products & Services');
    if (missing.bank) items.push('Bank Connection (Optional)');
    
    return items;
  }

  clearStatus(): void {
    this.missingConfig.set(null);
    sessionStorage.removeItem('rfbooks_missing_config');
    localStorage.removeItem('rfbooks_onboarding_incomplete');
  }
}
