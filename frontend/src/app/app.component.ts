import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastService } from './shared/toast.service';

@Component({
  selector: 'rf-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="app-container">
      <nav class="top-nav">
        <a routerLink="/dashboard" class="nav-brand">RF Books</a>
        <div class="nav-links">
          <a routerLink="/dashboard" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Dashboard</a>
          <a routerLink="/income" routerLinkActive="active">Income</a>
          <a routerLink="/expenses" routerLinkActive="active">Expenses</a>
          <a routerLink="/recon" routerLinkActive="active">Reconciliation</a>
          <a routerLink="/reports" routerLinkActive="active">Reports</a>
        </div>
      </nav>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
      <div class="toast-container">
        @for (toast of toastService.toasts(); track toast.id) {
          <div class="toast" [class]="'toast-' + toast.type" (click)="toastService.dismiss(toast.id)">
            {{ toast.message }}
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background: linear-gradient(135deg, #f0f9ff 0%, #f5f3ff 100%);
    }

    .top-nav {
      display: flex;
      align-items: center;
      gap: 2rem;
      padding: 0 2.5rem;
      height: 56px;
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid rgba(0, 0, 0, 0.06);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .nav-brand {
      font-weight: 900;
      font-size: 1.1rem;
      color: #1e293b;
      text-decoration: none;
      letter-spacing: -0.5px;
    }

    .nav-links {
      display: flex;
      gap: 0.25rem;
    }

    .nav-links a {
      padding: 0.4rem 0.85rem;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #6b7280;
      text-decoration: none;
      transition: all 0.15s ease;
    }

    .nav-links a:hover {
      color: #374151;
      background: rgba(59, 130, 246, 0.08);
    }

    .nav-links a.active {
      color: #2563eb;
      background: rgba(59, 130, 246, 0.1);
    }

    .main-content {
      flex: 1;
    }

    .toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column-reverse;
      gap: 0.5rem;
      max-width: 380px;
    }

    .toast {
      padding: 0.75rem 1rem;
      border-radius: 10px;
      font-size: 0.85rem;
      font-weight: 500;
      color: #fff;
      cursor: pointer;
      animation: slideIn 0.25s ease;
      box-shadow: 0 4px 16px rgba(0,0,0,0.15);
    }

    .toast-error { background: #ef4444; }
    .toast-success { background: #10b981; }
    .toast-info { background: #3b82f6; }

    @keyframes slideIn {
      from { opacity: 0; transform: translateX(30px); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class AppComponent {
  constructor(public toastService: ToastService) {}
}

