import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Liste des endpoints publics qui ne nécessitent pas d'authentification
  const publicEndpoints = [
    '/currencies',
    '/lta',
    '/auth/login',
    '/auth/register',
    '/public'
  ];

  // Vérifier si l'URL est un endpoint public
  const isPublicEndpoint = publicEndpoints.some(endpoint => 
    req.url.includes(endpoint)
  );

  // Get the auth token from the service
  const authToken = authService.getToken();

  // Clone the request and add the authorization header if token exists and not public endpoint
  let authReq = req;
  
  if (!isPublicEndpoint && authToken) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      }
    });
  } else if (!req.headers.has('Content-Type')) {
    authReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json'
      }
    });
  }

  // Send the cloned request with the header to the next handler
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle authentication errors
      if (error.status === 401) {
        // Token is invalid or expired
        authService.logout();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        // Access forbidden - only log for non-public endpoints
        if (!isPublicEndpoint) {
          console.error('Access forbidden:', error.message);
        }
      }
      
      return throwError(() => error);
    })
  );
};
