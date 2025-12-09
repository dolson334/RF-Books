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
          <a routerLink="/reports" routerLinkActive="active" class="nav-link">
            <span class="icon">📊</span>
            <span>Reports</span>
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
      background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
      color: white;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .nav-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 2rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 72px;
    }

    .nav-brand h1 {
      margin: 0;
      font-size: 1.75rem;
      font-weight: 800;
      background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.5px;
    }

    .nav-links {
      display: flex;
      gap: 0.75rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.875rem 1.5rem;
      color: rgba(255, 255, 255, 0.7);
      text-decoration: none;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
    }

    .nav-link::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(96, 165, 250, 0.15), rgba(167, 139, 250, 0.15));
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 10px;
    }

    .nav-link:hover {
      color: white;
      transform: translateY(-2px);
    }

    .nav-link:hover::before {
      opacity: 1;
    }

    .nav-link.active {
      background: linear-gradient(135deg, rgba(96, 165, 250, 0.2), rgba(167, 139, 250, 0.2));
      color: white;
      box-shadow: 0 4px 12px rgba(96, 165, 250, 0.3);
    }

    .nav-link.active::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40%;
      height: 3px;
      background: linear-gradient(90deg, #60a5fa, #a78bfa);
      border-radius: 2px 2px 0 0;
    }

    .icon {
      font-size: 1.25rem;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
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
