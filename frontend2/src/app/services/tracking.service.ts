import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { PublicTrackingResponse } from '../models/public-tracking-response.model';

@Injectable({
  providedIn: 'root'
})
export class TrackingService {
  private readonly apiUrl = environment.apiUrl;
  private readonly timeoutMs = 30000; // 30 seconds timeout

  constructor(private http: HttpClient) {}

  trackShipment(trackingNumber: string): Observable<ApiResponse<PublicTrackingResponse>> {
    if (!trackingNumber || trackingNumber.trim().length === 0) {
      return throwError(() => new Error('Numéro de suivi requis'));
    }

    const cleanTrackingNumber = trackingNumber.trim();
    const url = `${this.apiUrl}/api/public/tracking/${encodeURIComponent(cleanTrackingNumber)}`;

    return this.http.get<ApiResponse<PublicTrackingResponse>>(url).pipe(
      timeout(this.timeoutMs),
      catchError(this.handleError)
    );
  }

  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'Une erreur est survenue';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = 'Erreur de connexion. Vérifiez votre connexion internet.';
    } else {
      // Server-side error
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.';
          break;
        case 404:
          errorMessage = 'Aucun envoi trouvé avec ce numéro de suivi.';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur. Veuillez réessayer plus tard.';
          break;
        case 503:
          errorMessage = 'Service temporairement indisponible. Veuillez réessayer.';
          break;
        default:
          errorMessage = `Erreur ${error.status}: ${error.message}`;
      }
    }

    console.error('TrackingService Error:', error);
    return throwError(() => new Error(errorMessage));
  };
}
