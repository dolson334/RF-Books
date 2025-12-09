import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Payment, ReconciliationMatch, ReconciliationSummary } from './reconciliation.models';

@Injectable({
  providedIn: 'root',
})
export class ReconciliationService {
  private readonly baseUrl = 'http://localhost:8081/api/reconciliation';

  constructor(private http: HttpClient) {}

  getLatestSummary(): Observable<ReconciliationSummary> {
    return this.http.get<ReconciliationSummary>(`${this.baseUrl}/latest`);
  }

  getLatestDetails(): Observable<ReconciliationMatch[]> {
    return this.http.get<ReconciliationMatch[]>(`${this.baseUrl}/latest/details`);
  }

  runNow(): Observable<ReconciliationSummary> {
    return this.http.post<ReconciliationSummary>(`${this.baseUrl}/run-now`, {});
  }

  getPayments(from?: string, to?: string): Observable<Payment[]> {
    const params: any = {};
    if (from) params.from = from;
    if (to) params.to = to;
    return this.http.get<Payment[]>(`${this.baseUrl}/payments`, { params });
  }

  runReconciliation(from: string, to: string): Observable<ReconciliationMatch[]> {
    return this.http.post<ReconciliationMatch[]>(`${this.baseUrl}/run`, {
      from,
      to
    });
  }

  createManualMatch(paymentId: string, transactionId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/match`, {
      paymentId,
      transactionId
    });
  }

  deleteManualMatch(paymentId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/match/${paymentId}`);
  }

  getManualMatches(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/matches`);
  }
}
