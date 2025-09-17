import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Invoice {
  id?: number;
  invoiceNumber: string;
  type: 'CLIENT' | 'SUPPLIER';
  client: any;
  quote?: any;
  lta?: any;
  invoiceDate: string;
  dueDate: string;
  description?: string;
  amountExcludingTax: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  remainingAmount: number;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'PARTIALLY_PAID' | 'OVERDUE' | 'CANCELLED';
  items: InvoiceItem[];
  payments: any[];
  createdAt?: string;
  updatedAt?: string;
}

export interface InvoiceItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  tax?: any;
  taxAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private apiUrl = `${environment.apiUrl}/api/invoices`;

  constructor(private http: HttpClient) {}

  getAllInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(this.apiUrl);
  }

  getInvoicesByClient(clientId: number): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getInvoicesByType(type: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/type/${type}`);
  }

  getInvoicesByStatus(status: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/status/${status}`);
  }

  getOverdueInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/overdue`);
  }

  getInvoicesByDateRange(startDate: string, endDate: string): Observable<Invoice[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Invoice[]>(`${this.apiUrl}/date-range`, { params });
  }

  getInvoicesByTypeAndDateRange(type: string, startDate: string, endDate: string): Observable<Invoice[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Invoice[]>(`${this.apiUrl}/type/${type}/date-range`, { params });
  }

  getInvoiceById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`);
  }

  getInvoiceByNumber(invoiceNumber: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/number/${invoiceNumber}`);
  }

  getTotalPaidAmount(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<number>(`${this.apiUrl}/stats/paid-amount`, { params });
  }

  getTotalRemainingAmount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/remaining-amount`);
  }

  createInvoice(invoice: Invoice): Observable<Invoice> {
    return this.http.post<Invoice>(this.apiUrl, invoice);
  }

  updateInvoice(id: number, invoice: Invoice): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${id}`, invoice);
  }

  deleteInvoice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addItemToInvoice(invoiceId: number, item: InvoiceItem): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/${invoiceId}/items`, item);
  }

  removeItemFromInvoice(invoiceId: number, itemId: number): Observable<Invoice> {
    return this.http.delete<Invoice>(`${this.apiUrl}/${invoiceId}/items/${itemId}`);
  }

  updateInvoiceStatus(id: number, status: string): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${id}/status`, status);
  }

  updateOverdueInvoices(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/update-overdue`, {});
  }

  generateInvoicePdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  exportInvoicesToExcel(startDate?: string, endDate?: string): Observable<Blob> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get(`${this.apiUrl}/export/excel`, { 
      params, 
      responseType: 'blob' 
    });
  }
}
