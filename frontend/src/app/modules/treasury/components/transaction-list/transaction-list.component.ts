import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TreasuryTransactionService } from '../../services/treasury-transaction.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss']
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];
  filteredTransactions: any[] = [];
  loading = true;
  
  // Filters
  searchTerm = '';
  typeFilter = 'all';
  dateFromFilter = '';
  dateToFilter = '';
  categoryFilter = 'all';
  categories: string[] = [];

  displayedColumns: string[] = ['reference', 'type', 'amount', 'transactionDate', 'description', 'category', 'cashBox', 'bankAccount', 'actions'];

  constructor(
    private transactionService: TreasuryTransactionService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
    this.loadCategories();
  }

  loadTransactions(): void {
    this.loading = true;
    this.transactionService.getAllTransactions().subscribe({
      next: (data) => {
        this.transactions = data.sort((a, b) => 
          new Date(b.transactionDate).getTime() - new Date(a.transactionDate).getTime()
        );
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.snackBar.open('Erreur lors du chargement des transactions', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.transactionService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  applyFilters(): void {
    this.filteredTransactions = this.transactions.filter(transaction => {
      const matchesSearch = !this.searchTerm || 
        transaction.reference.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        transaction.description.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (transaction.category && transaction.category.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesType = this.typeFilter === 'all' || transaction.type === this.typeFilter;

      const matchesDateFrom = !this.dateFromFilter || 
        new Date(transaction.transactionDate) >= new Date(this.dateFromFilter);

      const matchesDateTo = !this.dateToFilter || 
        new Date(transaction.transactionDate) <= new Date(this.dateToFilter);

      const matchesCategory = this.categoryFilter === 'all' || 
        transaction.category === this.categoryFilter;

      return matchesSearch && matchesType && matchesDateFrom && matchesDateTo && matchesCategory;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onTypeFilterChange(): void {
    this.applyFilters();
  }

  onDateFilterChange(): void {
    this.applyFilters();
  }

  onCategoryFilterChange(): void {
    this.applyFilters();
  }

  createTransaction(): void {
    this.router.navigate(['/treasury/transactions/new']);
  }

  editTransaction(transaction: any): void {
    this.router.navigate(['/treasury/transactions/edit', transaction.id]);
  }

  viewTransaction(transaction: any): void {
    this.router.navigate(['/treasury/transactions/detail', transaction.id]);
  }

  deleteTransaction(transaction: any): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer la transaction "${transaction.reference}" ?`)) {
      this.transactionService.deleteTransaction(transaction.id).subscribe({
        next: () => {
          this.transactions = this.transactions.filter(t => t.id !== transaction.id);
          this.applyFilters();
          this.snackBar.open('Transaction supprimée avec succès', 'Fermer', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error deleting transaction:', error);
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  getTransactionTypeColor(type: string): string {
    switch (type) {
      case 'INCOME': return 'primary';
      case 'EXPENSE': return 'warn';
      case 'TRANSFER': return 'accent';
      default: return 'basic';
    }
  }

  getTransactionTypeIcon(type: string): string {
    switch (type) {
      case 'INCOME': return 'trending_up';
      case 'EXPENSE': return 'trending_down';
      case 'TRANSFER': return 'swap_horiz';
      default: return 'account_balance';
    }
  }

  getTransactionTypeLabel(type: string): string {
    switch (type) {
      case 'INCOME': return 'Recette';
      case 'EXPENSE': return 'Dépense';
      case 'TRANSFER': return 'Transfert';
      default: return type;
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

  getAccountName(transaction: any): string {
    if (transaction.cashBox) {
      return transaction.cashBox.name;
    }
    if (transaction.bankAccount) {
      return transaction.bankAccount.accountName;
    }
    return '-';
  }

  getAccountType(transaction: any): string {
    if (transaction.cashBox) {
      return 'Caisse';
    }
    if (transaction.bankAccount) {
      return 'Banque';
    }
    return '-';
  }

  getTotalIncome(): number {
    return this.filteredTransactions
      .filter(t => t.type === 'INCOME')
      .reduce((sum, t) => sum + t.amount, 0);
  }

  getTotalExpense(): number {
    return this.filteredTransactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + t.amount, 0);
  }

  getNetFlow(): number {
    return this.getTotalIncome() - this.getTotalExpense();
  }

  exportToExcel(): void {
    // TODO: Implement Excel export
    this.snackBar.open('Export Excel en cours de développement', 'Fermer', { duration: 3000 });
  }
}
