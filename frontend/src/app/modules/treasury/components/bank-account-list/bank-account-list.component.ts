import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BankAccountService } from '../../services/bank-account.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-bank-account-list',
  templateUrl: './bank-account-list.component.html',
  styleUrls: ['./bank-account-list.component.scss']
})
export class BankAccountListComponent implements OnInit {
  bankAccounts: any[] = [];
  filteredBankAccounts: any[] = [];
  loading = true;
  
  // Filters
  searchTerm = '';
  statusFilter = 'all';
  balanceFilter = 'all';
  bankFilter = 'all';

  displayedColumns: string[] = ['accountName', 'accountNumber', 'bankName', 'currentBalance', 'initialBalance', 'active', 'createdAt', 'actions'];

  constructor(
    private bankAccountService: BankAccountService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBankAccounts();
  }

  loadBankAccounts(): void {
    this.loading = true;
    this.bankAccountService.getAllBankAccounts().subscribe({
      next: (data) => {
        this.bankAccounts = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading bank accounts:', error);
        this.snackBar.open('Erreur lors du chargement des comptes bancaires', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredBankAccounts = this.bankAccounts.filter(account => {
      const matchesSearch = !this.searchTerm || 
        account.accountName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        account.accountNumber.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        account.bankName.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesStatus = this.statusFilter === 'all' ||
        (this.statusFilter === 'active' && account.active) ||
        (this.statusFilter === 'inactive' && !account.active);

      const matchesBalance = this.balanceFilter === 'all' ||
        (this.balanceFilter === 'positive' && account.currentBalance > 0) ||
        (this.balanceFilter === 'zero' && account.currentBalance === 0) ||
        (this.balanceFilter === 'negative' && account.currentBalance < 0) ||
        (this.balanceFilter === 'low' && account.currentBalance < 50000);

      const matchesBank = this.bankFilter === 'all' ||
        account.bankName === this.bankFilter;

      return matchesSearch && matchesStatus && matchesBalance && matchesBank;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
  }

  onBalanceFilterChange(): void {
    this.applyFilters();
  }

  onBankFilterChange(): void {
    this.applyFilters();
  }

  getUniqueBanks(): string[] {
    return [...new Set(this.bankAccounts.map(account => account.bankName))].sort();
  }

  createBankAccount(): void {
    this.router.navigate(['/treasury/bank-accounts/new']);
  }

  editBankAccount(account: any): void {
    this.router.navigate(['/treasury/bank-accounts/edit', account.id]);
  }

  viewBankAccount(account: any): void {
    this.router.navigate(['/treasury/bank-accounts/detail', account.id]);
  }

  toggleBankAccountStatus(account: any): void {
    const action = account.active ? 'deactivateBankAccount' : 'activateBankAccount';
    const message = account.active ? 'désactivé' : 'activé';

    this.bankAccountService[action](account.id).subscribe({
      next: (updatedAccount) => {
        const index = this.bankAccounts.findIndex(acc => acc.id === account.id);
        if (index !== -1) {
          this.bankAccounts[index] = updatedAccount;
          this.applyFilters();
        }
        this.snackBar.open(`Compte ${message} avec succès`, 'Fermer', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error toggling bank account status:', error);
        this.snackBar.open('Erreur lors de la modification du statut', 'Fermer', { duration: 3000 });
      }
    });
  }

  deleteBankAccount(account: any): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le compte "${account.accountName}" ?`)) {
      this.bankAccountService.deleteBankAccount(account.id).subscribe({
        next: () => {
          this.bankAccounts = this.bankAccounts.filter(acc => acc.id !== account.id);
          this.applyFilters();
          this.snackBar.open('Compte supprimé avec succès', 'Fermer', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error deleting bank account:', error);
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  adjustBalance(account: any): void {
    const amount = prompt('Montant d\'ajustement (positif ou négatif):');
    if (amount !== null && !isNaN(Number(amount))) {
      const reason = prompt('Raison de l\'ajustement:') || 'Ajustement manuel';
      
      this.bankAccountService.adjustBalance(account.id, Number(amount), reason).subscribe({
        next: (updatedAccount) => {
          const index = this.bankAccounts.findIndex(acc => acc.id === account.id);
          if (index !== -1) {
            this.bankAccounts[index] = updatedAccount;
            this.applyFilters();
          }
          this.snackBar.open('Solde ajusté avec succès', 'Fermer', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error adjusting balance:', error);
          this.snackBar.open('Erreur lors de l\'ajustement', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'XAF'
    }).format(amount);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }

  getBalanceColor(balance: number): string {
    if (balance < 0) return 'warn';
    if (balance < 50000) return 'accent';
    return 'primary';
  }

  exportToExcel(): void {
    // TODO: Implement Excel export
    this.snackBar.open('Export Excel en cours de développement', 'Fermer', { duration: 3000 });
  }
}
