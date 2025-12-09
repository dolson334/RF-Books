import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Payment, ReconciliationMatch, ReconciliationSummary, Expense, Income } from './reconciliation.models';

@Injectable({
  providedIn: 'root',
})
export class ReconciliationService {
  private readonly baseUrl = 'http://localhost:8081/api/reconciliation';
  private readonly expenseUrl = 'http://localhost:8081/api/expenses';
  private readonly incomeUrl = 'http://localhost:8081/api/income';

  constructor(private http: HttpClient) {}

  getLatestSummary(): Observable<ReconciliationSummary> {
    return this.http.get<ReconciliationSummary>(`${this.baseUrl}/summary`);
  }

  runNow(): Observable<ReconciliationSummary> {
    return this.http.post<ReconciliationSummary>(`${this.baseUrl}/refresh`, {});
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

  // Expense reconciliation
  getExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(this.expenseUrl);
  }

  createManualExpenseMatch(expenseId: number, transactionId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/match/expense`, {
      expenseId,
      transactionId
    });
  }

  deleteManualExpenseMatch(expenseId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/match/expense/${expenseId}`);
  }

  getManualExpenseMatches(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/matches/expenses`);
  }

  // Income reconciliation
  getIncome(): Observable<Income[]> {
    return this.http.get<Income[]>(this.incomeUrl);
  }

  createManualIncomeMatch(incomeId: number, transactionId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/match/income`, {
      incomeId,
      transactionId
    });
  }

  deleteManualIncomeMatch(incomeId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/match/income/${incomeId}`);
  }

  getManualIncomeMatches(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/matches/income`);
  }
}
