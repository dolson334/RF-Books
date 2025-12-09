import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PLLineItem {
  name: string;
  amount: number;
  percentage?: number;
  isCategory?: boolean;
  isSubtotal?: boolean;
  isTotal?: boolean;
  children?: PLLineItem[];
  expanded?: boolean;
  transactions?: Transaction[];
  showTransactions?: boolean;
}

export interface Transaction {
  date: string;
  description: string;
  amount: number;
  category?: string;
  vendor?: string;
}

export interface ProfitLossReport {
  lineItems: PLLineItem[];
  summary: {
    totalRevenue: number;
    totalExpenses: number;
    netProfit: number;
    profitMargin: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class ProfitLossService {
  private apiUrl = 'http://localhost:8081/api/reports/profit-loss';

  constructor(private http: HttpClient) {}

  getProfitLossReport(startDate: string, endDate: string): Observable<ProfitLossReport> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    
    return this.http.get<ProfitLossReport>(this.apiUrl, { params });
  }
}
