import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { 
  Account, 
  JournalEntry, 
  AccountingEntry, 
  FiscalYear, 
  BalanceSummary, 
  DashboardStats,
  JournalEntryFilter,
  TrialBalanceFilter,
  AccountingRule,
  AuditLog
} from '../models/accounting.models';

@Injectable({
  providedIn: 'root'
})
export class AccountingService {
  private readonly apiUrl = `${environment.apiUrl}/api/accounting`;

  constructor(private http: HttpClient) {}

  // ========== DASHBOARD ==========
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`);
  }

  // ========== ACCOUNTS ==========
  getAllAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/accounts`);
  }

  getAccountById(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/accounts/${id}`);
  }

  getAccountByNumber(accountNumber: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/accounts/number/${accountNumber}`);
  }

  getAccountsByType(accountType: string): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/accounts/type/${accountType}`);
  }

  createAccount(account: Partial<Account>): Observable<Account> {
    return this.http.post<Account>(`${this.apiUrl}/accounts`, account);
  }

  updateAccount(id: number, account: Partial<Account>): Observable<Account> {
    return this.http.put<Account>(`${this.apiUrl}/accounts/${id}`, account);
  }

  deactivateAccount(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/accounts/${id}/deactivate`, {});
  }

  // ========== JOURNAL ENTRIES ==========
  getJournalEntries(filter?: JournalEntryFilter): Observable<JournalEntry[]> {
    let params = new HttpParams();
    
    if (filter) {
      if (filter.startDate) {
        params = params.set('startDate', filter.startDate.toISOString().split('T')[0]);
      }
      if (filter.endDate) {
        params = params.set('endDate', filter.endDate.toISOString().split('T')[0]);
      }
      if (filter.sourceType) {
        params = params.set('sourceType', filter.sourceType);
      }
      if (filter.status) {
        params = params.set('status', filter.status);
      }
      if (filter.accountNumber) {
        params = params.set('accountNumber', filter.accountNumber);
      }
      if (filter.reference) {
        params = params.set('reference', filter.reference);
      }
    }

    return this.http.get<JournalEntry[]>(`${this.apiUrl}/journal-entries`, { params });
  }

  getJournalEntryById(id: number): Observable<JournalEntry> {
    return this.http.get<JournalEntry>(`${this.apiUrl}/journal-entries/${id}`);
  }

  getJournalEntriesByFiscalYear(fiscalYearId: number): Observable<JournalEntry[]> {
    return this.http.get<JournalEntry[]>(`${this.apiUrl}/journal-entries/fiscal-year/${fiscalYearId}`);
  }

  getJournalEntriesBySource(sourceType: string, sourceId: number): Observable<JournalEntry[]> {
    return this.http.get<JournalEntry[]>(`${this.apiUrl}/journal-entries/source/${sourceType}/${sourceId}`);
  }

  createJournalEntry(journalEntry: Partial<JournalEntry>): Observable<JournalEntry> {
    return this.http.post<JournalEntry>(`${this.apiUrl}/journal-entries`, journalEntry);
  }

  postJournalEntry(id: number): Observable<JournalEntry> {
    return this.http.patch<JournalEntry>(`${this.apiUrl}/journal-entries/${id}/post`, {});
  }

  reverseJournalEntry(id: number, reason: string): Observable<JournalEntry> {
    return this.http.patch<JournalEntry>(`${this.apiUrl}/journal-entries/${id}/reverse`, { reason });
  }

  // ========== FISCAL YEARS ==========
  getFiscalYears(): Observable<FiscalYear[]> {
    return this.http.get<FiscalYear[]>(`${this.apiUrl}/fiscal-years`);
  }

  getCurrentFiscalYear(): Observable<FiscalYear> {
    return this.http.get<FiscalYear>(`${this.apiUrl}/fiscal-years/current`);
  }

  getFiscalYearById(id: number): Observable<FiscalYear> {
    return this.http.get<FiscalYear>(`${this.apiUrl}/fiscal-years/${id}`);
  }

  createFiscalYear(fiscalYear: Partial<FiscalYear>): Observable<FiscalYear> {
    return this.http.post<FiscalYear>(`${this.apiUrl}/fiscal-years`, fiscalYear);
  }

  closeFiscalYear(id: number): Observable<FiscalYear> {
    return this.http.patch<FiscalYear>(`${this.apiUrl}/fiscal-years/${id}/close`, {});
  }

  // ========== REPORTS ==========
  getTrialBalance(filter?: TrialBalanceFilter): Observable<BalanceSummary[]> {
    let params = new HttpParams();
    
    if (filter) {
      if (filter.fiscalYearId) {
        params = params.set('fiscalYearId', filter.fiscalYearId.toString());
      }
      if (filter.startDate) {
        params = params.set('startDate', filter.startDate.toISOString().split('T')[0]);
      }
      if (filter.endDate) {
        params = params.set('endDate', filter.endDate.toISOString().split('T')[0]);
      }
      if (filter.accountType) {
        params = params.set('accountType', filter.accountType);
      }
      if (filter.currency) {
        params = params.set('currency', filter.currency);
      }
      if (filter.includeInactive !== undefined) {
        params = params.set('includeInactive', filter.includeInactive.toString());
      }
    }

    return this.http.get<BalanceSummary[]>(`${this.apiUrl}/reports/trial-balance`, { params });
  }

  getBalanceSummary(): Observable<BalanceSummary[]> {
    return this.http.get<BalanceSummary[]>(`${this.apiUrl}/reports/balance-summary`);
  }

  getGeneralLedger(accountNumber: string, startDate?: Date, endDate?: Date): Observable<AccountingEntry[]> {
    let params = new HttpParams();
    
    if (startDate) {
      params = params.set('startDate', startDate.toISOString().split('T')[0]);
    }
    if (endDate) {
      params = params.set('endDate', endDate.toISOString().split('T')[0]);
    }

    return this.http.get<AccountingEntry[]>(`${this.apiUrl}/reports/general-ledger/${accountNumber}`, { params });
  }

  getIncomeStatement(fiscalYearId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/reports/income-statement/${fiscalYearId}`);
  }

  getBalanceSheet(fiscalYearId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/reports/balance-sheet/${fiscalYearId}`);
  }

  // ========== ACCOUNTING RULES ==========
  getAccountingRules(): Observable<AccountingRule[]> {
    return this.http.get<AccountingRule[]>(`${this.apiUrl}/rules`);
  }

  createAccountingRule(rule: Partial<AccountingRule>): Observable<AccountingRule> {
    return this.http.post<AccountingRule>(`${this.apiUrl}/rules`, rule);
  }

  updateAccountingRule(id: number, rule: Partial<AccountingRule>): Observable<AccountingRule> {
    return this.http.put<AccountingRule>(`${this.apiUrl}/rules/${id}`, rule);
  }

  deleteAccountingRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rules/${id}`);
  }

  // ========== AUDIT LOG ==========
  getAuditLogs(page: number = 0, size: number = 20): Observable<{ content: AuditLog[], totalElements: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<{ content: AuditLog[], totalElements: number }>(`${this.apiUrl}/audit-logs`, { params });
  }

  // ========== VALIDATION ==========
  validateJournalEntries(): Observable<{ valid: boolean, errors: string[] }> {
    return this.http.get<{ valid: boolean, errors: string[] }>(`${this.apiUrl}/reports/validation`);
  }

  // ========== EXPORT ==========
  exportTrialBalance(format: 'excel' | 'pdf', filter?: TrialBalanceFilter): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    
    if (filter) {
      if (filter.fiscalYearId) {
        params = params.set('fiscalYearId', filter.fiscalYearId.toString());
      }
      if (filter.startDate) {
        params = params.set('startDate', filter.startDate.toISOString().split('T')[0]);
      }
      if (filter.endDate) {
        params = params.set('endDate', filter.endDate.toISOString().split('T')[0]);
      }
    }

    return this.http.get(`${this.apiUrl}/reports/trial-balance/export`, { 
      params, 
      responseType: 'blob' 
    });
  }

  exportGeneralLedger(accountNumber: string, format: 'excel' | 'pdf', startDate?: Date, endDate?: Date): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    
    if (startDate) {
      params = params.set('startDate', startDate.toISOString().split('T')[0]);
    }
    if (endDate) {
      params = params.set('endDate', endDate.toISOString().split('T')[0]);
    }

    return this.http.get(`${this.apiUrl}/reports/general-ledger/${accountNumber}/export`, { 
      params, 
      responseType: 'blob' 
    });
  }
}
