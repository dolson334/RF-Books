import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../shared/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError(error => {
      if (error.status === 401) {
        toast.error('Session expired. Please sign in again.');
      } else if (error.status === 403) {
        toast.error('Access denied. You do not have permission for this action.');
      } else if (error.status >= 500) {
        toast.error('Server error. Please try again later.');
      }
      return throwError(() => error);
    })
  );
};
