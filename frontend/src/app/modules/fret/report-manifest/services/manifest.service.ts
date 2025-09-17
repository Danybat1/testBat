import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';

export interface ManifestItem {
  id?: number;
  lineNumber?: number;
  trackingNumber: string;
  description: string;
  packagingType: string;
  packageCount: number;
  grossWeight: number;
  volume: number;
  volumetricWeight: number;
  declaredValue: number;
  containerNumber?: string;
  remarks?: string;
}

export interface Manifest {
  id?: number;
  manifestNumber?: string;
  proformaNumber: string;
  transportMode: string;
  vehicleReference: string;
  driverName: string;
  driverPhone: string;
  scheduledDeparture: Date;
  scheduledArrival: Date;
  
  // Shipper
  shipperName: string;
  shipperAddress: string;
  shipperContact: string;
  shipperPhone: string;
  
  // Consignee
  consigneeName: string;
  consigneeAddress: string;
  consigneeContact: string;
  consigneePhone: string;
  
  // Client
  clientName: string;
  clientReference: string;
  clientContact: string;
  clientPhone: string;
  
  // Agent
  agentName: string;
  agentAddress: string;
  agentContact: string;
  agentPhone: string;
  
  // Instructions
  deliveryInstructions?: string;
  generalRemarks?: string;
  attachments?: string;
  
  // Totals
  totalPackages?: number;
  totalWeight?: number;
  totalVolume?: number;
  totalVolumetricWeight?: number;
  totalValue?: number;
  
  // Signatures
  loadingSignature?: string;
  loadingSignatory?: string;
  loadingSignatureDate?: Date;
  deliverySignature?: string;
  deliverySignatory?: string;
  deliverySignatureDate?: Date;
  deliveryRemarks?: string;
  
  // Metadata
  qrCode?: string;
  status?: string;
  createdAt?: Date;
  updatedAt?: Date;
  
  items?: ManifestItem[];
}

export interface ManifestRequest {
  proformaNumber: string;
  transportMode: string;
  vehicleReference: string;
  driverName: string;
  driverPhone: string;
  scheduledDeparture: string;
  scheduledArrival: string;
  
  shipperName: string;
  shipperAddress: string;
  shipperContact: string;
  shipperPhone: string;
  
  consigneeName: string;
  consigneeAddress: string;
  consigneeContact: string;
  consigneePhone: string;
  
  clientName: string;
  clientReference: string;
  clientContact: string;
  clientPhone: string;
  
  agentName: string;
  agentAddress: string;
  agentContact: string;
  agentPhone: string;
  
  deliveryInstructions?: string;
  generalRemarks?: string;
  attachments?: string;
  
  items: {
    trackingNumber: string;
    description: string;
    packagingType: string;
    packageCount: number;
    grossWeight: number;
    volume: number;
    volumetricWeight: number;
    declaredValue: number;
    containerNumber?: string;
    remarks?: string;
  }[];
}

export interface SignatureRequest {
  signatureData: string;
  signatoryName: string;
  remarks?: string;
}

export interface ManifestCreateRequest {
  proformaNumber: string;
  transportMode: string;
  vehicleInfo: string;
  driverName: string;
  departureDate?: Date;
  arrivalDate?: Date;
  parties: PartyRequest[];
  goods: GoodsItemRequest[];
  deliveryInstructions?: string;
  remarks?: string;
  attachments?: string;
}

export interface PartyRequest {
  partyType: string; // SHIPPER, CONSIGNEE, CLIENT, AGENT
  companyName: string;
  contactName?: string;
  address?: string;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  taxId?: string;
}

export interface GoodsItemRequest {
  trackingNumber?: string;
  description: string;
  packaging?: string;
  packageCount: number;
  weight: number;
  volume: number;
  value: number;
  currency?: string;
  origin?: string;
  destination?: string;
  specialInstructions?: string;
  handlingCode?: string;
}

export interface ManifestResponse {
  id: number;
  manifestNumber: string;
  proformaNumber: string;
  transportMode: string;
  vehicleInfo: string;
  driverName: string;
  departureDate?: Date;
  arrivalDate?: Date;
  parties: PartyResponse[];
  goods: GoodsItemResponse[];
  deliveryInstructions?: string;
  remarks?: string;
  attachments?: string;
  status: string;
  createdDate: Date;
  qrCode?: string;
  loadingSignature?: string;
  loadingSignatory?: string;
  loadingSignatureDate?: Date;
  deliverySignature?: string;
  deliverySignatory?: string;
  deliverySignatureDate?: Date;
  deliveryRemarks?: string;
}

export interface PartyResponse {
  partyType: string; // SHIPPER, CONSIGNEE, CLIENT, AGENT
  companyName: string;
  contactName?: string;
  address?: string;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  taxId?: string;
}

export interface GoodsItemResponse {
  trackingNumber?: string;
  description: string;
  packaging?: string;
  packageCount: number;
  weight: number;
  volume: number;
  value: number;
  currency?: string;
  origin?: string;
  destination?: string;
  specialInstructions?: string;
  handlingCode?: string;
}

export interface ManifestListResponse {
  content: ManifestResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ManifestService {
  private readonly API_URL = `${environment.apiUrl}/api/fret/manifests`;

  constructor(private http: HttpClient) {}

  /**
   * Créer un nouveau manifeste
   */
  createManifest(manifest: ManifestCreateRequest): Observable<ManifestResponse> {
    return this.http.post<ManifestResponse>(this.API_URL, manifest);
  }

  /**
   * Récupérer tous les manifestes
   */
  getAllManifests(): Observable<ManifestResponse[]> {
    // Utiliser la méthode paginée avec une grande taille pour récupérer tous les manifestes
    return this.getManifests(0, 1000).pipe(
      map(response => response.content || [])
    );
  }

  /**
   * Récupérer un manifeste par ID
   */
  getManifestById(id: number): Observable<ManifestResponse> {
    return this.http.get<ManifestResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Générer PDF d'un manifeste
   */
  generatePDF(manifestId: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${manifestId}/pdf`, {
      responseType: 'blob'
    });
  }

  /**
   * Générer document Word d'un manifeste
   */
  generateWord(manifestId: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${manifestId}/word`, {
      responseType: 'blob'
    });
  }

  /**
   * Récupérer tous les manifestes avec pagination
   */
  getManifests(page: number = 0, size: number = 10, sort: string = 'createdDate,desc'): Observable<ManifestListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<ManifestListResponse>(this.API_URL, { params });
  }

  /**
   * Récupérer un manifeste par numéro
   */
  getManifestByNumber(manifestNumber: string): Observable<ManifestResponse> {
    return this.http.get<ManifestResponse>(`${this.API_URL}/number/${manifestNumber}`);
  }

  /**
   * Mettre à jour un manifeste
   */
  updateManifest(id: number, manifest: ManifestCreateRequest): Observable<ManifestResponse> {
    return this.http.put<ManifestResponse>(`${this.API_URL}/${id}`, manifest);
  }

  /**
   * Mettre à jour le statut d'un manifeste
   */
  updateManifestStatus(id: number, status: string): Observable<ManifestResponse> {
    return this.http.patch<ManifestResponse>(`${this.API_URL}/${id}/status`, { status });
  }

  /**
   * Supprimer un manifeste
   */
  deleteManifest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Rechercher des manifestes
   */
  searchManifests(
    query: string, 
    page: number = 0, 
    size: number = 10
  ): Observable<ManifestListResponse> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ManifestListResponse>(`${this.API_URL}/search`, { params });
  }

  /**
   * Télécharger le PDF d'un manifeste
   */
  downloadManifestPdf(id: number, manifestNumber: string): void {
    this.generatePDF(id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `Manifeste_${manifestNumber}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Erreur lors du téléchargement du PDF:', error);
      }
    });
  }

  /**
   * Ajouter une signature de chargement
   */
  addLoadingSignature(id: number, signatureData: {
    signature: string;
    signatory: string;
    remarks?: string;
  }): Observable<ManifestResponse> {
    return this.http.post<ManifestResponse>(`${this.API_URL}/${id}/loading-signature`, signatureData);
  }

  /**
   * Ajouter une signature de livraison
   */
  addDeliverySignature(id: number, signatureData: {
    signature: string;
    signatory: string;
    remarks?: string;
  }): Observable<ManifestResponse> {
    return this.http.post<ManifestResponse>(`${this.API_URL}/${id}/delivery-signature`, signatureData);
  }

  /**
   * Récupérer les manifestes par statut
   */
  getManifestsByStatus(status: string, page: number = 0, size: number = 10): Observable<ManifestListResponse> {
    const params = new HttpParams()
      .set('status', status)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ManifestListResponse>(`${this.API_URL}/status`, { params });
  }

  /**
   * Récupérer les statistiques des manifestes
   */
  getManifestStats(): Observable<{
    total: number;
    byStatus: { [key: string]: number };
    byTransportMode: { [key: string]: number };
    recentCount: number;
  }> {
    return this.http.get<any>(`${this.API_URL}/stats`);
  }

  /**
   * Valider un numéro de suivi
   */
  validateTrackingNumber(trackingNumber: string): Observable<{ valid: boolean; exists: boolean }> {
    return this.http.get<{ valid: boolean; exists: boolean }>(`${this.API_URL}/validate-tracking/${trackingNumber}`);
  }

  /**
   * Générer un QR code pour un manifeste
   */
  generateQRCode(id: number): Observable<{ qrCode: string }> {
    return this.http.post<{ qrCode: string }>(`${this.API_URL}/${id}/qr-code`, {});
  }

  /**
   * Exporter les manifestes vers Excel
   */
  exportToExcel(filters?: {
    status?: string;
    transportMode?: string;
    dateFrom?: Date;
    dateTo?: Date;
  }): Observable<Blob> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.status) params = params.set('status', filters.status);
      if (filters.transportMode) params = params.set('transportMode', filters.transportMode);
      if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom.toISOString());
      if (filters.dateTo) params = params.set('dateTo', filters.dateTo.toISOString());
    }

    return this.http.get(`${this.API_URL}/export/excel`, { 
      params,
      responseType: 'blob' 
    });
  }

  /**
   * Dupliquer un manifeste
   */
  duplicateManifest(id: number): Observable<ManifestResponse> {
    return this.http.post<ManifestResponse>(`${this.API_URL}/${id}/duplicate`, {});
  }

  // Create a new manifest
  createManifestOld(manifest: ManifestRequest): Observable<Manifest> {
    return this.http.post<Manifest>(this.API_URL, manifest, this.getHttpOptions());
  }

  // Get all manifests
  getAllManifestsOld(): Observable<Manifest[]> {
    return this.http.get<Manifest[]>(this.API_URL);
  }

  // Get manifest by ID
  getManifestByIdOld(id: number): Observable<Manifest> {
    return this.http.get<Manifest>(`${this.API_URL}/${id}`);
  }

  // Update manifest
  updateManifestOld(id: number, manifest: ManifestRequest): Observable<Manifest> {
    return this.http.put<Manifest>(`${this.API_URL}/${id}`, manifest, this.getHttpOptions());
  }

  // Delete manifest
  deleteManifestOld(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // Generate PDF
  generateManifestPDFOld(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf`, { 
      responseType: 'blob',
      headers: new HttpHeaders({
        'Accept': 'application/pdf'
      })
    });
  }

  // Add loading signature
  addLoadingSignatureOld(id: number, signature: SignatureRequest): Observable<Manifest> {
    return this.http.post<Manifest>(`${this.API_URL}/${id}/signatures/loading`, signature, this.getHttpOptions());
  }

  // Add delivery signature
  addDeliverySignatureOld(id: number, signature: SignatureRequest): Observable<Manifest> {
    return this.http.post<Manifest>(`${this.API_URL}/${id}/signatures/delivery`, signature, this.getHttpOptions());
  }

  // Get manifest reports
  getManifestReportsOld(startDate?: string, endDate?: string, status?: string): Observable<Manifest[]> {
    let params = '';
    const queryParams: string[] = [];
    
    if (startDate) queryParams.push(`startDate=${startDate}`);
    if (endDate) queryParams.push(`endDate=${endDate}`);
    if (status) queryParams.push(`status=${status}`);
    
    if (queryParams.length > 0) {
      params = '?' + queryParams.join('&');
    }
    
    return this.http.get<Manifest[]>(`${this.API_URL}/reports${params}`);
  }

  // Utility method to download PDF
  downloadManifestPDFOld(id: number, filename?: string): void {
    this.generateManifestPDFOld(id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename || `manifest-${id}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }

  private getHttpOptions() {
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };
  }
}
