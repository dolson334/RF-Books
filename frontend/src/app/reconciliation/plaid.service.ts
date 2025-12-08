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

export interface TransactionsRequest {
  startDate: string;
  endDate: string;
}

export interface ConnectionStatus {
  connected: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class PlaidService {
  private readonly baseUrl = 'http://localhost:8081/api/plaid';

  constructor(private http: HttpClient) {}

  /**
   * Ask backend to create a Plaid Link token.
   */
  createLinkToken(): Observable<LinkTokenResponse> {
    return this.http.post<LinkTokenResponse>(`${this.baseUrl}/link-token`, {});
  }

  /**
   * Exchange the public_token from Plaid Link for an access token on the backend.
   */
  exchangePublicToken(req: ExchangePublicTokenRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/exchange`, req);
  }

  /**
   * Get transactions from Plaid for the connected bank account.
   */
  getTransactions(startDate: string, endDate: string): Observable<PlaidTransaction[]> {
    return this.http.post<PlaidTransaction[]>(`${this.baseUrl}/transactions`, {
      startDate,
      endDate
    });
  }

  /**
   * Check if backend has an active Plaid connection.
   */
  getConnectionStatus(): Observable<ConnectionStatus> {
    return this.http.get<ConnectionStatus>(`${this.baseUrl}/status`);
  }

  /**
   * Disconnect the bank account.
   */
  disconnectBank(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/disconnect`);
  }
}
