import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { BankOnboardingComponent } from './reconciliation/bank-onboarding.component';
import { OnboardingComponent } from './onboarding/onboarding.component';

export const routes: Routes = [
  { path: '', redirectTo: 'onboarding', pathMatch: 'full' },
  { path: 'onboarding', component: OnboardingComponent },
  { path: 'recon', component: ReconciliationComponent },
  { path: 'recon/onboarding', component: BankOnboardingComponent },
  { path: '**', redirectTo: 'onboarding' },
];
