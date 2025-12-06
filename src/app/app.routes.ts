import { Routes } from '@angular/router';
import { ReconciliationComponent } from './reconciliation/reconciliation.component';

export const routes: Routes = [
  { path: '', redirectTo: 'reconciliation', pathMatch: 'full' },
  { path: 'reconciliation', component: ReconciliationComponent },
  { path: '**', redirectTo: 'reconciliation' },
];
