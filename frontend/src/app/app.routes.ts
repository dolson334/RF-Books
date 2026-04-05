import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { BankOnboardingComponent } from './reconciliation/bank-onboarding.component';
import { ExpensesComponent } from './expenses/expenses.component';
import { IncomeComponent } from './income/income.component';

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: 'recon', 
    pathMatch: 'full' 
  },
  { path: 'recon', component: ReconciliationComponent },
  { path: 'recon/onboarding', component: BankOnboardingComponent },
  { path: 'expenses', component: ExpensesComponent },
  { path: 'income', component: IncomeComponent },
  { path: '**', redirectTo: 'recon' },
];
