import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PlaidService } from './plaid.service';

declare const Plaid: any; // from Plaid Link script

@Component({
  selector: 'rf-bank-onboarding',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bank-onboarding.component.html',
  styleUrls: ['./bank-onboarding.component.scss'],
})
export class BankOnboardingComponent implements OnInit {
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);
  plaidReady = signal<boolean>(false);
  currentStep = signal<1 | 2 | 3>(1);

  private linkToken: string | null = null;

  constructor(
    private plaid: PlaidService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadPlaidScriptIfNeeded();
  }

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
    this.error.set(null);

    this.plaid.createLinkToken().subscribe({
      next: res => {
        this.linkToken = res.link_token;
        this.plaidReady.set(true);
        this.isLoading.set(false);
        this.currentStep.set(2);
      },
      error: () => {
        this.error.set('Failed to create Plaid link token.');
        this.isLoading.set(false);
      },
    });
  }

  launchPlaid(): void {
    if (!this.linkToken) return;

    this.error.set(null);

    const handler = Plaid.create({
      token: this.linkToken,
      onSuccess: (publicToken: string, metadata: any) => {
        const institutionName = metadata?.institution?.name;
        this.completeExchange(publicToken, institutionName);
      },
      onExit: () => {
        // User closed Plaid without connecting
      },
    });

    handler.open();
  }

  private completeExchange(publicToken: string, institutionName?: string): void {
    this.isLoading.set(true);
    this.plaid
      .exchangePublicToken({ publicToken, institutionName })
      .subscribe({
        next: () => {
          // mark bank connected for recon UI
          localStorage.setItem('rfbooks_bank_connected', 'true');
          this.isLoading.set(false);
          this.currentStep.set(3);
        },
        error: () => {
          this.error.set('Failed to connect bank account.');
          this.isLoading.set(false);
        },
      });
  }

  goToReconciliation(): void {
    this.router.navigate(['/recon']);
  }
}
