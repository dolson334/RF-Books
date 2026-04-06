import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FinancialSummary, CategoryBreakdown, MonthlyTrend } from './report.models';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly baseUrl = '/api/reports';

  constructor(private http: HttpClient) {}

  getSummary(period: string = 'MONTH', date?: string): Observable<FinancialSummary> {
    let params = new HttpParams().set('period', period);
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<FinancialSummary>(`${this.baseUrl}/summary`, { params });
  }

  getIncomeByCategory(startDate: string, endDate: string): Observable<CategoryBreakdown[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<CategoryBreakdown[]>(`${this.baseUrl}/income-by-category`, { params });
  }

  getExpensesByCategory(startDate: string, endDate: string): Observable<CategoryBreakdown[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<CategoryBreakdown[]>(`${this.baseUrl}/expenses-by-category`, { params });
  }

  getMonthlyTrend(months: number = 6): Observable<MonthlyTrend[]> {
    const params = new HttpParams().set('months', months.toString());
    return this.http.get<MonthlyTrend[]>(`${this.baseUrl}/monthly-trend`, { params });
  }
}
