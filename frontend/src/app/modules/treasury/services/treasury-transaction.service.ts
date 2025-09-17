import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface TreasuryTransaction {
  id?: number;
  reference: string;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  amount: number;
  transactionDate: string;
  description: string;
  category?: string;
  cashBox?: any;
  bankAccount?: any;
  destinationCashBox?: any;
  destinationBankAccount?: any;
  payment?: any;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TreasuryTransactionService {
  private apiUrl = `${environment.apiUrl}/api/treasury-transactions`;

  constructor(private http: HttpClient) {}

  getAllTransactions(): Observable<TreasuryTransaction[]> {
    return this.http.get<TreasuryTransaction[]>(this.apiUrl);
  }

  getTransactionsByType(type: string): Observable<TreasuryTransaction[]> {
    return this.http.get<TreasuryTransaction[]>(`${this.apiUrl}/type/${type}`);
  }

  getTransactionsByDateRange(startDate: string, endDate: string): Observable<TreasuryTransaction[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<TreasuryTransaction[]>(`${this.apiUrl}/date-range`, { params });
  }

  getTransactionsByCashBox(cashBoxId: number): Observable<TreasuryTransaction[]> {
    return this.http.get<TreasuryTransaction[]>(`${this.apiUrl}/cash-box/${cashBoxId}`);
  }

  getTransactionsByBankAccount(bankAccountId: number): Observable<TreasuryTransaction[]> {
    return this.http.get<TreasuryTransaction[]>(`${this.apiUrl}/bank-account/${bankAccountId}`);
  }

  getTransactionsByCategory(category: string): Observable<TreasuryTransaction[]> {
    return this.http.get<TreasuryTransaction[]>(`${this.apiUrl}/category/${category}`);
  }

  getAllCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }

  getTransactionById(id: number): Observable<TreasuryTransaction> {
    return this.http.get<TreasuryTransaction>(`${this.apiUrl}/${id}`);
  }

  getTransactionByReference(reference: string): Observable<TreasuryTransaction> {
    return this.http.get<TreasuryTransaction>(`${this.apiUrl}/reference/${reference}`);
  }

  getTotalIncomeByDateRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<number>(`${this.apiUrl}/stats/income`, { params });
  }

  getTotalExpenseByDateRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<number>(`${this.apiUrl}/stats/expense`, { params });
  }

  createTransaction(transaction: TreasuryTransaction): Observable<TreasuryTransaction> {
    return this.http.post<TreasuryTransaction>(this.apiUrl, transaction);
  }

  updateTransaction(id: number, transaction: TreasuryTransaction): Observable<TreasuryTransaction> {
    return this.http.put<TreasuryTransaction>(`${this.apiUrl}/${id}`, transaction);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createTransfer(transferData: {
    sourceCashBoxId?: number;
    sourceAccountId?: number;
    destCashBoxId?: number;
    destAccountId?: number;
    amount: number;
    description: string;
  }): Observable<TreasuryTransaction> {
    return this.http.post<TreasuryTransaction>(`${this.apiUrl}/transfer`, transferData);
  }
}
