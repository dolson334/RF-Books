import { createApplication } from '@angular/platform-browser';
import { createCustomElement } from '@angular/elements';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ReconciliationComponent } from './app/reconciliation/reconciliation.component';
import { routes } from './app/app.routes';
import { resortAliasInterceptor } from './app/interceptors/resort-alias.interceptor';

(async () => {
  const app = await createApplication({
    providers: [
      provideRouter(routes),
      provideHttpClient(withInterceptors([resortAliasInterceptor])),
    ],
  });

  const ReconElement = createCustomElement(ReconciliationComponent, {
    injector: app.injector,
  });

  customElements.define('rf-books-recon', ReconElement);
})();
