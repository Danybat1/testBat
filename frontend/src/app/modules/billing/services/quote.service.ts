import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Quote {
  id?: number;
  quoteNumber: string;
  client: any;
  quoteDate: string;
  validUntil: string;
  description?: string;
  amountExcludingTax: number;
  taxAmount: number;
  totalAmount: number;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'PARTIALLY_PAID' | 'OVERDUE' | 'CANCELLED';
  converted: boolean;
  items: QuoteItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface QuoteItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuoteService {
  private apiUrl = `${environment.apiUrl}/api/quotes`;

  constructor(private http: HttpClient) {}

  getAllQuotes(): Observable<Quote[]> {
    return this.http.get<Quote[]>(this.apiUrl);
  }

  getQuotesByClient(clientId: number): Observable<Quote[]> {
    return this.http.get<Quote[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getQuotesByStatus(status: string): Observable<Quote[]> {
    return this.http.get<Quote[]>(`${this.apiUrl}/status/${status}`);
  }

  getUnconvertedQuotes(): Observable<Quote[]> {
    return this.http.get<Quote[]>(`${this.apiUrl}/unconverted`);
  }

  getExpiredQuotes(): Observable<Quote[]> {
    return this.http.get<Quote[]>(`${this.apiUrl}/expired`);
  }

  getQuotesByDateRange(startDate: string, endDate: string): Observable<Quote[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Quote[]>(`${this.apiUrl}/date-range`, { params });
  }

  getQuoteById(id: number): Observable<Quote> {
    return this.http.get<Quote>(`${this.apiUrl}/${id}`);
  }

  getQuoteByNumber(quoteNumber: string): Observable<Quote> {
    return this.http.get<Quote>(`${this.apiUrl}/number/${quoteNumber}`);
  }

  createQuote(quote: Quote): Observable<Quote> {
    return this.http.post<Quote>(this.apiUrl, quote);
  }

  updateQuote(id: number, quote: Quote): Observable<Quote> {
    return this.http.put<Quote>(`${this.apiUrl}/${id}`, quote);
  }

  deleteQuote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addItemToQuote(quoteId: number, item: QuoteItem): Observable<Quote> {
    return this.http.post<Quote>(`${this.apiUrl}/${quoteId}/items`, item);
  }

  removeItemFromQuote(quoteId: number, itemId: number): Observable<Quote> {
    return this.http.delete<Quote>(`${this.apiUrl}/${quoteId}/items/${itemId}`);
  }

  convertQuoteToInvoice(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/convert`, {});
  }

  generateQuotePdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  exportQuotesToExcel(startDate?: string, endDate?: string): Observable<Blob> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get(`${this.apiUrl}/export/excel`, { 
      params, 
      responseType: 'blob' 
    });
  }
}
