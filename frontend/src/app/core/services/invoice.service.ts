import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  Invoice, 
  InvoiceRequest, 
  InvoiceResponse, 
  InvoiceListResponse, 
  InvoiceStats, 
  InvoiceSearchParams,
  InvoiceStatus,
  PaymentTerms
} from '../../models/invoice.model';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Create a new invoice
   */
  createInvoice(invoice: InvoiceRequest): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(`${this.API_URL}/invoices`, invoice);
  }

  /**
   * Get invoice by ID
   */
  getInvoiceById(id: number): Observable<InvoiceResponse> {
    return this.http.get<InvoiceResponse>(`${this.API_URL}/invoices/${id}`);
  }

  /**
   * Update invoice
   */
  updateInvoice(id: number, invoice: Partial<Invoice>): Observable<InvoiceResponse> {
    return this.http.put<InvoiceResponse>(`${this.API_URL}/invoices/${id}`, invoice);
  }

  /**
   * Delete invoice
   */
  deleteInvoice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/invoices/${id}`);
  }

  /**
   * Get paginated list of invoices with search and filters
   */
  getInvoices(params: InvoiceSearchParams = {}): Observable<InvoiceListResponse> {
    let httpParams = new HttpParams();
    
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);
    if (params.direction) httpParams = httpParams.set('direction', params.direction);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.type) httpParams = httpParams.set('type', params.type);
    if (params.clientId) httpParams = httpParams.set('clientId', params.clientId.toString());
    if (params.dateFrom) httpParams = httpParams.set('dateFrom', params.dateFrom);
    if (params.dateTo) httpParams = httpParams.set('dateTo', params.dateTo);
    if (params.dueDateFrom) httpParams = httpParams.set('dueDateFrom', params.dueDateFrom);
    if (params.dueDateTo) httpParams = httpParams.set('dueDateTo', params.dueDateTo);
    if (params.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<InvoiceListResponse>(`${this.API_URL}/invoices`, { params: httpParams });
  }

  /**
   * Update invoice status
   */
  updateInvoiceStatus(id: number, status: InvoiceStatus): Observable<InvoiceResponse> {
    return this.http.patch<InvoiceResponse>(`${this.API_URL}/invoices/${id}/status`, { status });
  }

  /**
   * Send invoice to client
   */
  sendInvoice(id: number, email?: string): Observable<InvoiceResponse> {
    const body = email ? { email } : {};
    return this.http.post<InvoiceResponse>(`${this.API_URL}/invoices/${id}/send`, body);
  }

  /**
   * Mark invoice as paid
   */
  markAsPaid(id: number, paidAmount: number, paymentDate: string, paymentMethod?: string): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(`${this.API_URL}/invoices/${id}/payment`, {
      paidAmount,
      paymentDate,
      paymentMethod
    });
  }

  /**
   * Get invoice statistics
   */
  getInvoiceStats(): Observable<InvoiceStats> {
    return this.http.get<InvoiceStats>(`${this.API_URL}/invoices/stats`);
  }

  /**
   * Generate PDF for invoice
   */
  generateInvoicePDF(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/invoices/${id}/pdf`, { 
      responseType: 'blob' 
    });
  }

  /**
   * Generate PDF for invoice (alias for compatibility)
   */
  generateInvoicePdf(id: number): Observable<Blob> {
    return this.generateInvoicePDF(id);
  }

  /**
   * Get all invoices (simplified version for list component)
   */
  getAllInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.API_URL}/invoices`);
  }

  /**
   * Export invoices to Excel
   */
  exportInvoicesToExcel(): Observable<Blob> {
    return this.http.get(`${this.API_URL}/invoices/export/excel`, {
      responseType: 'blob'
    });
  }

  /**
   * Get invoice preview in HTML format
   */
  getInvoicePreview(id: number): Observable<string> {
    return this.http.get(`${this.API_URL}/invoices/${id}/preview`, { 
      responseType: 'text' 
    });
  }

  /**
   * Generate invoice preview without creating the invoice in database
   */
  generateInvoicePreview(invoiceRequest: InvoiceRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/invoices/preview`, invoiceRequest, {
      responseType: 'text'
    });
  }

  /**
   * Get invoices for a specific client
   */
  getClientInvoices(clientId: number, params: InvoiceSearchParams = {}): Observable<InvoiceListResponse> {
    return this.getInvoices({ ...params, clientId });
  }

  /**
   * Create invoice from LTA
   */
  createInvoiceFromLTA(ltaId: number, additionalData?: Partial<InvoiceRequest>): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(`${this.API_URL}/invoices/from-lta/${ltaId}`, additionalData || {});
  }

  /**
   * Create invoice from multiple LTAs
   */
  createInvoiceFromLTAs(ltaIds: number[], additionalData?: Partial<InvoiceRequest>): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(`${this.API_URL}/invoices/from-ltas`, {
      ltaIds,
      ...additionalData
    });
  }

  /**
   * Generate next invoice number
   */
  generateInvoiceNumber(): Observable<{ invoiceNumber: string }> {
    return this.http.get<{ invoiceNumber: string }>(`${this.API_URL}/invoices/next-number`);
  }

  /**
   * Calculate due date based on payment terms
   */
  calculateDueDate(invoiceDate: string, paymentTerms: PaymentTerms): string {
    const date = new Date(invoiceDate);
    
    switch (paymentTerms) {
      case PaymentTerms.IMMEDIATE:
        return invoiceDate;
      case PaymentTerms.NET_15:
        date.setDate(date.getDate() + 15);
        break;
      case PaymentTerms.NET_30:
        date.setDate(date.getDate() + 30);
        break;
      case PaymentTerms.NET_45:
        date.setDate(date.getDate() + 45);
        break;
      case PaymentTerms.NET_60:
        date.setDate(date.getDate() + 60);
        break;
    }
    
    return date.toISOString().split('T')[0];
  }

  /**
   * Calculate totals for invoice items
   */
  calculateInvoiceTotals(items: Array<{quantity: number, unitPrice: number, taxRate: number}>) {
    let subtotal = 0;
    let totalTax = 0;

    items.forEach(item => {
      const itemTotal = item.quantity * item.unitPrice;
      const itemTax = itemTotal * (item.taxRate / 100);
      
      subtotal += itemTotal;
      totalTax += itemTax;
    });

    return {
      subtotal: Math.round(subtotal * 100) / 100,
      totalTax: Math.round(totalTax * 100) / 100,
      totalAmount: Math.round((subtotal + totalTax) * 100) / 100
    };
  }
}
