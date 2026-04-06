import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';
import { OnboardingComponent } from './onboarding/onboarding.component';
import { ExpensesComponent } from './expenses/expenses.component';
import { IncomeComponent } from './income/income.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ReportsComponent } from './reports/reports.component';

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: 'dashboard', 
    pathMatch: 'full' 
  },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'recon', component: ReconciliationComponent },
  { path: 'onboarding', component: OnboardingComponent },
  { path: 'expenses', component: ExpensesComponent },
  { path: 'income', component: IncomeComponent },
  { path: 'reports', component: ReportsComponent },
  { path: '**', redirectTo: 'dashboard' },
];
