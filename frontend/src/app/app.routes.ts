import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { BankOnboardingComponent } from './reconciliation/bank-onboarding.component';
import { OnboardingComponent } from './onboarding/onboarding.component';
import { ExpensesComponent } from './expenses/expenses.component';
import { IncomeComponent } from './income/income.component';
import { ReportsComponent } from './reports/reports.component';
import { ProfitLossComponent } from './reports/profit-loss/profit-loss.component';
import { onboardingGuard } from './guards/onboarding.guard';

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
  { 
    path: 'recon', 
    component: ReconciliationComponent,
    canActivate: [onboardingGuard]
  },
  { path: 'recon/onboarding', component: BankOnboardingComponent },
  { 
    path: 'expenses', 
    component: ExpensesComponent,
    canActivate: [onboardingGuard]
  },
  { 
    path: 'income', 
    component: IncomeComponent,
    canActivate: [onboardingGuard]
  },
  { 
    path: 'reports', 
    component: ReportsComponent,
    canActivate: [onboardingGuard]
  },
  { 
    path: 'reports/profit-loss', 
    component: ProfitLossComponent,
    canActivate: [onboardingGuard]
  },
  { path: '**', redirectTo: 'recon' },
];
