import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'rf-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="main-nav">
      <div class="nav-container">
        <div class="nav-brand" (click)="goHome()">
          <div class="logo-icon">💼</div>
          <div class="brand-text">
            <span class="brand-name">RF Books</span>
            <span class="brand-tagline">Financial Intelligence</span>
          </div>
        </div>
        <div class="nav-links">
          <a routerLink="/recon" routerLinkActive="active" class="nav-link" data-tooltip="Reconciliation">
            <span class="icon">🔄</span>
            <span class="link-text">Reconciliation</span>
            <span class="link-indicator"></span>
          </a>
          <a routerLink="/income" routerLinkActive="active" class="nav-link" data-tooltip="Income Tracking">
            <span class="icon">💰</span>
            <span class="link-text">Income</span>
            <span class="link-indicator"></span>
          </a>
          <a routerLink="/expenses" routerLinkActive="active" class="nav-link" data-tooltip="Expense Management">
            <span class="icon">💸</span>
            <span class="link-text">Expenses</span>
            <span class="link-indicator"></span>
          </a>
          <a routerLink="/reports" routerLinkActive="active" class="nav-link" data-tooltip="Analytics & Reports">
            <span class="icon">📊</span>
            <span class="link-text">Reports</span>
            <span class="link-indicator"></span>
          </a>
        </div>
        <div class="nav-actions">
          <button class="nav-btn" (click)="openSearch()" data-tooltip="Search">
            <span class="icon">🔍</span>
          </button>
          <button class="nav-btn" (click)="toggleNotifications()" data-tooltip="Notifications">
            <span class="icon">🔔</span>
            @if (hasNotifications()) {
              <span class="notification-badge">{{ notificationCount() }}</span>
            }
          </button>
          <a routerLink="/settings" class="nav-btn settings-btn" data-tooltip="Settings">
            <span class="icon">⚙️</span>
          </a>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .main-nav {
      background: rgba(15, 23, 42, 0.95);
      backdrop-filter: blur(20px) saturate(180%);
      -webkit-backdrop-filter: blur(20px) saturate(180%);
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .nav-container {
      max-width: 1600px;
      margin: 0 auto;
      padding: 0 2rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 80px;
      gap: 2rem;
    }

    .nav-brand {
      display: flex;
      align-items: center;
      gap: 1rem;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .nav-brand:hover {
      transform: scale(1.02);
    }

    .logo-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 50%, #ec4899 100%);
      border-radius: 14px;
      font-size: 1.5rem;
      box-shadow: 0 4px 20px rgba(96, 165, 250, 0.4);
      animation: pulse 2s ease-in-out infinite;
    }

    @keyframes pulse {
      0%, 100% { box-shadow: 0 4px 20px rgba(96, 165, 250, 0.4); }
      50% { box-shadow: 0 4px 30px rgba(167, 139, 250, 0.6); }
    }

    .brand-text {
      display: flex;
      flex-direction: column;
      gap: 0.125rem;
    }

    .brand-name {
      font-size: 1.5rem;
      font-weight: 800;
      background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 50%, #ec4899 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.5px;
      line-height: 1;
    }

    .brand-tagline {
      font-size: 0.65rem;
      color: rgba(255, 255, 255, 0.5);
      font-weight: 600;
      letter-spacing: 0.5px;
      text-transform: uppercase;
    }

    .nav-links {
      display: flex;
      gap: 0.5rem;
      flex: 1;
      justify-content: center;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.875rem 1.5rem;
      color: rgba(255, 255, 255, 0.65);
      text-decoration: none;
      border-radius: 12px;
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
      background: linear-gradient(135deg, rgba(96, 165, 250, 0.1), rgba(167, 139, 250, 0.1));
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .nav-link:hover::before {
      opacity: 1;
    }

    .nav-link:hover {
      color: rgba(255, 255, 255, 0.95);
      transform: translateY(-2px);
    }

    .nav-link .icon {
      font-size: 1.25rem;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
      transition: transform 0.3s ease;
    }

    .nav-link:hover .icon {
      transform: scale(1.1);
    }

    .link-text {
      position: relative;
    }

    .link-indicator {
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%) scaleX(0);
      width: 80%;
      height: 3px;
      background: linear-gradient(90deg, #60a5fa, #a78bfa);
      border-radius: 999px;
      transition: transform 0.3s ease;
    }

    .nav-link.active {
      color: white;
      background: rgba(96, 165, 250, 0.15);
      box-shadow: 0 4px 12px rgba(96, 165, 250, 0.2);
    }

    .nav-link.active .link-indicator {
      transform: translateX(-50%) scaleX(1);
    }

    .nav-actions {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }

    .nav-btn {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
      text-decoration: none;
    }

    .nav-btn:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
      transform: translateY(-2px);
    }

    .nav-btn .icon {
      font-size: 1.25rem;
    }

    .notification-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      min-width: 18px;
      height: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #ef4444, #dc2626);
      color: white;
      font-size: 0.65rem;
      font-weight: 700;
      border-radius: 999px;
      padding: 0 4px;
      box-shadow: 0 2px 8px rgba(239, 68, 68, 0.5);
      animation: bounce 2s ease-in-out infinite;
    }

    @keyframes bounce {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-4px); }
    }

    @media (max-width: 1024px) {
      .link-text {
        display: none;
      }

      .nav-links {
        gap: 0.25rem;
      }

      .nav-link {
        padding: 0.75rem;
      }

      .brand-tagline {
        display: none;
      }
    }

    @media (max-width: 768px) {
      .nav-container {
        padding: 0 1rem;
      }

      .logo-icon {
        width: 40px;
        height: 40px;
        font-size: 1.25rem;
      }

      .brand-name {
        font-size: 1.25rem;
      }
    }
  `]
})
export class NavigationComponent {
  hasNotifications = signal(true);
  notificationCount = signal(3);

  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/recon']);
  }

  openSearch() {
    console.log('Search opened');
    // Implement search functionality
  }

  toggleNotifications() {
    console.log('Notifications toggled');
    // Implement notifications panel
  }
}
