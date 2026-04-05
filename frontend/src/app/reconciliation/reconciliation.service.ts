import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ReconciliationSummary, Expense, Income, MatchSuggestion } from './reconciliation.models';

@Injectable({
  providedIn: 'root',
})
export class ReconciliationService {
  private readonly baseUrl = '/api/reconciliation';
  private readonly expenseUrl = '/api/expenses';
  private readonly incomeUrl = '/api/income';

  constructor(private http: HttpClient) {}

  getLatestSummary(): Observable<ReconciliationSummary> {
    return this.http.get<ReconciliationSummary>(`${this.baseUrl}/summary`);
  }

  runNow(): Observable<ReconciliationSummary> {
    return this.http.post<ReconciliationSummary>(`${this.baseUrl}/refresh`, {});
  }

  // --- Auto-match suggestion APIs ---

  generateSuggestions(): Observable<MatchSuggestion[]> {
    return this.http.post<MatchSuggestion[]>(`${this.baseUrl}/suggestions/generate`, {});
  }

  getSuggestions(): Observable<MatchSuggestion[]> {
    return this.http.get<MatchSuggestion[]>(`${this.baseUrl}/suggestions`);
  }

  acceptSuggestion(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/suggestions/${id}/accept`, {});
  }

  rejectSuggestion(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/suggestions/${id}/reject`, {});
  }

  autoMatchAll(): Observable<{ accepted: number }> {
    return this.http.post<{ accepted: number }>(`${this.baseUrl}/auto-match`, {});
  }

  // --- Expense reconciliation ---

  getExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(this.expenseUrl);
  }

  createManualExpenseMatch(expenseId: number, transactionId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/match/expense`, { expenseId, transactionId });
  }

  deleteManualExpenseMatch(expenseId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/match/expense/${expenseId}`);
  }

  getManualExpenseMatches(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/matches/expenses`);
  }

  // --- Income reconciliation ---

  getIncome(): Observable<Income[]> {
    return this.http.get<Income[]>(this.incomeUrl);
  }

  createManualIncomeMatch(incomeId: number, transactionId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/match/income`, { incomeId, transactionId });
  }

  deleteManualIncomeMatch(incomeId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/match/income/${incomeId}`);
  }

  getManualIncomeMatches(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/matches/income`);
  }
}
