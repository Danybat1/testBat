import { Component, OnInit } from '@angular/core';

export interface TrialBalanceAccount {
  accountCode: string;
  accountName: string;
  debitBalance: number;
  creditBalance: number;
  accountType: string;
}

@Component({
  selector: 'app-trial-balance',
  templateUrl: './trial-balance.component.html',
  styleUrls: ['./trial-balance.component.scss']
})
export class TrialBalanceComponent implements OnInit {
  accounts: TrialBalanceAccount[] = [];
  totalDebits = 0;
  totalCredits = 0;
  isBalanced = false;
  loading = false;

  displayedColumns: string[] = ['accountCode', 'accountName', 'debitBalance', 'creditBalance'];

  constructor() { }

  ngOnInit(): void {
    this.loadTrialBalance();
  }

  loadTrialBalance(): void {
    this.loading = true;
    
    // Mock data - sera remplacé par l'appel API
    setTimeout(() => {
      this.accounts = [
        { accountCode: '101', accountName: 'Capital social', debitBalance: 0, creditBalance: 500000, accountType: 'EQUITY' },
        { accountCode: '411', accountName: 'Clients', debitBalance: 125000, creditBalance: 0, accountType: 'ASSET' },
        { accountCode: '512', accountName: 'Banque', debitBalance: 85000, creditBalance: 0, accountType: 'ASSET' },
        { accountCode: '531', accountName: 'Caisse', debitBalance: 15000, creditBalance: 0, accountType: 'ASSET' },
        { accountCode: '701', accountName: 'Ventes de marchandises', debitBalance: 0, creditBalance: 180000, accountType: 'REVENUE' },
        { accountCode: '445', accountName: 'TVA collectée', debitBalance: 0, creditBalance: 32400, accountType: 'LIABILITY' },
        { accountCode: '607', accountName: 'Achats de marchandises', debitBalance: 95000, creditBalance: 0, accountType: 'EXPENSE' },
        { accountCode: '621', accountName: 'Personnel', debitBalance: 45000, creditBalance: 0, accountType: 'EXPENSE' },
        { accountCode: '622', accountName: 'Charges sociales', debitBalance: 12400, creditBalance: 0, accountType: 'EXPENSE' }
      ];
      
      this.calculateTotals();
      this.loading = false;
    }, 1000);
  }

  calculateTotals(): void {
    this.totalDebits = this.accounts.reduce((sum, account) => sum + account.debitBalance, 0);
    this.totalCredits = this.accounts.reduce((sum, account) => sum + account.creditBalance, 0);
    this.isBalanced = Math.abs(this.totalDebits - this.totalCredits) < 0.01;
  }

  exportToExcel(): void {
    // TODO: Implémenter l'export Excel
  }

  printBalance(): void {
    window.print();
  }
}
