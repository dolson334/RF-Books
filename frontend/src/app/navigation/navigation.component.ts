import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'rf-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="main-nav">
      <div class="nav-container">
        <div class="nav-brand">
          <h1>RF Books</h1>
        </div>
        <div class="nav-links">
          <a routerLink="/recon" routerLinkActive="active" class="nav-link">
            <span class="icon">💰</span>
            <span>Reconciliation</span>
          </a>
          <a routerLink="/expenses" routerLinkActive="active" class="nav-link">
            <span class="icon">📝</span>
            <span>Expenses</span>
          </a>
          <a routerLink="/settings" class="nav-link">
            <span class="icon">⚙️</span>
            <span>Settings</span>
          </a>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .main-nav {
      background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
      color: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .nav-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 2rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64px;
    }

    .nav-brand h1 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 700;
      color: white;
    }

    .nav-links {
      display: flex;
      gap: 0.5rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.25rem;
      color: rgba(255, 255, 255, 0.9);
      text-decoration: none;
      border-radius: 6px;
      font-weight: 500;
      transition: all 0.2s;
    }

    .nav-link:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }

    .nav-link.active {
      background: rgba(255, 255, 255, 0.2);
      color: white;
    }

    .icon {
      font-size: 1.2rem;
    }

    @media (max-width: 768px) {
      .nav-container {
        flex-direction: column;
        height: auto;
        padding: 1rem;
        gap: 1rem;
      }

      .nav-links {
        width: 100%;
        flex-direction: column;
      }

      .nav-link {
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class NavigationComponent {}
