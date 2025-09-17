import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { LTA, LTARequest, LTAResponse, LTAListResponse, LTAStats, LTASearchParams } from '../models/lta.model';
import { ApiResponse, PagedResponse } from '../models/common.model';
import { LTAStatus, PaymentMode } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LTAService {
  private readonly apiUrl = `${environment.apiUrl}/api/lta`;

  constructor(private http: HttpClient) {}

  /**
   * Get all LTAs with pagination and filtering
   */
  getLTAs(searchParams: LTASearchParams = {}): Observable<LTAListResponse> {
    let params = new HttpParams()
      .set('page', (searchParams.page || 0).toString())
      .set('size', (searchParams.size || 20).toString());
    
    if (searchParams.sort) {
      params = params.set('sort', searchParams.sort);
    }
    if (searchParams.direction) {
      params = params.set('direction', searchParams.direction);
    }
    if (searchParams.status) {
      params = params.set('status', searchParams.status);
    }
    if (searchParams.originCityId) {
      params = params.set('originCityId', searchParams.originCityId.toString());
    }
    if (searchParams.destinationCityId) {
      params = params.set('destinationCityId', searchParams.destinationCityId.toString());
    }
    if (searchParams.clientId) {
      params = params.set('clientId', searchParams.clientId.toString());
    }
    if (searchParams.paymentMode) {
      params = params.set('paymentMode', searchParams.paymentMode);
    }
    if (searchParams.dateFrom) {
      params = params.set('dateFrom', searchParams.dateFrom);
    }
    if (searchParams.dateTo) {
      params = params.set('dateTo', searchParams.dateTo);
    }
    if (searchParams.search) {
      params = params.set('search', searchParams.search);
    }

    return this.http.get<LTAListResponse>(`${this.apiUrl}`, { params });
  }

  /**
   * Get LTA by ID
   */
  getLTAById(id: number): Observable<LTAResponse> {
    console.log('🌐 === APPEL API GET LTA BY ID ===');
    console.log('🌐 ID demandé:', id);
    console.log('🌐 URL complète:', `${this.apiUrl}/${id}`);
    
    return this.http.get<ApiResponse<LTAResponse>>(`${this.apiUrl}/${id}`)
      .pipe(
        map(response => {
          console.log('🌐 Réponse brute API:', response);
          console.log('🌐 Données LTA extraites:', response.data);
          console.log('🌐 Type de response.data:', typeof response.data);
          console.log('🌐 Structure response.data:', JSON.stringify(response.data, null, 2));
          return response.data;
        }),
        catchError(error => {
          console.error('🌐 Erreur dans getLTAById service:', error);
          throw error;
        })
      );
  }

  /**
   * Get LTA by LTA number
   */
  getLTAByNumber(ltaNumber: string): Observable<LTAResponse> {
    return this.http.get<ApiResponse<LTAResponse>>(`${this.apiUrl}/by-number/${ltaNumber}`)
      .pipe(map(response => response.data));
  }

  /**
   * Create new LTA
   */
  createLTA(lta: LTARequest): Observable<LTAResponse> {
    console.log('🌐 === APPEL API CRÉATION LTA ===');
    console.log('🌐 URL complète:', this.apiUrl);
    console.log('🌐 Données envoyées:', JSON.stringify(lta, null, 2));
    
    return this.http.post<LTAResponse>(this.apiUrl, lta);
  }

  /**
   * Get endpoint URL for debugging
   */
  getCreateEndpoint(): string {
    return this.apiUrl;
  }

  /**
   * Update existing LTA
   */
  updateLTA(id: number, lta: LTARequest): Observable<LTAResponse> {
    return this.http.put<LTAResponse>(`${this.apiUrl}/${id}`, lta);
  }

  /**
   * Delete LTA
   */
  deleteLTA(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Update LTA status
   */
  updateLTAStatus(id: number, status: LTAStatus): Observable<LTAResponse> {
    return this.http.patch<ApiResponse<LTAResponse>>(`${this.apiUrl}/${id}/status`, { status })
      .pipe(map(response => response.data));
  }

  /**
   * Calculer le coût d'une LTA
   */
  calculateCost(originCityId: number, destinationCityId: number, weight: number, currency?: string): Observable<number | null> {
    const params = {
      originCityId: originCityId.toString(),
      destinationCityId: destinationCityId.toString(),
      weight: weight.toString(),
      currency: currency || 'USD'
    };

    return this.http.get<{ cost: number }>(`${this.apiUrl}/calculate-cost`, { params })
      .pipe(
        map(response => response.cost),
        catchError(error => {
          console.error('Erreur lors du calcul du coût:', error);
          // Fallback: calcul local approximatif
          return of(this.calculateLocalCost(weight, currency || 'USD'));
        })
      );
  }

  /**
   * Calcul local approximatif en cas d'erreur backend
   */
  private calculateLocalCost(weight: number, currency: string): number {
    // Tarif de base par kg
    const baseRateUSD = 5; // $5 par kg
    const baseRateCDF = 13500; // 13500 FC par kg (équivalent à $5 au taux 2700)
    
    const baseRate = currency === 'CDF' ? baseRateCDF : baseRateUSD;
    return Math.round(weight * baseRate * 100) / 100;
  }

  /**
   * Get LTA statistics
   */
  getLTAStats(): Observable<LTAStats> {
    return this.http.get<LTAStats>(`${this.apiUrl}/stats`);
  }

  /**
   * Get LTAs by client
   */
  getLTAsByClient(clientId: number, page: number = 0, size: number = 20): Observable<LTAListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<LTAListResponse>(`${this.apiUrl}/by-client/${clientId}`, { params });
  }

  /**
   * Get recent LTAs
   */
  getRecentLTAs(limit: number = 10): Observable<LTAResponse[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<PagedResponse<LTAResponse>>(`${this.apiUrl}/recent`, { params })
      .pipe(map(response => response.content));
  }

  /**
   * Validate LTA business rules
   */
  validateLTA(lta: LTARequest): Observable<boolean> {
    return this.http.post<ApiResponse<boolean>>(`${this.apiUrl}/validate`, lta)
      .pipe(map(response => response.data));
  }

  /**
   * Check if LTA number is available
   */
  checkLTANumberAvailability(ltaNumber: string, excludeId?: number): Observable<boolean> {
    let params = new HttpParams().set('ltaNumber', ltaNumber);
    if (excludeId) {
      params = params.set('excludeId', excludeId.toString());
    }
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/check-lta-number`, { params })
      .pipe(map(response => response.data));
  }

  /**
   * Download LTA as PDF
   */
  downloadLTAPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, {
      responseType: 'blob'
    });
  }

  /**
   * Get LTA by tracking number for public tracking
   */
  getLTAByTrackingNumber(trackingNumber: string): Observable<LTA> {
    return this.http.get<ApiResponse<LTA>>(`${this.apiUrl}/tracking/${trackingNumber}`)
      .pipe(map(response => response.data));
  }
}
