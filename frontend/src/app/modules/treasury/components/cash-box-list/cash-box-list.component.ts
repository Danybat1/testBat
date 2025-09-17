import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CashBoxService } from '../../services/cash-box.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cash-box-list',
  templateUrl: './cash-box-list.component.html',
  styleUrls: ['./cash-box-list.component.scss']
})
export class CashBoxListComponent implements OnInit {
  cashBoxes: any[] = [];
  filteredCashBoxes: any[] = [];
  loading = true;
  
  // Filters
  searchTerm = '';
  statusFilter = 'all';
  balanceFilter = 'all';

  displayedColumns: string[] = ['name', 'description', 'currentBalance', 'initialBalance', 'active', 'createdAt', 'actions'];

  constructor(
    private cashBoxService: CashBoxService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCashBoxes();
  }

  loadCashBoxes(): void {
    this.loading = true;
    this.cashBoxService.getAllCashBoxes().subscribe({
      next: (data) => {
        this.cashBoxes = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading cash boxes:', error);
        this.snackBar.open('Erreur lors du chargement des caisses', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredCashBoxes = this.cashBoxes.filter(cashBox => {
      const matchesSearch = !this.searchTerm || 
        cashBox.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (cashBox.description && cashBox.description.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesStatus = this.statusFilter === 'all' ||
        (this.statusFilter === 'active' && cashBox.active) ||
        (this.statusFilter === 'inactive' && !cashBox.active);

      const matchesBalance = this.balanceFilter === 'all' ||
        (this.balanceFilter === 'positive' && cashBox.currentBalance > 0) ||
        (this.balanceFilter === 'zero' && cashBox.currentBalance === 0) ||
        (this.balanceFilter === 'negative' && cashBox.currentBalance < 0) ||
        (this.balanceFilter === 'low' && cashBox.currentBalance < 10000);

      return matchesSearch && matchesStatus && matchesBalance;
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

  createCashBox(): void {
    this.router.navigate(['/treasury/cash-boxes/new']);
  }

  editCashBox(cashBox: any): void {
    this.router.navigate(['/treasury/cash-boxes/edit', cashBox.id]);
  }

  viewCashBox(cashBox: any): void {
    this.router.navigate(['/treasury/cash-boxes/detail', cashBox.id]);
  }

  toggleCashBoxStatus(cashBox: any): void {
    const action = cashBox.active ? 'deactivateCashBox' : 'activateCashBox';
    const message = cashBox.active ? 'désactivée' : 'activée';

    this.cashBoxService[action](cashBox.id).subscribe({
      next: (updatedCashBox) => {
        const index = this.cashBoxes.findIndex(cb => cb.id === cashBox.id);
        if (index !== -1) {
          this.cashBoxes[index] = updatedCashBox;
          this.applyFilters();
        }
        this.snackBar.open(`Caisse ${message} avec succès`, 'Fermer', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error toggling cash box status:', error);
        this.snackBar.open('Erreur lors de la modification du statut', 'Fermer', { duration: 3000 });
      }
    });
  }

  deleteCashBox(cashBox: any): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer la caisse "${cashBox.name}" ?`)) {
      this.cashBoxService.deleteCashBox(cashBox.id).subscribe({
        next: () => {
          this.cashBoxes = this.cashBoxes.filter(cb => cb.id !== cashBox.id);
          this.applyFilters();
          this.snackBar.open('Caisse supprimée avec succès', 'Fermer', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error deleting cash box:', error);
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  adjustBalance(cashBox: any): void {
    const amount = prompt('Montant d\'ajustement (positif ou négatif):');
    if (amount !== null && !isNaN(Number(amount))) {
      const reason = prompt('Raison de l\'ajustement:') || 'Ajustement manuel';
      
      this.cashBoxService.adjustBalance(cashBox.id, Number(amount), reason).subscribe({
        next: (updatedCashBox) => {
          const index = this.cashBoxes.findIndex(cb => cb.id === cashBox.id);
          if (index !== -1) {
            this.cashBoxes[index] = updatedCashBox;
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
    if (balance < 10000) return 'accent';
    return 'primary';
  }

  exportToExcel(): void {
    // TODO: Implement Excel export
    this.snackBar.open('Export Excel en cours de développement', 'Fermer', { duration: 3000 });
  }
}
