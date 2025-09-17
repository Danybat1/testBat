import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LTA, LTARequest, LTAResponse, LTAListResponse, LTAStats } from '../../models/lta.model';
import { LTAStatus } from '../../models/common.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LTAService {
  private readonly API_URL = `${environment.apiUrl}/api/lta`;

  constructor(private http: HttpClient) {}

  /**
   * Get all LTAs with pagination and filtering
   * Can be called with individual parameters or an object parameter
   */
  getLTAs(
    pageOrParams?: number | {
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: string;
      status?: LTAStatus | string;
      clientId?: number;
      shipper?: string;
      consignee?: string;
    },
    size?: number,
    sortBy?: string,
    sortDir?: string,
    status?: LTAStatus,
    shipper?: string,
    consignee?: string
  ): Observable<LTAListResponse> {
    let httpParams = new HttpParams();
    
    if (typeof pageOrParams === 'object' && pageOrParams !== null) {
      // Object parameters
      const params = pageOrParams;
      httpParams = httpParams
        .set('page', (params.page || 0).toString())
        .set('size', (params.size || 20).toString())
        .set('sortBy', params.sortBy || 'createdAt')
        .set('sortDir', params.sortDir || 'desc');

      if (params.status) {
        httpParams = httpParams.set('status', params.status.toString());
      }
      if (params.clientId) {
        httpParams = httpParams.set('clientId', params.clientId.toString());
      }
      if (params.shipper) {
        httpParams = httpParams.set('shipper', params.shipper);
      }
      if (params.consignee) {
        httpParams = httpParams.set('consignee', params.consignee);
      }
    } else {
      // Individual parameters
      httpParams = httpParams
        .set('page', (pageOrParams || 0).toString())
        .set('size', (size || 20).toString())
        .set('sortBy', sortBy || 'createdAt')
        .set('sortDir', sortDir || 'desc');

      if (status) {
        httpParams = httpParams.set('status', status.toString());
      }
      if (shipper) {
        httpParams = httpParams.set('shipper', shipper);
      }
      if (consignee) {
        httpParams = httpParams.set('consignee', consignee);
      }
    }

    return this.http.get<LTAListResponse>(`${this.API_URL}`, { params: httpParams });
  }

  /**
   * Get LTA by ID
   */
  getLTAById(id: number): Observable<LTA> {
    return this.http.get<LTA>(`${this.API_URL}/${id}`);
  }

  /**
   * Get LTA by LTA number
   */
  getLTAByNumber(ltaNumber: string): Observable<LTA> {
    return this.http.get<LTA>(`${this.API_URL}/number/${ltaNumber}`);
  }

  /**
   * Get LTA by tracking number (public endpoint)
   */
  getLTAByTrackingNumber(trackingNumber: string): Observable<LTA> {
    return this.http.get<LTA>(`${this.API_URL}/tracking/${trackingNumber}`);
  }

  /**
   * Create new LTA
   */
  createLTA(lta: LTARequest): Observable<LTAResponse> {
    return this.http.post<LTAResponse>(`${this.API_URL}`, lta);
  }

  /**
   * Update LTA
   */
  updateLTA(id: number, lta: Partial<LTA>): Observable<LTA> {
    return this.http.put<LTA>(`${this.API_URL}/${id}`, lta);
  }

  /**
   * Update LTA status
   */
  updateLTAStatus(id: number, status: LTAStatus): Observable<LTA> {
    return this.http.patch<LTA>(`${this.API_URL}/${id}/status`, { status });
  }

  /**
   * Delete LTA
   */
  deleteLTA(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Get LTA statistics
   */
  getLTAStats(): Observable<LTAStats> {
    return this.http.get<LTAStats>(`${this.API_URL}/stats`);
  }

  /**
   * Get LTA status options for dropdowns
   */
  getLTAStatusOptions(): { value: LTAStatus; label: string }[] {
    return [
      { value: LTAStatus.DRAFT, label: 'Brouillon' },
      { value: LTAStatus.CONFIRMED, label: 'Confirmé' },
      { value: LTAStatus.IN_TRANSIT, label: 'En transit' },
      { value: LTAStatus.DELIVERED, label: 'Livré' },
      { value: LTAStatus.CANCELLED, label: 'Annulé' }
    ];
  }

  /**
   * Get recent LTAs
   */
  getRecentLTAs(limit: number = 10): Observable<LTA[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<{ content: LTA[] }>(`${this.API_URL}/recent`, { params })
      .pipe(map((response: { content: LTA[] }) => response.content));
  }

  /**
   * Get status color for UI display
   */
  getStatusColor(status: LTAStatus): string {
    const colors: { [key in LTAStatus]: string } = {
      [LTAStatus.DRAFT]: 'secondary',
      [LTAStatus.CONFIRMED]: 'info',
      [LTAStatus.IN_TRANSIT]: 'primary',
      [LTAStatus.DELIVERED]: 'success',
      [LTAStatus.CANCELLED]: 'danger'
    };
    return colors[status] || 'secondary';
  }

  /**
   * Download LTA PDF
   */
  downloadLTAPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf`, {
      responseType: 'blob'
    });
  }

  /**
   * Generate QR code data for LTA tracking
   */
  generateQRCodeData(trackingNumber: string): string {
    const trackingUrl = `${window.location.origin}/track/${trackingNumber}`;
    return trackingUrl;
  }

  /**
   * Get public tracking URL for LTA
   */
  getTrackingUrl(trackingNumber: string): string {
    return `${window.location.origin}/track/${trackingNumber}`;
  }
}
