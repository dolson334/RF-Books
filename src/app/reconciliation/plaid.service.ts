import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LinkTokenResponse {
  link_token: string;
}

export interface ExchangePublicTokenRequest {
  publicToken: string;
  institutionName?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PlaidService {
  private readonly baseUrl = '/api/plaid';

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
}
