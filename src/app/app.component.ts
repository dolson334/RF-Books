import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'rf-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="app-shell">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [
    `
      .app-shell {
        min-height: 100vh;
        margin: 0;
      }
    `,
  ],
})
export class AppComponent {}
