import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private token = signal<string | null>(null);
  private resortAlias = signal<string>('testresort');

  constructor() {
    this.initFromUrl();
    this.listenForMessages();
  }

  private initFromUrl(): void {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const alias = params.get('resortAlias');

    if (token) {
      this.token.set(token);
    }
    if (alias) {
      this.resortAlias.set(alias);
    }
  }

  private listenForMessages(): void {
    window.addEventListener('message', (event: MessageEvent) => {
      if (event.data?.type === 'rf-books-config') {
        if (event.data.token) {
          this.token.set(event.data.token);
        }
        if (event.data.resortAlias) {
          this.resortAlias.set(event.data.resortAlias);
        }
      }
    });
  }

  getToken(): string | null {
    return this.token();
  }

  getResortAlias(): string {
    return this.resortAlias();
  }

  isAuthenticated(): boolean {
    return this.token() !== null;
  }

  isEmbedded(): boolean {
    try {
      return window.self !== window.top;
    } catch {
      return true;
    }
  }
}
