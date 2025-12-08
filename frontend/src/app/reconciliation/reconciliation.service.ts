import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Payment, ReconciliationMatch } from './reconciliation.models';

@Injectable({
  providedIn: 'root',
})
export class ReconciliationService {
  private readonly baseUrl = 'http://localhost:8081/api/reconciliation';

  constructor(private http: HttpClient) {}

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
}
