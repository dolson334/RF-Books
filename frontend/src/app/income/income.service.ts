import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Income } from './income.models';

@Injectable({
  providedIn: 'root'
})
export class IncomeService {
  private readonly baseUrl = '/api/income';

  constructor(private http: HttpClient) {}

  getAllIncome(): Observable<Income[]> {
    return this.http.get<Income[]>(this.baseUrl);
  }

  getIncomeByDateRange(startDate: string, endDate: string): Observable<Income[]> {
    return this.http.get<Income[]>(`${this.baseUrl}?startDate=${startDate}&endDate=${endDate}`);
  }

  getIncomeById(id: number): Observable<Income> {
    return this.http.get<Income>(`${this.baseUrl}/${id}`);
  }

  createIncome(income: Income): Observable<Income> {
    return this.http.post<Income>(this.baseUrl, income);
  }

  updateIncome(id: number, income: Income): Observable<Income> {
    return this.http.put<Income>(`${this.baseUrl}/${id}`, income);
  }

  deleteIncome(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
