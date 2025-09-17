import { Component, OnInit } from '@angular/core';
import { AccountingService } from '../../services/accounting.service';
import { DashboardStats, BalanceSummary, AccountType } from '../../models/accounting.models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-accounting-dashboard',
  templateUrl: './accounting-dashboard.component.html',
  styleUrls: ['./accounting-dashboard.component.scss']
})
export class AccountingDashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  balanceSummary: BalanceSummary[] = [];
  loading = true;
  error: string | null = null;

  // Account type summary for display
  accountTypeSummary = {
    [AccountType.ASSET]: 0,
    [AccountType.LIABILITY]: 0,
    [AccountType.EQUITY]: 0,
    [AccountType.REVENUE]: 0,
    [AccountType.EXPENSE]: 0
  };

  accountTypes = Object.values(AccountType);

  // Interface pour les données d'affichage des types de comptes
  accountTypeDisplayData: Array<{
    type: AccountType;
    label: string;
    balance: number;
    cssClass: string;
  }> = [];

  constructor(private accountingService: AccountingService, private router: Router) {}

  ngOnInit(): void {
    this.initializeAccountTypeDisplayData();
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.loading = true;
    this.error = null;

    // Charger les statistiques du dashboard
    this.accountingService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
        this.error = 'Erreur lors du chargement des statistiques';
      }
    });

    // Charger le résumé des soldes
    this.accountingService.getBalanceSummary().subscribe({
      next: (balances) => {
        this.balanceSummary = balances;
        this.updateAccountTypeSummary(balances);
        this.updateAccountTypeDisplayData();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des soldes:', error);
        this.error = 'Erreur lors du chargement des soldes';
        this.loading = false;
      }
    });
  }

  private updateAccountTypeSummary(balances: BalanceSummary[]): void {
    // Reset summary
    Object.keys(this.accountTypeSummary).forEach(key => {
      this.accountTypeSummary[key as AccountType] = 0;
    });

    // Calculate totals by account type
    balances.forEach(balance => {
      this.accountTypeSummary[balance.accountType] += Math.abs(balance.balance);
    });
  }

  private initializeAccountTypeDisplayData(): void {
    this.accountTypeDisplayData = this.accountTypes.map(type => ({
      type,
      label: this.getAccountTypeLabel(type),
      balance: this.getAccountTypeBalance(type),
      cssClass: `type-${type.toLowerCase()}`
    }));
  }

  private updateAccountTypeDisplayData(): void {
    this.accountTypeDisplayData = this.accountTypes.map(type => ({
      type,
      label: this.getAccountTypeLabel(type),
      balance: this.getAccountTypeBalance(type),
      cssClass: `type-${type.toLowerCase()}`
    }));
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'CDF',
      minimumFractionDigits: 0
    }).format(amount);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'success': return '#4CAF50';
      case 'warning': return '#FF9800';
      case 'error': return '#F44336';
      default: return '#2196F3';
    }
  }

  getAccountTypeLabel(type: AccountType): string {
    switch (type) {
      case AccountType.ASSET: return 'Actifs';
      case AccountType.LIABILITY: return 'Passifs';
      case AccountType.EQUITY: return 'Capitaux';
      case AccountType.REVENUE: return 'Produits';
      case AccountType.EXPENSE: return 'Charges';
      default: return type;
    }
  }

  getAccountTypeBalance(type: AccountType): number {
    return this.accountTypeSummary[type];
  }

  refreshData(): void {
    this.loadDashboardData();
  }
}
