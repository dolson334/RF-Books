import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FinancialReport, ReportFilter } from './report.models';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly baseUrl = 'http://localhost:8081/api/reports';

  constructor(private http: HttpClient) {}

  getFinancialReport(filter: ReportFilter): Observable<FinancialReport> {
    let params: any = { period: filter.period };
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;
    
    return this.http.get<FinancialReport>(`${this.baseUrl}/financial`, { params });
  }
}
