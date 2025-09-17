import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccountingService } from '../../services/accounting.service';
import { JournalEntry, JournalEntryFilter, SourceType, EntryStatus } from '../../models/accounting.models';

@Component({
  selector: 'app-journal-entries',
  templateUrl: './journal-entries.component.html',
  styleUrls: ['./journal-entries.component.scss']
})
export class JournalEntriesComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = [
    'entryNumber',
    'entryDate',
    'description',
    'reference',
    'sourceType',
    'totalDebit',
    'totalCredit',
    'status',
    'actions'
  ];

  dataSource = new MatTableDataSource<JournalEntry>();
  loading = false;
  error: string | null = null;

  filterForm: FormGroup;
  sourceTypes = Object.values(SourceType);
  entryStatuses = Object.values(EntryStatus);

  constructor(
    private accountingService: AccountingService,
    private fb: FormBuilder,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.filterForm = this.fb.group({
      startDate: [null],
      endDate: [null],
      sourceType: [''],
      status: [''],
      reference: ['']
    });
  }

  ngOnInit(): void {
    this.loadJournalEntries();
    this.setupFilterSubscription();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  private setupFilterSubscription(): void {
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  private loadJournalEntries(): void {
    this.loading = true;
    this.error = null;

    const filter = this.buildFilter();

    this.accountingService.getJournalEntries(filter).subscribe({
      next: (entries) => {
        this.dataSource.data = entries;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des écritures:', error);
        this.error = 'Erreur lors du chargement des écritures comptables';
        this.loading = false;
        this.showError('Erreur lors du chargement des écritures comptables');
      }
    });
  }

  private buildFilter(): JournalEntryFilter {
    const formValue = this.filterForm.value;
    const filter: JournalEntryFilter = {};

    if (formValue.startDate) {
      filter.startDate = formValue.startDate;
    }
    if (formValue.endDate) {
      filter.endDate = formValue.endDate;
    }
    if (formValue.sourceType) {
      filter.sourceType = formValue.sourceType;
    }
    if (formValue.status) {
      filter.status = formValue.status;
    }
    if (formValue.reference?.trim()) {
      filter.reference = formValue.reference.trim();
    }

    return filter;
  }

  applyFilters(): void {
    this.loadJournalEntries();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.loadJournalEntries();
  }

  viewEntry(entry: JournalEntry): void {
    this.router.navigate(['/accounting/journal-entries', entry.id]);
  }

  postEntry(entry: JournalEntry): void {
    if (entry.status !== EntryStatus.DRAFT) {
      this.showError('Seules les écritures en brouillon peuvent être postées');
      return;
    }

    if (!entry.isBalanced) {
      this.showError('Cette écriture n\'est pas équilibrée et ne peut pas être postée');
      return;
    }

    this.accountingService.postJournalEntry(entry.id).subscribe({
      next: (updatedEntry) => {
        this.showSuccess('Écriture postée avec succès');
        this.loadJournalEntries();
      },
      error: (error) => {
        console.error('Erreur lors du postage:', error);
        this.showError('Erreur lors du postage de l\'écriture');
      }
    });
  }

  reverseEntry(entry: JournalEntry): void {
    if (entry.status !== EntryStatus.POSTED) {
      this.showError('Seules les écritures postées peuvent être contrepassées');
      return;
    }

    const reason = prompt('Motif de la contrepassation:');
    if (!reason?.trim()) {
      return;
    }

    this.accountingService.reverseJournalEntry(entry.id, reason.trim()).subscribe({
      next: (reversedEntry) => {
        this.showSuccess('Écriture contrepassée avec succès');
        this.loadJournalEntries();
      },
      error: (error) => {
        console.error('Erreur lors de la contrepassation:', error);
        this.showError('Erreur lors de la contrepassation de l\'écriture');
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'CDF',
      minimumFractionDigits: 0
    }).format(amount);
  }

  getStatusColor(status: EntryStatus): string {
    switch (status) {
      case EntryStatus.DRAFT:
        return '#FF9800';
      case EntryStatus.POSTED:
        return '#4CAF50';
      case EntryStatus.REVERSED:
        return '#F44336';
      default:
        return '#666';
    }
  }

  getStatusLabel(status: EntryStatus): string {
    switch (status) {
      case EntryStatus.DRAFT:
        return 'Brouillon';
      case EntryStatus.POSTED:
        return 'Postée';
      case EntryStatus.REVERSED:
        return 'Contrepassée';
      default:
        return status;
    }
  }

  getSourceTypeLabel(sourceType: SourceType): string {
    switch (sourceType) {
      case SourceType.INVOICE:
        return 'Facture';
      case SourceType.PAYMENT:
        return 'Paiement';
      case SourceType.LTA:
        return 'LTA';
      case SourceType.TREASURY:
        return 'Trésorerie';
      case SourceType.MANUAL:
        return 'Manuel';
      case SourceType.ADJUSTMENT:
        return 'Ajustement';
      case SourceType.OPENING:
        return 'Ouverture';
      case SourceType.CLOSING:
        return 'Clôture';
      default:
        return sourceType;
    }
  }

  canPost(entry: JournalEntry): boolean {
    return entry.status === EntryStatus.DRAFT && entry.isBalanced;
  }

  canReverse(entry: JournalEntry): boolean {
    return entry.status === EntryStatus.POSTED;
  }

  refreshData(): void {
    this.loadJournalEntries();
  }

  exportData(): void {
    // TODO: Implémenter l'export des données
    this.showInfo('Fonctionnalité d\'export en cours de développement');
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }

  private showInfo(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: ['info-snackbar']
    });
  }

  getTotalDebit(): string {
    const total = this.dataSource.data.reduce((sum, entry) => sum + entry.totalDebit, 0);
    return this.formatCurrency(total);
  }

  getTotalCredit(): string {
    const total = this.dataSource.data.reduce((sum, entry) => sum + entry.totalCredit, 0);
    return this.formatCurrency(total);
  }

  getUnbalancedCount(): number {
    return this.dataSource.data.filter(entry => !entry.isBalanced).length;
  }
}
