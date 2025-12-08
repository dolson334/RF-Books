import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { BankOnboardingComponent } from './reconciliation/bank-onboarding.component';

export const routes: Routes = [
  { path: '', redirectTo: 'recon', pathMatch: 'full' },
  { path: 'recon', component: ReconciliationComponent },
  { path: 'recon/onboarding', component: BankOnboardingComponent },
  { path: '**', redirectTo: 'recon' },
];
