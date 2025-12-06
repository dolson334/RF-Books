import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Payment, ReconciliationMatch } from './reconciliation.models';
import { MOCK_PAYMENTS, MOCK_MATCHES } from './reconciliation.mock-data';

@Injectable({
  providedIn: 'root',
})
export class ReconciliationService {
  constructor() {}

  getPayments(from?: string, to?: string): Observable<Payment[]> {
    return of(MOCK_PAYMENTS);
  }

  runReconciliation(from: string, to: string): Observable<ReconciliationMatch[]> {
    return of(MOCK_MATCHES);
  }
}
