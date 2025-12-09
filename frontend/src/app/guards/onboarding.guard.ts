import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { OnboardingService } from '../onboarding/onboarding.service';
import { map, catchError, of } from 'rxjs';

export const onboardingGuard = () => {
  const router = inject(Router);
  const onboardingService = inject(OnboardingService);

  return onboardingService.getProgress().pipe(
    map(progress => {
      const isComplete = progress.chartOfAccountsCreated && progress.productsServicesCreated;
      
      if (!isComplete) {
        // Store incomplete status for UI display
        localStorage.setItem('rfbooks_onboarding_incomplete', 'true');
        sessionStorage.setItem('rfbooks_missing_config', JSON.stringify({
          chartOfAccounts: !progress.chartOfAccountsCreated,
          productsServices: !progress.productsServicesCreated,
          bank: !progress.bankConnected
        }));
        
        // Allow access but flag for warning
        return true;
      }
      
      localStorage.setItem('rfbooks_onboarding_incomplete', 'false');
      return true;
    }),
    catchError(() => {
      // If backend fails, allow access but flag for warning
      localStorage.setItem('rfbooks_onboarding_incomplete', 'true');
      return of(true);
    })
  );
};
