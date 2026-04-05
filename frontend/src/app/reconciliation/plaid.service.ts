import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlaidTransaction } from './reconciliation.models';

export interface LinkTokenResponse {
  link_token: string;
}

export interface ExchangePublicTokenRequest {
  publicToken: string;
  institutionName?: string;
}

export interface ConnectionStatus {
  connected: boolean;
}

export interface BalanceResponse {
  balance: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class PlaidService {
  private readonly baseUrl = '/api/plaid';

  constructor(private http: HttpClient) {}

  createLinkToken(): Observable<LinkTokenResponse> {
    return this.http.post<LinkTokenResponse>(`${this.baseUrl}/link-token`, {});
  }

  exchangePublicToken(req: ExchangePublicTokenRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/exchange`, req);
  }

  getTransactions(startDate: string, endDate: string): Observable<PlaidTransaction[]> {
    return this.http.post<PlaidTransaction[]>(`${this.baseUrl}/transactions`, {
      startDate,
      endDate
    });
  }

  syncTransactions(): Observable<PlaidTransaction[]> {
    return this.http.post<PlaidTransaction[]>(`${this.baseUrl}/sync`, {});
  }

  getBalance(): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.baseUrl}/balance`);
  }

  getConnectionStatus(): Observable<ConnectionStatus> {
    return this.http.get<ConnectionStatus>(`${this.baseUrl}/status`);
  }

  disconnectBank(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/disconnect`);
  }
}
