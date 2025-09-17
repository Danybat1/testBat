import { Component, OnInit } from '@angular/core';

export interface LedgerEntry {
  id: number;
  date: Date;
  reference: string;
  description: string;
  debitAmount: number;
  creditAmount: number;
  balance: number;
  journalEntryId: number;
}

export interface GeneralLedgerAccount {
  accountCode: string;
  accountName: string;
  accountType: string;
  openingBalance: number;
  totalDebits: number;
  totalCredits: number;
  closingBalance: number;
  entries: LedgerEntry[];
}

@Component({
  selector: 'app-general-ledger',
  templateUrl: './general-ledger.component.html',
  styleUrls: ['./general-ledger.component.scss']
})
export class GeneralLedgerComponent implements OnInit {
  accounts: GeneralLedgerAccount[] = [];
  selectedAccount: GeneralLedgerAccount | null = null;
  loading = false;
  searchTerm = '';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;

  displayedColumns: string[] = ['date', 'reference', 'description', 'debit', 'credit', 'balance'];

  constructor() { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    
    // Mock data - Grand livre avec écritures
    setTimeout(() => {
      this.accounts = [
        {
          accountCode: '411',
          accountName: 'Clients',
          accountType: 'ASSET',
          openingBalance: 0,
          totalDebits: 205000,
          totalCredits: 80000,
          closingBalance: 125000,
          entries: [
            {
              id: 1,
              date: new Date('2024-01-15'),
              reference: 'JE-2024-000001',
              description: 'Facture client ABC Transport',
              debitAmount: 118000,
              creditAmount: 0,
              balance: 118000,
              journalEntryId: 1
            },
            {
              id: 2,
              date: new Date('2024-01-20'),
              reference: 'JE-2024-000003',
              description: 'Paiement client ABC Transport',
              debitAmount: 0,
              creditAmount: 50000,
              balance: 68000,
              journalEntryId: 3
            },
            {
              id: 3,
              date: new Date('2024-01-25'),
              reference: 'JE-2024-000005',
              description: 'Facture client XYZ Logistics',
              debitAmount: 87000,
              creditAmount: 0,
              balance: 155000,
              journalEntryId: 5
            },
            {
              id: 4,
              date: new Date('2024-01-30'),
              reference: 'JE-2024-000007',
              description: 'Paiement partiel XYZ Logistics',
              debitAmount: 0,
              creditAmount: 30000,
              balance: 125000,
              journalEntryId: 7
            }
          ]
        },
        {
          accountCode: '512',
          accountName: 'Banque',
          accountType: 'ASSET',
          openingBalance: 50000,
          totalDebits: 80000,
          totalCredits: 45000,
          closingBalance: 85000,
          entries: [
            {
              id: 5,
              date: new Date('2024-01-20'),
              reference: 'JE-2024-000003',
              description: 'Encaissement client ABC Transport',
              debitAmount: 50000,
              creditAmount: 0,
              balance: 100000,
              journalEntryId: 3
            },
            {
              id: 6,
              date: new Date('2024-01-22'),
              reference: 'JE-2024-000004',
              description: 'Paiement fournisseur carburant',
              debitAmount: 0,
              creditAmount: 25000,
              balance: 75000,
              journalEntryId: 4
            },
            {
              id: 7,
              date: new Date('2024-01-30'),
              reference: 'JE-2024-000007',
              description: 'Encaissement XYZ Logistics',
              debitAmount: 30000,
              creditAmount: 0,
              balance: 105000,
              journalEntryId: 7
            },
            {
              id: 8,
              date: new Date('2024-01-31'),
              reference: 'JE-2024-000008',
              description: 'Paiement salaires',
              debitAmount: 0,
              creditAmount: 20000,
              balance: 85000,
              journalEntryId: 8
            }
          ]
        },
        {
          accountCode: '701',
          accountName: 'Ventes de marchandises',
          accountType: 'REVENUE',
          openingBalance: 0,
          totalDebits: 0,
          totalCredits: 205000,
          closingBalance: -205000,
          entries: [
            {
              id: 9,
              date: new Date('2024-01-15'),
              reference: 'JE-2024-000001',
              description: 'Vente transport ABC',
              debitAmount: 0,
              creditAmount: 100000,
              balance: -100000,
              journalEntryId: 1
            },
            {
              id: 10,
              date: new Date('2024-01-25'),
              reference: 'JE-2024-000005',
              description: 'Vente transport XYZ',
              debitAmount: 0,
              creditAmount: 75000,
              balance: -175000,
              journalEntryId: 5
            },
            {
              id: 11,
              date: new Date('2024-01-28'),
              reference: 'JE-2024-000006',
              description: 'Services additionnels',
              debitAmount: 0,
              creditAmount: 30000,
              balance: -205000,
              journalEntryId: 6
            }
          ]
        }
      ];
      
      this.loading = false;
    }, 1000);
  }

  selectAccount(account: GeneralLedgerAccount): void {
    this.selectedAccount = account;
  }

  applyDateFilter(): void {
    if (!this.selectedAccount) return;
    
    let filteredEntries = [...this.selectedAccount.entries];
    
    if (this.dateFrom) {
      filteredEntries = filteredEntries.filter(entry => entry.date >= this.dateFrom!);
    }
    
    if (this.dateTo) {
      filteredEntries = filteredEntries.filter(entry => entry.date <= this.dateTo!);
    }
    
    // Recalculer les soldes pour les écritures filtrées
    let runningBalance = this.selectedAccount.openingBalance;
    filteredEntries.forEach(entry => {
      runningBalance += entry.debitAmount - entry.creditAmount;
      entry.balance = runningBalance;
    });
    
    this.selectedAccount.entries = filteredEntries;
  }

  clearFilters(): void {
    this.dateFrom = null;
    this.dateTo = null;
    this.searchTerm = '';
    if (this.selectedAccount) {
      // Recharger les données originales
      const originalAccount = this.accounts.find(acc => acc.accountCode === this.selectedAccount!.accountCode);
      if (originalAccount) {
        this.selectedAccount = { ...originalAccount };
      }
    }
  }

  exportLedger(): void {
    // TODO: Export du grand livre
  }

  printLedger(): void {
    window.print();
  }

  goBackToAccounts(): void {
    this.selectedAccount = null;
  }
}
