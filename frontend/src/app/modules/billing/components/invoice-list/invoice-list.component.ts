import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { InvoiceService } from '../../../../core/services/invoice.service';
import { Invoice } from '../../../../models/invoice.model';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.scss']
})
export class InvoiceListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = [
    'invoiceNumber', 
    'client', 
    'invoiceDate', 
    'dueDate', 
    'totalAmount', 
    'paidAmount', 
    'remainingAmount', 
    'status', 
    'actions'
  ];

  dataSource = new MatTableDataSource<Invoice>();
  loading = true;
  
  searchControl = new FormControl('');
  statusFilter = new FormControl('');
  typeFilter = new FormControl('');
  
  statusOptions = [
    { value: '', label: 'Tous les statuts' },
    { value: 'DRAFT', label: 'Brouillon' },
    { value: 'SENT', label: 'Envoyée' },
    { value: 'PAID', label: 'Payée' },
    { value: 'PARTIALLY_PAID', label: 'Partiellement payée' },
    { value: 'OVERDUE', label: 'En retard' },
    { value: 'CANCELLED', label: 'Annulée' }
  ];

  typeOptions = [
    { value: '', label: 'Tous les types' },
    { value: 'CLIENT', label: 'Client' },
    { value: 'SUPPLIER', label: 'Fournisseur' }
  ];

  constructor(
    private invoiceService: InvoiceService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
    this.setupFilters();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  private setupFilters(): void {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.applyFilters();
    });

    this.statusFilter.valueChanges.subscribe(() => {
      this.applyFilters();
    });

    this.typeFilter.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  private loadInvoices(): void {
    this.loading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: (invoices) => {
        this.dataSource.data = invoices;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading invoices:', error);
        this.snackBar.open('Erreur lors du chargement des factures', 'Fermer', {
          duration: 3000
        });
        this.loading = false;
      }
    });
  }

  private applyFilters(): void {
    this.dataSource.filterPredicate = (data: Invoice, filter: string) => {
      const searchTerm = this.searchControl.value?.toLowerCase() || '';
      const statusFilter = this.statusFilter.value || '';
      const typeFilter = this.typeFilter.value || '';

      const matchesSearch = !searchTerm || 
        data.invoiceNumber.toLowerCase().includes(searchTerm) ||
        data.client?.name?.toLowerCase().includes(searchTerm) ||
        data.notes?.toLowerCase().includes(searchTerm) ||
        data.items?.some(item => item.description?.toLowerCase().includes(searchTerm));

      const matchesStatus = !statusFilter || data.status === statusFilter;
      const matchesType = !typeFilter || data.type === typeFilter;

      return matchesSearch && matchesStatus && matchesType;
    };

    this.dataSource.filter = 'trigger';
  }

  viewInvoice(invoice: Invoice): void {
    try {
      this.router.navigate(['/billing/invoices', invoice.id]);
    } catch (error) {
      console.error('Error viewing invoice:', error);
      this.snackBar.open('Erreur lors de la visualisation de la facture', 'Fermer', {
        duration: 3000
      });
    }
  }

  editInvoice(invoice: Invoice): void {
    try {
      this.router.navigate(['/billing/invoices', invoice.id, 'edit']);
    } catch (error) {
      console.error('Error editing invoice:', error);
      this.snackBar.open('Erreur lors de l\'édition de la facture', 'Fermer', {
        duration: 3000
      });
    }
  }

  deleteInvoice(invoice: Invoice): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer la facture ${invoice.invoiceNumber} ?`)) {
      try {
        this.invoiceService.deleteInvoice(invoice.id!).subscribe({
          next: () => {
            this.snackBar.open('Facture supprimée avec succès', 'Fermer', {
              duration: 3000
            });
            this.loadInvoices();
          },
          error: (error) => {
            console.error('Error deleting invoice:', error);
            this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
              duration: 3000
            });
          }
        });
      } catch (error) {
        console.error('Error deleting invoice:', error);
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
          duration: 3000
        });
      }
    }
  }

  generatePdf(invoice: Invoice): void {
    this.invoiceService.generateInvoicePdf(invoice.id!).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `facture-${invoice.invoiceNumber}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.snackBar.open('PDF téléchargé avec succès', 'Fermer', {
          duration: 3000
        });
      },
      error: (error) => {
        console.error('Error generating PDF:', error);
        this.snackBar.open('Erreur lors de la génération du PDF', 'Fermer', {
          duration: 3000
        });
      }
    });
  }

  printInvoice(invoice: Invoice): void {
    this.invoiceService.generateInvoicePdf(invoice.id!).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const printWindow = window.open(url, '_blank');
        if (printWindow) {
          printWindow.onload = () => {
            printWindow.print();
          };
        } else {
          this.snackBar.open('Impossible d\'ouvrir la fenêtre d\'impression', 'Fermer', {
            duration: 3000
          });
        }
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error printing invoice:', error);
        this.snackBar.open('Erreur lors de l\'impression', 'Fermer', {
          duration: 3000
        });
      }
    });
  }

  exportToExcel(): void {
    this.invoiceService.exportInvoicesToExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `factures-${new Date().toISOString().split('T')[0]}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error exporting to Excel:', error);
        this.snackBar.open('Erreur lors de l\'export Excel', 'Fermer', {
          duration: 3000
        });
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PAID': return 'primary';
      case 'SENT': return 'accent';
      case 'OVERDUE': return 'warn';
      case 'DRAFT': return 'basic';
      case 'CANCELLED': return 'warn';
      default: return 'basic';
    }
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    return option ? option.label : status;
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

  clearFilters(): void {
    this.searchControl.setValue('');
    this.statusFilter.setValue('');
    this.typeFilter.setValue('');
  }
}
