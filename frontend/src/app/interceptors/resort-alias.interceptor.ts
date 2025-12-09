import { HttpInterceptorFn } from '@angular/common/http';

export const resortAliasInterceptor: HttpInterceptorFn = (req, next) => {
  // Only add resortAlias to API requests (not external URLs like Plaid)
  if (req.url.includes('/api/')) {
    const resortAlias = 'testresort'; // TODO: Get from auth service or config
    
    // Add resortAlias as query parameter
    const modifiedReq = req.clone({
      params: req.params.set('resortAlias', resortAlias)
    });
    
    return next(modifiedReq);
  }
  
  return next(req);
};
