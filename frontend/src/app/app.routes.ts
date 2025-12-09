import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { BankOnboardingComponent } from './reconciliation/bank-onboarding.component';
import { OnboardingComponent } from './onboarding/onboarding.component';

const getInitialRedirect = (): string => {
  const complete = localStorage.getItem('rfbooks_onboarding_complete');
  return complete === 'true' ? 'recon' : 'onboarding';
};

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: getInitialRedirect(), 
    pathMatch: 'full' 
  },
  { path: 'onboarding', component: OnboardingComponent },
  { path: 'settings', component: OnboardingComponent }, // Edit mode
  { path: 'recon', component: ReconciliationComponent },
  { path: 'recon/onboarding', component: BankOnboardingComponent },
  { path: '**', redirectTo: 'recon' },
];
