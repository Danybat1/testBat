import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { 
  Shipment, 
  TrackingEvent, 
  ShipmentStatus, 
  ShipmentSearchCriteria, 
  ShipmentStatistics,
  PagedResult 
} from '../models/shipment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ShipmentService {
  private apiUrl = `${environment.apiUrl}/api/shipments`;

  constructor(private http: HttpClient) {}

  /**
   * Track shipment by tracking number (Public endpoint)
   */
  trackShipment(trackingNumber: string): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.apiUrl}/track/${trackingNumber}`)
      .pipe(map(shipment => this.convertDates(shipment)));
  }

  /**
   * Get shipment by ID (Admin only)
   */
  getShipmentById(id: number): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.apiUrl}/${id}`)
      .pipe(map(shipment => this.convertDates(shipment)));
  }

  /**
   * Search shipments with filters (Admin only)
   */
  searchShipments(criteria: ShipmentSearchCriteria): Observable<PagedResult<Shipment>> {
    let params = new HttpParams();
    
    if (criteria.trackingNumber) {
      params = params.set('trackingNumber', criteria.trackingNumber);
    }
    if (criteria.senderName) {
      params = params.set('senderName', criteria.senderName);
    }
    if (criteria.recipientName) {
      params = params.set('recipientName', criteria.recipientName);
    }
    if (criteria.status) {
      params = params.set('status', criteria.status);
    }
    if (criteria.clientId) {
      params = params.set('clientId', criteria.clientId.toString());
    }
    if (criteria.page !== undefined) {
      params = params.set('page', criteria.page.toString());
    }
    if (criteria.size !== undefined) {
      params = params.set('size', criteria.size.toString());
    }

    return this.http.get<PagedResult<Shipment>>(`${this.apiUrl}/search`, { params })
      .pipe(map(result => ({
        ...result,
        content: result.content.map(shipment => this.convertDates(shipment))
      })));
  }

  /**
   * Get shipments by client (Client portal)
   */
  getShipmentsByClient(clientId: number, page: number = 0, size: number = 10): Observable<PagedResult<Shipment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResult<Shipment>>(`${this.apiUrl}/client/${clientId}`, { params })
      .pipe(map(result => ({
        ...result,
        content: result.content.map(shipment => this.convertDates(shipment))
      })));
  }

  /**
   * Get shipments by status (Admin only)
   */
  getShipmentsByStatus(status: ShipmentStatus, page: number = 0, size: number = 20): Observable<PagedResult<Shipment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResult<Shipment>>(`${this.apiUrl}/status/${status}`, { params })
      .pipe(map(result => ({
        ...result,
        content: result.content.map(shipment => this.convertDates(shipment))
      })));
  }

  /**
   * Get in-transit shipments (Admin only)
   */
  getInTransitShipments(page: number = 0, size: number = 20): Observable<PagedResult<Shipment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResult<Shipment>>(`${this.apiUrl}/in-transit`, { params })
      .pipe(map(result => ({
        ...result,
        content: result.content.map(shipment => this.convertDates(shipment))
      })));
  }

  /**
   * Get overdue shipments (Admin only)
   */
  getOverdueShipments(): Observable<Shipment[]> {
    return this.http.get<Shipment[]>(`${this.apiUrl}/overdue`)
      .pipe(map(shipments => shipments.map(shipment => this.convertDates(shipment))));
  }

  /**
   * Get recent shipments for client (Client portal)
   */
  getRecentShipmentsByClient(clientId: number, limit: number = 5): Observable<Shipment[]> {
    const params = new HttpParams().set('limit', limit.toString());
    
    return this.http.get<Shipment[]>(`${this.apiUrl}/client/${clientId}/recent`, { params })
      .pipe(map(shipments => shipments.map(shipment => this.convertDates(shipment))));
  }

  /**
   * Get shipment statistics (Admin only)
   */
  getShipmentStatistics(): Observable<ShipmentStatistics> {
    return this.http.get<ShipmentStatistics>(`${this.apiUrl}/statistics`);
  }

  /**
   * Create a new shipment (Admin only)
   */
  createShipment(shipment: Shipment): Observable<Shipment> {
    return this.http.post<Shipment>(this.apiUrl, shipment)
      .pipe(map(result => this.convertDates(result)));
  }

  /**
   * Update shipment (Admin only)
   */
  updateShipment(id: number, shipment: Shipment): Observable<Shipment> {
    return this.http.put<Shipment>(`${this.apiUrl}/${id}`, shipment)
      .pipe(map(result => this.convertDates(result)));
  }

  /**
   * Update shipment status (Admin only)
   */
  updateShipmentStatus(id: number, status: ShipmentStatus, description: string, location?: string): Observable<Shipment> {
    let params = new HttpParams()
      .set('status', status)
      .set('description', description);
    
    if (location) {
      params = params.set('location', location);
    }

    return this.http.put<Shipment>(`${this.apiUrl}/${id}/status`, null, { params })
      .pipe(map(result => this.convertDates(result)));
  }

  /**
   * Add tracking event (Admin only)
   */
  addTrackingEvent(
    shipmentId: number, 
    status: ShipmentStatus, 
    description: string, 
    location?: string,
    city?: string,
    country?: string,
    operatorName?: string
  ): Observable<TrackingEvent> {
    let params = new HttpParams()
      .set('status', status)
      .set('description', description);
    
    if (location) params = params.set('location', location);
    if (city) params = params.set('city', city);
    if (country) params = params.set('country', country);
    if (operatorName) params = params.set('operatorName', operatorName);

    return this.http.post<TrackingEvent>(`${this.apiUrl}/${shipmentId}/events`, null, { params })
      .pipe(map(event => this.convertEventDates(event)));
  }

  /**
   * Get tracking events for shipment
   */
  getTrackingEvents(shipmentId: number): Observable<TrackingEvent[]> {
    return this.http.get<TrackingEvent[]>(`${this.apiUrl}/${shipmentId}/events`)
      .pipe(map(events => events.map(event => this.convertEventDates(event))));
  }

  /**
   * Delete shipment (Admin only)
   */
  deleteShipment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all shipment statuses
   */
  getShipmentStatuses(): Observable<ShipmentStatus[]> {
    return this.http.get<ShipmentStatus[]>(`${this.apiUrl}/statuses`);
  }

  /**
   * Convert string dates to Date objects
   */
  private convertDates(shipment: any): Shipment {
    if (shipment.pickupDate) {
      shipment.pickupDate = new Date(shipment.pickupDate);
    }
    if (shipment.expectedDeliveryDate) {
      shipment.expectedDeliveryDate = new Date(shipment.expectedDeliveryDate);
    }
    if (shipment.actualDeliveryDate) {
      shipment.actualDeliveryDate = new Date(shipment.actualDeliveryDate);
    }
    if (shipment.createdAt) {
      shipment.createdAt = new Date(shipment.createdAt);
    }
    if (shipment.updatedAt) {
      shipment.updatedAt = new Date(shipment.updatedAt);
    }
    
    // Convert tracking events dates
    if (shipment.trackingEvents) {
      shipment.trackingEvents = shipment.trackingEvents.map((event: any) => this.convertEventDates(event));
    }
    
    return shipment;
  }

  /**
   * Convert string dates to Date objects for tracking events
   */
  private convertEventDates(event: any): TrackingEvent {
    if (event.eventDate) {
      event.eventDate = new Date(event.eventDate);
    }
    if (event.nextAttemptDate) {
      event.nextAttemptDate = new Date(event.nextAttemptDate);
    }
    if (event.createdAt) {
      event.createdAt = new Date(event.createdAt);
    }
    if (event.updatedAt) {
      event.updatedAt = new Date(event.updatedAt);
    }
    
    return event;
  }

  /**
   * Get status progress percentage for UI
   */
  getStatusProgress(status: ShipmentStatus): number {
    const progressMap: { [key in ShipmentStatus]: number } = {
      [ShipmentStatus.PENDING]: 0,
      [ShipmentStatus.CREATED]: 5,
      [ShipmentStatus.CONFIRMED]: 10,
      [ShipmentStatus.PICKUP_SCHEDULED]: 20,
      [ShipmentStatus.PICKED_UP]: 30,
      [ShipmentStatus.IN_TRANSIT]: 60,
      [ShipmentStatus.OUT_FOR_DELIVERY]: 85,
      [ShipmentStatus.DELIVERED]: 100,
      [ShipmentStatus.DELIVERY_ATTEMPTED]: 80,
      [ShipmentStatus.EXCEPTION]: 50,
      [ShipmentStatus.RETURNED_TO_SENDER]: 100,
      [ShipmentStatus.CANCELLED]: 0,
      [ShipmentStatus.LOST]: 0,
      [ShipmentStatus.DAMAGED]: 0
    };
    
    return progressMap[status] || 0;
  }

  /**
   * Check if status is final (no more updates expected)
   */
  isFinalStatus(status: ShipmentStatus): boolean {
    return [
      ShipmentStatus.DELIVERED,
      ShipmentStatus.RETURNED_TO_SENDER,
      ShipmentStatus.CANCELLED,
      ShipmentStatus.LOST,
      ShipmentStatus.DAMAGED
    ].includes(status);
  }

  /**
   * Check if status indicates an exception
   */
  isExceptionStatus(status: ShipmentStatus): boolean {
    return [
      ShipmentStatus.EXCEPTION,
      ShipmentStatus.DELIVERY_ATTEMPTED,
      ShipmentStatus.LOST,
      ShipmentStatus.DAMAGED
    ].includes(status);
  }
}
