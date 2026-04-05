import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'rf-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="app-container">
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
      background: linear-gradient(135deg, #f0f9ff 0%, #f5f3ff 100%);
    }

    .main-content {
      flex: 1;
    }
  `]
})
export class AppComponent {}

