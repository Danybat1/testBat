import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface BankAccount {
  id?: number;
  accountName: string;
  accountNumber: string;
  bankName: string;
  iban?: string;
  swift?: string;
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
export class BankAccountService {
  private apiUrl = `${environment.apiUrl}/api/bank-accounts`;

  constructor(private http: HttpClient) {}

  getAllBankAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(this.apiUrl);
  }

  getActiveBankAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.apiUrl}/active`);
  }

  getBankAccountById(id: number): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.apiUrl}/${id}`);
  }

  getBankAccountByNumber(accountNumber: string): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.apiUrl}/number/${accountNumber}`);
  }

  getBankAccountByName(accountName: string): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.apiUrl}/name/${accountName}`);
  }

  getBankAccountsByBank(bankName: string): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.apiUrl}/bank/${bankName}`);
  }

  getTotalBankBalance(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/total-balance`);
  }

  getBankAccountsWithLowBalance(threshold: number): Observable<BankAccount[]> {
    const params = new HttpParams().set('threshold', threshold.toString());
    return this.http.get<BankAccount[]>(`${this.apiUrl}/low-balance`, { params });
  }

  getBankAccountBalance(id: number, date: string): Observable<number> {
    const params = new HttpParams().set('date', date);
    return this.http.get<number>(`${this.apiUrl}/${id}/balance`, { params });
  }

  getBankAccountTransactions(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/transactions`);
  }

  getBankAccountTransactionsByDateRange(id: number, startDate: string, endDate: string): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<any[]>(`${this.apiUrl}/${id}/transactions/date-range`, { params });
  }

  createBankAccount(bankAccount: BankAccount): Observable<BankAccount> {
    return this.http.post<BankAccount>(this.apiUrl, bankAccount);
  }

  updateBankAccount(id: number, bankAccount: BankAccount): Observable<BankAccount> {
    return this.http.put<BankAccount>(`${this.apiUrl}/${id}`, bankAccount);
  }

  deleteBankAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  activateBankAccount(id: number): Observable<BankAccount> {
    return this.http.put<BankAccount>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivateBankAccount(id: number): Observable<BankAccount> {
    return this.http.put<BankAccount>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  adjustBalance(id: number, amount: number, reason: string): Observable<BankAccount> {
    const body = { amount, reason };
    return this.http.put<BankAccount>(`${this.apiUrl}/${id}/adjust-balance`, body);
  }
}
