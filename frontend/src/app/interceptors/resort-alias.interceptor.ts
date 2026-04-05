import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const resortAliasInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/')) {
    const authService = inject(AuthService);
    const resortAlias = authService.getResortAlias();
    const token = authService.getToken();

    let modifiedReq = req.clone({
      params: req.params.set('resortAlias', resortAlias)
    });

    if (token) {
      modifiedReq = modifiedReq.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next(modifiedReq);
  }

  return next(req);
};
