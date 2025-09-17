import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

import { LTAService } from '../../../../services/lta.service';
import { CityService } from '../../../../services/city.service';
import { ClientService } from '../../../../services/client.service';
import { LTAResponse, LTASearchParams, LTAListResponse } from '../../../../models/lta.model';
import { LTAStatus, PaymentMode } from '../../../../models/common.model';
import { CitySearchResult } from '../../../../models/city.model';
import { ClientSearchResult } from '../../../../models/client.model';

@Component({
  selector: 'app-lta-list',
  templateUrl: './lta-list.component.html',
  styleUrls: ['./lta-list.component.scss']
})
export class LtaListComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = [
    'ltaNumber',
    'route',
    'status',
    'paymentMode',
    'client',
    'weight',
    'cost',
    'createdAt',
    'actions'
  ];

  dataSource: LTAResponse[] = [];
  loading = true;
  totalElements = 0;
  totalPages = 0;
  pageSize = 20;
  pageIndex = 0;

  filterForm: FormGroup;
  
  // Enums for template
  LTAStatus = LTAStatus;
  PaymentMode = PaymentMode;
  
  // Math object for template
  Math = Math;
  
  // Filter options
  statusOptions = Object.values(LTAStatus);
  paymentModeOptions = Object.values(PaymentMode);
  
  private destroy$ = new Subject<void>();

  constructor(
    private ltaService: LTAService,
    private cityService: CityService,
    private clientService: ClientService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder
  ) {
    this.filterForm = this.formBuilder.group({
      status: [''],
      paymentMode: [''],
      originCityId: [''],
      destinationCityId: [''],
      clientId: [''],
      dateFrom: [''],
      dateTo: [''],
      search: ['']
    });

    this.setupFilterSubscriptions();
  }

  ngOnInit(): void {
    this.loadLTAs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupFilterSubscriptions(): void {
    // Setup debounced search
    this.filterForm.get('search')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.applyFilter();
    });

    // Setup immediate filters for dropdowns
    this.filterForm.get('status')?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.applyFilter();
    });

    this.filterForm.get('paymentMode')?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.applyFilter();
    });
  }

  loadLTAs(): void {
    this.loading = true;
    
    const filters = this.filterForm.value;
    const searchParams: LTASearchParams = {
      page: this.pageIndex,
      size: this.pageSize,
      sort: 'createdAt',
      direction: 'DESC',
      status: filters.status || undefined,
      paymentMode: filters.paymentMode || undefined,
      originCityId: filters.originCityId || undefined,
      destinationCityId: filters.destinationCityId || undefined,
      clientId: filters.clientId || undefined,
      dateFrom: filters.dateFrom || undefined,
      dateTo: filters.dateTo || undefined,
      search: filters.search || undefined
    };

    this.ltaService.getLTAs(searchParams).subscribe({
      next: (response: LTAListResponse) => {
        this.dataSource = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des LTAs:', error);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    this.pageIndex = 0;
    this.loadLTAs();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.applyFilter();
  }

  onPageChange(pageIndex: number, pageSize?: number): void {
    this.pageIndex = pageIndex;
    if (pageSize) {
      this.pageSize = pageSize;
    }
    this.loadLTAs();
  }

  viewDetail(lta: LTAResponse): void {
    this.router.navigate(['../detail', lta.id], { relativeTo: this.route });
  }

  editLTA(lta: LTAResponse): void {
    this.router.navigate(['/lta/create'], { 
      queryParams: { edit: lta.id } 
    });
  }

  deleteLTA(lta: LTAResponse): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer la LTA ${lta.ltaNumber}?`)) {
      this.ltaService.deleteLTA(lta.id!).subscribe({
        next: () => {
          this.loadLTAs();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression de la LTA:', error);
        }
      });
    }
  }

  getStatusLabel(status: LTAStatus): string {
    const statusLabels: Record<LTAStatus, string> = {
      [LTAStatus.DRAFT]: 'Brouillon',
      [LTAStatus.CONFIRMED]: 'Confirmée',
      [LTAStatus.IN_TRANSIT]: 'En transit',
      [LTAStatus.DELIVERED]: 'Livrée',
      [LTAStatus.CANCELLED]: 'Annulée'
    };
    return statusLabels[status] || status;
  }

  getStatusClass(status: LTAStatus): string {
    const statusClasses: Record<LTAStatus, string> = {
      [LTAStatus.DRAFT]: 'bg-secondary text-white',
      [LTAStatus.CONFIRMED]: 'bg-primary text-white',
      [LTAStatus.IN_TRANSIT]: 'bg-warning text-dark',
      [LTAStatus.DELIVERED]: 'bg-success text-white',
      [LTAStatus.CANCELLED]: 'bg-danger text-white'
    };
    return statusClasses[status] || 'bg-secondary text-white';
  }

  getPaymentModeLabel(paymentMode: PaymentMode): string {
    const paymentLabels: Record<PaymentMode, string> = {
      [PaymentMode.CASH]: 'Espèces',
      [PaymentMode.TO_INVOICE]: 'À facturer',
      [PaymentMode.FREIGHT_COLLECT]: 'Port dû',
      [PaymentMode.FREE]: 'Gratuit'
    };
    return paymentLabels[paymentMode] || paymentMode;
  }

  trackByFn(index: number, item: LTAResponse): any {
    return item.id || index;
  }

  onCreateLTA(): void {
    this.router.navigate(['/lta/create']);
  }

  refreshData(): void {
    this.loadLTAs();
  }

  printLTA(lta: LTAResponse): void {
    if (!lta.id) {
      console.error('LTA ID is missing');
      return;
    }

    this.ltaService.downloadLTAPdf(lta.id).subscribe({
      next: (blob) => {
        // Create a blob URL and open in new window for printing
        const url = window.URL.createObjectURL(blob);
        const printWindow = window.open(url, '_blank');
        if (printWindow) {
          printWindow.onload = () => {
            printWindow.print();
          };
        }
        // Clean up the blob URL after a delay
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
      },
      error: (error) => {
        console.error('Erreur lors de l\'impression:', error);
      }
    });
  }

  downloadPDF(lta: LTAResponse): void {
    if (!lta.id) {
      console.error('LTA ID is missing');
      return;
    }

    this.ltaService.downloadLTAPdf(lta.id).subscribe({
      next: (blob) => {
        // Create a blob URL and trigger download
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `LTA-${lta.ltaNumber || lta.id}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        // You might want to show a user-friendly error message here
      }
    });
  }
}
