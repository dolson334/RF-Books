import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChartOfAccount, ProductService, OnboardingProgress } from './onboarding.models';

@Injectable({
  providedIn: 'root',
})
export class OnboardingService {
  private readonly baseUrl = 'http://localhost:8081/api/onboarding';

  constructor(private http: HttpClient) {}

  // Chart of Accounts
  saveChartOfAccounts(accounts: ChartOfAccount[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/chart-of-accounts`, accounts);
  }

  getChartOfAccounts(): Observable<ChartOfAccount[]> {
    return this.http.get<ChartOfAccount[]>(`${this.baseUrl}/chart-of-accounts`);
  }

  // Products & Services
  saveProductsServices(items: ProductService[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/products-services`, items);
  }

  getProductsServices(): Observable<ProductService[]> {
    return this.http.get<ProductService[]>(`${this.baseUrl}/products-services`);
  }

  // Progress
  getProgress(): Observable<OnboardingProgress> {
    return this.http.get<OnboardingProgress>(`${this.baseUrl}/progress`);
  }

  completeOnboarding(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/complete`, {});
  }
}
