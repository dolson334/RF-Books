import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChartOfAccount, ProductServiceItem, OnboardingProgress } from './onboarding.models';

@Injectable({
  providedIn: 'root',
})
export class OnboardingService {
  private readonly baseUrl = '/api/onboarding';

  constructor(private http: HttpClient) {}

  getChartOfAccounts(): Observable<ChartOfAccount[]> {
    return this.http.get<ChartOfAccount[]>(`${this.baseUrl}/chart-of-accounts`);
  }

  saveChartOfAccounts(accounts: ChartOfAccount[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/chart-of-accounts`, accounts);
  }

  getProductsServices(): Observable<ProductServiceItem[]> {
    return this.http.get<ProductServiceItem[]>(`${this.baseUrl}/products-services`);
  }

  saveProductsServices(items: ProductServiceItem[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/products-services`, items);
  }

  getProgress(): Observable<OnboardingProgress> {
    return this.http.get<OnboardingProgress>(`${this.baseUrl}/progress`);
  }

  completeOnboarding(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/complete`, {});
  }
}
