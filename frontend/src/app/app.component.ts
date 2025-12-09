import { Component } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { NavigationComponent } from './navigation/navigation.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'rf-root',
  standalone: true,
  imports: [RouterOutlet, NavigationComponent, CommonModule],
  template: `
    <div class="app-container">
      <rf-navigation *ngIf="showNavigation()"></rf-navigation>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .main-content {
      flex: 1;
      background-color: #f9fafb;
    }
  `]
})
export class AppComponent {
  constructor(private router: Router) {}

  showNavigation(): boolean {
    // Hide navigation on onboarding page
    const url = this.router.url;
    return !url.includes('/onboarding') || url.includes('/settings');
  }
}

