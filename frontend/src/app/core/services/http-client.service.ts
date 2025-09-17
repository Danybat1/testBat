import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  timestamp?: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class HttpClientService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * GET request générique
   */
  get<T>(endpoint: string, params?: HttpParams): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.get<T>(url, { params })
      .pipe(
        retry(1),
        catchError(this.handleError)
      );
  }

  /**
   * POST request générique
   */
  post<T>(endpoint: string, data: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.post<T>(url, data, this.getHttpOptions())
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * PUT request générique
   */
  put<T>(endpoint: string, data: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.put<T>(url, data, this.getHttpOptions())
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * PATCH request générique
   */
  patch<T>(endpoint: string, data: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.patch<T>(url, data, this.getHttpOptions())
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * DELETE request générique
   */
  delete<T>(endpoint: string): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.delete<T>(url)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Download file (PDF, Excel, etc.)
   */
  downloadFile(endpoint: string, filename?: string): Observable<Blob> {
    const url = this.buildUrl(endpoint);
    return this.http.get(url, { responseType: 'blob' })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * GET avec pagination
   */
  getWithPagination<T>(
    endpoint: string, 
    page: number = 0, 
    size: number = 10, 
    sort?: string,
    additionalParams?: { [key: string]: string }
  ): Observable<PagedResponse<T>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (additionalParams) {
      Object.keys(additionalParams).forEach(key => {
        params = params.set(key, additionalParams[key]);
      });
    }

    return this.get<PagedResponse<T>>(endpoint, params);
  }

  /**
   * Construire l'URL complète
   */
  private buildUrl(endpoint: string): string {
    // Assurer que l'endpoint commence par /api si ce n'est pas déjà le cas
    if (!endpoint.startsWith('/api') && !endpoint.startsWith('http')) {
      endpoint = `/api${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    }
    
    return `${this.baseUrl}${endpoint}`;
  }

  /**
   * Options HTTP par défaut
   */
  private getHttpOptions() {
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };
  }

  /**
   * Gestion centralisée des erreurs
   */
  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'Une erreur est survenue';

    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Requête invalide';
          break;
        case 401:
          errorMessage = 'Non autorisé - Veuillez vous connecter';
          break;
        case 403:
          errorMessage = 'Accès interdit';
          break;
        case 404:
          errorMessage = `Ressource non trouvée: ${error.url}`;
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur';
          break;
        case 503:
          errorMessage = 'Service temporairement indisponible';
          break;
        default:
          errorMessage = `Erreur ${error.status}: ${error.error?.message || error.message}`;
      }
    }

    console.error('🚨 Erreur HTTP:', {
      status: error.status,
      message: errorMessage,
      url: error.url,
      error: error.error
    });

    return throwError(() => new Error(errorMessage));
  };
}
