import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup } from '@angular/forms';

interface JournalEntry {
  id: number;
  entryNumber: string;
  entryDate: string;
  description: string;
  reference: string;
  totalDebit: number;
  totalCredit: number;
  sourceType: string;
  accountingEntries: AccountingEntry[];
}

interface AccountingEntry {
  id: number;
  account: {
    accountNumber: string;
    accountName: string;
  };
  debitAmount: number;
  creditAmount: number;
  description: string;
}

interface AccountBalance {
  accountNumber: string;
  accountName: string;
  balance: number;
}

@Component({
  selector: 'app-accounting-report',
  templateUrl: './accounting-report.component.html',
  styleUrls: ['./accounting-report.component.scss']
})
export class AccountingReportComponent implements OnInit {
  
  // Formulaires
  periodForm: FormGroup;
  accountForm: FormGroup;
  
  // Données
  journalEntries: JournalEntry[] = [];
  ltaPaymentReport: any = null;
  treasuryReport: any = null;
  accountsSummary: any = null;
  accountMovements: AccountingEntry[] = [];
  selectedAccountBalance: any = null;
  
  // États
  loading = false;
  activeTab = 'journal';
  
  // Options
  sourceTypes = [
    { value: 'LTA_PAYMENT', label: 'Paiements LTA' },
    { value: 'INVOICE', label: 'Factures' },
    { value: 'PAYMENT', label: 'Paiements' },
    { value: 'MANUAL', label: 'Écritures manuelles' }
  ];

  constructor(
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.periodForm = this.fb.group({
      startDate: [''],
      endDate: ['']
    });

    this.accountForm = this.fb.group({
      accountNumber: [''],
      date: ['']
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.loadAccountsSummary();
    this.loadTreasuryReport();
    this.loadRecentJournalEntries();
  }

  // Chargement des écritures de journal
  loadRecentJournalEntries(): void {
    this.loading = true;
    this.http.get<any>('/api/accounting-reports/journal-entries?page=0&size=20')
      .subscribe({
        next: (response) => {
          this.journalEntries = response.content || [];
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des écritures:', error);
          this.loading = false;
        }
      });
  }

  // Chargement par type de source
  loadEntriesBySourceType(sourceType: string): void {
    this.loading = true;
    this.http.get<JournalEntry[]>(`/api/accounting-reports/journal-entries/by-source/${sourceType}`)
      .subscribe({
        next: (entries) => {
          this.journalEntries = entries;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement par type:', error);
          this.loading = false;
        }
      });
  }

  // Chargement par période
  loadEntriesByPeriod(): void {
    const formValue = this.periodForm.value;
    if (!formValue.startDate || !formValue.endDate) {
      alert('Veuillez sélectionner une période');
      return;
    }

    this.loading = true;
    this.http.get<JournalEntry[]>('/api/accounting-reports/journal-entries/by-period', {
      params: {
        startDate: formValue.startDate,
        endDate: formValue.endDate
      }
    }).subscribe({
      next: (entries) => {
        this.journalEntries = entries;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement par période:', error);
        this.loading = false;
      }
    });
  }

  // Chargement du rapport des paiements LTA
  loadLTAPaymentReport(): void {
    const formValue = this.periodForm.value;
    if (!formValue.startDate || !formValue.endDate) {
      alert('Veuillez sélectionner une période');
      return;
    }

    this.loading = true;
    this.http.get<any>('/api/accounting-reports/lta-payments-report', {
      params: {
        startDate: formValue.startDate,
        endDate: formValue.endDate
      }
    }).subscribe({
      next: (report) => {
        this.ltaPaymentReport = report;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du rapport LTA:', error);
        this.loading = false;
      }
    });
  }

  // Chargement du rapport de trésorerie
  loadTreasuryReport(): void {
    this.loading = true;
    this.http.get<any>('/api/accounting-reports/treasury-report')
      .subscribe({
        next: (report) => {
          this.treasuryReport = report;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du rapport de trésorerie:', error);
          this.loading = false;
        }
      });
  }

  // Chargement du résumé des comptes
  loadAccountsSummary(): void {
    this.http.get<any>('/api/accounting-reports/accounts-summary')
      .subscribe({
        next: (summary) => {
          this.accountsSummary = summary;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du résumé des comptes:', error);
        }
      });
  }

  // Chargement des mouvements d'un compte
  loadAccountMovements(): void {
    const accountNumber = this.accountForm.value.accountNumber;
    if (!accountNumber) {
      alert('Veuillez saisir un numéro de compte');
      return;
    }

    this.loading = true;
    this.http.get<AccountingEntry[]>(`/api/accounting-reports/account-movements/${accountNumber}`)
      .subscribe({
        next: (movements) => {
          this.accountMovements = movements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des mouvements:', error);
          this.loading = false;
        }
      });
  }

  // Chargement du solde d'un compte
  loadAccountBalance(): void {
    const formValue = this.accountForm.value;
    if (!formValue.accountNumber) {
      alert('Veuillez saisir un numéro de compte');
      return;
    }

    const params: any = {};
    if (formValue.date) {
      params.date = formValue.date;
    }

    this.http.get<any>(`/api/accounting-reports/account-balance/${formValue.accountNumber}`, { params })
      .subscribe({
        next: (balance) => {
          this.selectedAccountBalance = balance;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du solde:', error);
        }
      });
  }

  // Méthodes utilitaires
  getSourceTypeLabel(sourceType: string): string {
    const source = this.sourceTypes.find(s => s.value === sourceType);
    return source ? source.label : sourceType;
  }

  getAccountTypeClass(accountNumber: string): string {
    if (accountNumber.startsWith('411')) return 'badge-primary';
    if (accountNumber.startsWith('531')) return 'badge-success';
    if (accountNumber.startsWith('512')) return 'badge-info';
    if (accountNumber.startsWith('701')) return 'badge-warning';
    return 'badge-secondary';
  }

  interpretMovement(entry: AccountingEntry): string {
    const accountNumber = entry.account.accountNumber;
    const isDebit = entry.debitAmount > 0;

    if (accountNumber.startsWith('411')) {
      return isDebit ? 'Augmentation créance client' : 'Diminution créance client (encaissement)';
    } else if (accountNumber.startsWith('531')) {
      return isDebit ? 'Entrée d\'espèces en caisse' : 'Sortie d\'espèces de la caisse';
    } else if (accountNumber.startsWith('512')) {
      return isDebit ? 'Crédit bancaire' : 'Débit bancaire';
    } else if (accountNumber.startsWith('701')) {
      return isDebit ? 'Diminution du CA' : 'Augmentation du CA (vente)';
    }
    return 'Mouvement comptable';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
