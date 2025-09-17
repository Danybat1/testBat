import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CashBox {
  id?: number;
  name: string;
  description?: string;
  initialBalance: number;
  currentBalance: number;
  active: boolean;
  transactions?: any[];
  payments?: any[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CashBoxService {
  private apiUrl = `${environment.apiUrl}/api/cash-boxes`;

  constructor(private http: HttpClient) {}

  getAllCashBoxes(): Observable<CashBox[]> {
    return this.http.get<CashBox[]>(this.apiUrl);
  }

  getActiveCashBoxes(): Observable<CashBox[]> {
    return this.http.get<CashBox[]>(`${this.apiUrl}/active`);
  }

  getCashBoxById(id: number): Observable<CashBox> {
    return this.http.get<CashBox>(`${this.apiUrl}/${id}`);
  }

  getCashBoxByName(name: string): Observable<CashBox> {
    return this.http.get<CashBox>(`${this.apiUrl}/name/${name}`);
  }

  getTotalCashBalance(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/total-balance`);
  }

  getCashBoxesWithLowBalance(threshold: number): Observable<CashBox[]> {
    const params = new HttpParams().set('threshold', threshold.toString());
    return this.http.get<CashBox[]>(`${this.apiUrl}/low-balance`, { params });
  }

  getCashBoxBalance(id: number, date: string): Observable<number> {
    const params = new HttpParams().set('date', date);
    return this.http.get<number>(`${this.apiUrl}/${id}/balance`, { params });
  }

  getCashBoxTransactions(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/transactions`);
  }

  getCashBoxTransactionsByDateRange(id: number, startDate: string, endDate: string): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<any[]>(`${this.apiUrl}/${id}/transactions/date-range`, { params });
  }

  createCashBox(cashBox: CashBox): Observable<CashBox> {
    return this.http.post<CashBox>(this.apiUrl, cashBox);
  }

  updateCashBox(id: number, cashBox: CashBox): Observable<CashBox> {
    return this.http.put<CashBox>(`${this.apiUrl}/${id}`, cashBox);
  }

  deleteCashBox(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  activateCashBox(id: number): Observable<CashBox> {
    return this.http.put<CashBox>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivateCashBox(id: number): Observable<CashBox> {
    return this.http.put<CashBox>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  adjustBalance(id: number, amount: number, reason: string): Observable<CashBox> {
    const body = { amount, reason };
    return this.http.put<CashBox>(`${this.apiUrl}/${id}/adjust-balance`, body);
  }
}
