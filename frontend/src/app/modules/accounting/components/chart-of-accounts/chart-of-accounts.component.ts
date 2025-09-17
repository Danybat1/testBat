import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

export interface Account {
  id: number;
  code: string;
  name: string;
  type: AccountType;
  parentId?: number;
  balance: number;
  isActive: boolean;
  level: number;
  children?: Account[];
}

export enum AccountType {
  ASSET = 'ASSET',
  LIABILITY = 'LIABILITY',
  EQUITY = 'EQUITY',
  REVENUE = 'REVENUE',
  EXPENSE = 'EXPENSE'
}

@Component({
  selector: 'app-chart-of-accounts',
  templateUrl: './chart-of-accounts.component.html',
  styleUrls: ['./chart-of-accounts.component.scss']
})
export class ChartOfAccountsComponent implements OnInit {
  accounts: Account[] = [];
  filteredAccounts: Account[] = [];
  searchTerm = '';
  selectedType: AccountType | 'ALL' = 'ALL';
  loading = false;
  expandedNodes = new Set<number>();

  displayedColumns: string[] = ['code', 'name', 'type', 'balance', 'status', 'actions'];
  accountTypes = Object.values(AccountType);

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    
    // Mock data - Plan comptable OHADA
    setTimeout(() => {
      this.accounts = [
        // Classe 1 - Comptes de ressources durables
        { id: 1, code: '10', name: 'CAPITAL ET RESERVES', type: AccountType.EQUITY, balance: 0, isActive: true, level: 0 },
        { id: 2, code: '101', name: 'Capital social', type: AccountType.EQUITY, parentId: 1, balance: 500000, isActive: true, level: 1 },
        { id: 3, code: '106', name: 'Réserves', type: AccountType.EQUITY, parentId: 1, balance: 125000, isActive: true, level: 1 },
        
        // Classe 2 - Comptes d'actif immobilisé
        { id: 4, code: '20', name: 'IMMOBILISATIONS INCORPORELLES', type: AccountType.ASSET, balance: 0, isActive: true, level: 0 },
        { id: 5, code: '21', name: 'IMMOBILISATIONS CORPORELLES', type: AccountType.ASSET, balance: 0, isActive: true, level: 0 },
        { id: 6, code: '211', name: 'Terrains', type: AccountType.ASSET, parentId: 5, balance: 200000, isActive: true, level: 1 },
        { id: 7, code: '213', name: 'Constructions', type: AccountType.ASSET, parentId: 5, balance: 350000, isActive: true, level: 1 },
        { id: 8, code: '218', name: 'Matériel de transport', type: AccountType.ASSET, parentId: 5, balance: 85000, isActive: true, level: 1 },
        
        // Classe 3 - Comptes de stocks
        { id: 9, code: '31', name: 'MARCHANDISES', type: AccountType.ASSET, balance: 45000, isActive: true, level: 0 },
        
        // Classe 4 - Comptes de tiers
        { id: 10, code: '41', name: 'CLIENTS ET COMPTES RATTACHES', type: AccountType.ASSET, balance: 0, isActive: true, level: 0 },
        { id: 11, code: '411', name: 'Clients', type: AccountType.ASSET, parentId: 10, balance: 125000, isActive: true, level: 1 },
        { id: 12, code: '44', name: 'ETAT ET COLLECTIVITES PUBLIQUES', type: AccountType.LIABILITY, balance: 0, isActive: true, level: 0 },
        { id: 13, code: '445', name: 'TVA collectée', type: AccountType.LIABILITY, parentId: 12, balance: 32400, isActive: true, level: 1 },
        
        // Classe 5 - Comptes de trésorerie
        { id: 14, code: '51', name: 'BANQUES', type: AccountType.ASSET, balance: 0, isActive: true, level: 0 },
        { id: 15, code: '512', name: 'Banque', type: AccountType.ASSET, parentId: 14, balance: 85000, isActive: true, level: 1 },
        { id: 16, code: '53', name: 'CAISSE', type: AccountType.ASSET, balance: 15000, isActive: true, level: 0 },
        
        // Classe 6 - Comptes de charges
        { id: 17, code: '60', name: 'ACHATS', type: AccountType.EXPENSE, balance: 0, isActive: true, level: 0 },
        { id: 18, code: '607', name: 'Achats de marchandises', type: AccountType.EXPENSE, parentId: 17, balance: 95000, isActive: true, level: 1 },
        { id: 19, code: '62', name: 'SERVICES EXTERIEURS', type: AccountType.EXPENSE, balance: 0, isActive: true, level: 0 },
        { id: 20, code: '621', name: 'Personnel', type: AccountType.EXPENSE, parentId: 19, balance: 45000, isActive: true, level: 1 },
        
        // Classe 7 - Comptes de produits
        { id: 21, code: '70', name: 'VENTES', type: AccountType.REVENUE, balance: 0, isActive: true, level: 0 },
        { id: 22, code: '701', name: 'Ventes de marchandises', type: AccountType.REVENUE, parentId: 21, balance: 180000, isActive: true, level: 1 }
      ];
      
      this.buildAccountHierarchy();
      this.applyFilters();
      this.loading = false;
    }, 1000);
  }

  buildAccountHierarchy(): void {
    const accountMap = new Map<number, Account>();
    this.accounts.forEach(account => accountMap.set(account.id, account));
    
    this.accounts.forEach(account => {
      if (account.parentId) {
        const parent = accountMap.get(account.parentId);
        if (parent) {
          if (!parent.children) parent.children = [];
          parent.children.push(account);
        }
      }
    });
  }

  applyFilters(): void {
    let filtered = this.accounts.filter(account => !account.parentId); // Root accounts only
    
    if (this.selectedType !== 'ALL') {
      filtered = filtered.filter(account => account.type === this.selectedType);
    }
    
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(account => 
        account.code.toLowerCase().includes(term) || 
        account.name.toLowerCase().includes(term)
      );
    }
    
    this.filteredAccounts = filtered;
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onTypeChange(): void {
    this.applyFilters();
  }

  toggleNode(accountId: number): void {
    if (this.expandedNodes.has(accountId)) {
      this.expandedNodes.delete(accountId);
    } else {
      this.expandedNodes.add(accountId);
    }
  }

  isExpanded(accountId: number): boolean {
    return this.expandedNodes.has(accountId);
  }

  getAccountTypeLabel(type: AccountType): string {
    const labels = {
      [AccountType.ASSET]: 'Actif',
      [AccountType.LIABILITY]: 'Passif',
      [AccountType.EQUITY]: 'Capitaux propres',
      [AccountType.REVENUE]: 'Produits',
      [AccountType.EXPENSE]: 'Charges'
    };
    return labels[type];
  }

  addAccount(): void {
    // TODO: Ouvrir dialog pour ajouter un compte
  }

  editAccount(account: Account): void {
    // TODO: Ouvrir dialog pour modifier un compte
  }

  deleteAccount(account: Account): void {
    // TODO: Confirmer et supprimer le compte
  }

  exportAccounts(): void {
    // TODO: Exporter le plan comptable
  }
}
