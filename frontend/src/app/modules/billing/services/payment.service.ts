import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Payment {
  id?: number;
  invoice: any;
  amount: number;
  paymentDate: string;
  paymentMethod?: string;
  reference?: string;
  notes?: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  cashBox?: any;
  bankAccount?: any;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/api/payments`;

  constructor(private http: HttpClient) {}

  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.apiUrl);
  }

  getPaymentsByInvoice(invoiceId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/invoice/${invoiceId}`);
  }

  getPaymentsByStatus(status: string): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/status/${status}`);
  }

  getPaymentsByDateRange(startDate: string, endDate: string): Observable<Payment[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Payment[]>(`${this.apiUrl}/date-range`, { params });
  }

  getPaymentsByCashBox(cashBoxId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/cash-box/${cashBoxId}`);
  }

  getPaymentsByBankAccount(bankAccountId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/bank-account/${bankAccountId}`);
  }

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/${id}`);
  }

  getTotalPaymentsByDateRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<number>(`${this.apiUrl}/stats/total`, { params });
  }

  createPayment(payment: Payment): Observable<Payment> {
    return this.http.post<Payment>(this.apiUrl, payment);
  }

  updatePayment(id: number, payment: Payment): Observable<Payment> {
    return this.http.put<Payment>(`${this.apiUrl}/${id}`, payment);
  }

  completePayment(id: number): Observable<Payment> {
    return this.http.put<Payment>(`${this.apiUrl}/${id}/complete`, {});
  }

  cancelPayment(id: number): Observable<Payment> {
    return this.http.put<Payment>(`${this.apiUrl}/${id}/cancel`, {});
  }

  deletePayment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
