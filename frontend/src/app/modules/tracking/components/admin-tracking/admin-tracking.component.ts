import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { 
  Shipment, 
  TrackingEvent, 
  ShipmentStatus, 
  ServiceType,
  ShipmentSearchRequest,
  ShipmentStatistics,
  SHIPMENT_STATUS_LABELS,
  SHIPMENT_STATUS_COLORS,
  SERVICE_TYPE_LABELS
} from '../../../../models/shipment.model';
import { ShipmentService } from '../../../../services/shipment.service';

@Component({
  selector: 'app-admin-tracking',
  templateUrl: './admin-tracking.component.html',
  styleUrls: ['./admin-tracking.component.scss']
})
export class AdminTrackingComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Data sources
  dataSource = new MatTableDataSource<Shipment>();
  
  // Forms
  searchForm: FormGroup;
  createShipmentForm: FormGroup;
  
  // State
  loading = false;
  selectedShipment: Shipment | null = null;
  trackingEvents: TrackingEvent[] = [];
  statistics: ShipmentStatistics | null = null;
  
  // UI Configuration
  displayedColumns: string[] = [
    'trackingNumber',
    'senderName', 
    'recipientName',
    'status',
    'serviceType',
    'pickupDate',
    'expectedDelivery',
    'actions'
  ];
  
  // Enums for templates
  ShipmentStatus = ShipmentStatus;
  ServiceType = ServiceType;
  SHIPMENT_STATUS_LABELS = SHIPMENT_STATUS_LABELS;
  SHIPMENT_STATUS_COLORS = SHIPMENT_STATUS_COLORS;
  SERVICE_TYPE_LABELS = SERVICE_TYPE_LABELS;
  
  // Filter options
  statusOptions = Object.values(ShipmentStatus);
  serviceTypeOptions = Object.values(ServiceType);
  
  constructor(
    private shipmentService: ShipmentService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = this.createSearchForm();
    this.createShipmentForm = this.initCreateShipmentForm();
  }

  ngOnInit(): void {
    this.loadShipments();
    this.loadStatistics();
    this.setupTableConfiguration();
  }

  private createSearchForm(): FormGroup {
    return this.fb.group({
      trackingNumber: [''],
      clientId: [''],
      status: [''],
      serviceType: [''],
      senderName: [''],
      recipientName: [''],
      pickupDateFrom: [''],
      pickupDateTo: [''],
      deliveryDateFrom: [''],
      deliveryDateTo: ['']
    });
  }

  private initCreateShipmentForm(): FormGroup {
    return this.fb.group({
      senderName: ['', Validators.required],
      senderAddress: ['', Validators.required],
      senderPhone: ['', Validators.required],
      senderEmail: ['', [Validators.email]],
      recipientName: ['', Validators.required],
      recipientAddress: ['', Validators.required],
      recipientPhone: ['', Validators.required],
      recipientEmail: ['', [Validators.email]],
      serviceType: [ServiceType.STANDARD, Validators.required],
      packageDescription: ['', Validators.required],
      packageWeight: ['', [Validators.required, Validators.min(0.1)]],
      packageVolume: [''],
      declaredValue: ['', [Validators.min(0)]],
      specialInstructions: [''],
      clientId: ['']
    });
  }

  private setupTableConfiguration(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    
    // Custom filter predicate
    this.dataSource.filterPredicate = (data: Shipment, filter: string) => {
      const searchTerms = JSON.parse(filter);
      
      return (!searchTerms.trackingNumber || 
              data.trackingNumber.toLowerCase().includes(searchTerms.trackingNumber.toLowerCase())) &&
             (!searchTerms.senderName || 
              data.senderName.toLowerCase().includes(searchTerms.senderName.toLowerCase())) &&
             (!searchTerms.recipientName || 
              data.recipientName.toLowerCase().includes(searchTerms.recipientName.toLowerCase())) &&
             (!searchTerms.status || data.status === searchTerms.status) &&
             (!searchTerms.serviceType || data.serviceType === searchTerms.serviceType);
    };
  }

  loadShipments(): void {
    this.loading = true;
    
    const searchRequest: ShipmentSearchRequest = {
      page: 0,
      size: 100,
      ...this.searchForm.value
    };

    this.shipmentService.searchShipments(searchRequest).subscribe({
      next: (response) => {
        this.dataSource.data = response.content || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des colis:', error);
        this.showError('Erreur lors du chargement des colis');
        this.loading = false;
      }
    });
  }

  loadStatistics(): void {
    this.shipmentService.getShipmentStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    });
  }

  onSearch(): void {
    this.loadShipments();
  }

  onReset(): void {
    this.searchForm.reset();
    this.loadShipments();
  }

  onSelectShipment(shipment: Shipment): void {
    this.selectedShipment = shipment;
    this.loadTrackingEvents(shipment.id!);
  }

  private loadTrackingEvents(shipmentId: number): void {
    this.shipmentService.getTrackingEvents(shipmentId).subscribe({
      next: (events) => {
        this.trackingEvents = events;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des événements:', error);
        this.showError('Erreur lors du chargement des événements de suivi');
      }
    });
  }

  onCreateShipment(): void {
    if (this.createShipmentForm.valid) {
      this.loading = true;
      
      this.shipmentService.createShipment(this.createShipmentForm.value).subscribe({
        next: (shipment) => {
          this.showSuccess(`Colis créé avec succès. N° de suivi: ${shipment.trackingNumber}`);
          this.createShipmentForm.reset();
          this.loadShipments();
          this.loadStatistics();
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors de la création du colis:', error);
          this.showError('Erreur lors de la création du colis');
          this.loading = false;
        }
      });
    } else {
      this.markFormGroupTouched(this.createShipmentForm);
    }
  }

  onUpdateStatus(shipment: Shipment, newStatus: ShipmentStatus): void {
    const description = `Statut mis à jour vers ${this.getStatusLabel(newStatus)}`;
    this.shipmentService.updateShipmentStatus(shipment.id!, newStatus, description).subscribe({
      next: (updatedShipment) => {
        const index = this.dataSource.data.findIndex(s => s.id === shipment.id);
        if (index !== -1) {
          this.dataSource.data[index] = updatedShipment;
          this.dataSource._updateChangeSubscription();
        }
        if (this.selectedShipment?.id === shipment.id) {
          this.selectedShipment = updatedShipment;
          this.loadTrackingEvents(shipment.id!);
        }
        this.showSuccess('Statut mis à jour avec succès');
        this.loadStatistics();
      },
      error: (error) => {
        console.error('Erreur lors de la mise à jour du statut:', error);
        this.showError('Erreur lors de la mise à jour du statut');
      }
    });
  }

  onAddTrackingEvent(shipment: Shipment): void {
    // This would open a dialog to add a new tracking event
    // Implementation depends on your dialog component
    console.log('Add tracking event for shipment:', shipment.trackingNumber);
  }

  onPrintLabel(shipment: Shipment): void {
    // Generate and print shipping label
    this.generateShippingLabel(shipment);
  }

  onDeleteShipment(shipment: Shipment): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le colis ${shipment.trackingNumber} ?`)) {
      this.shipmentService.deleteShipment(shipment.id!).subscribe({
        next: () => {
          this.dataSource.data = this.dataSource.data.filter(s => s.id !== shipment.id);
          this.showSuccess('Colis supprimé avec succès');
          this.loadStatistics();
          
          if (this.selectedShipment?.id === shipment.id) {
            this.selectedShipment = null;
            this.trackingEvents = [];
          }
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          this.showError('Erreur lors de la suppression du colis');
        }
      });
    }
  }

  // Utility methods
  getStatusLabel(status: ShipmentStatus): string {
    return SHIPMENT_STATUS_LABELS[status] || status;
  }

  getStatusColor(status: ShipmentStatus): string {
    return SHIPMENT_STATUS_COLORS[status] || 'primary';
  }

  getServiceTypeLabel(serviceType: ServiceType): string {
    return SERVICE_TYPE_LABELS[serviceType] || serviceType;
  }

  isOverdue(shipment: Shipment): boolean {
    if (!shipment.expectedDeliveryDate || shipment.status === ShipmentStatus.DELIVERED) {
      return false;
    }
    
    const expectedDate = new Date(shipment.expectedDeliveryDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    return expectedDate < today;
  }

  isInTransit(shipment: Shipment): boolean {
    return [
      ShipmentStatus.PICKED_UP,
      ShipmentStatus.IN_TRANSIT,
      ShipmentStatus.OUT_FOR_DELIVERY
    ].includes(shipment.status);
  }

  formatDate(date: string | Date | null): string {
    if (!date) return '-';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(date: string | Date | null): string {
    if (!date) return '-';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private generateShippingLabel(shipment: Shipment): void {
    const labelHtml = this.createLabelHtml(shipment);
    const printWindow = window.open('', '_blank');
    
    if (printWindow) {
      printWindow.document.write(labelHtml);
      printWindow.document.close();
      printWindow.print();
    }
  }

  private createLabelHtml(shipment: Shipment): string {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Étiquette d'expédition - ${shipment.trackingNumber}</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; }
          .label { border: 2px solid #000; padding: 20px; max-width: 400px; }
          .header { text-align: center; font-weight: bold; font-size: 18px; margin-bottom: 20px; }
          .tracking { text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; }
          .section { margin: 15px 0; }
          .section-title { font-weight: bold; text-decoration: underline; }
          .barcode { text-align: center; font-family: 'Courier New', monospace; font-size: 20px; margin: 20px 0; }
        </style>
      </head>
      <body>
        <div class="label">
          <div class="header">FREIGHTOPS - ÉTIQUETTE D'EXPÉDITION</div>
          
          <div class="tracking">${shipment.trackingNumber}</div>
          
          <div class="section">
            <div class="section-title">EXPÉDITEUR:</div>
            <div>${shipment.senderName}</div>
            <div>${shipment.senderAddress}</div>
            <div>${shipment.senderPhone}</div>
          </div>
          
          <div class="section">
            <div class="section-title">DESTINATAIRE:</div>
            <div>${shipment.recipientName}</div>
            <div>${shipment.recipientAddress}</div>
            <div>${shipment.recipientPhone}</div>
          </div>
          
          <div class="section">
            <div class="section-title">SERVICE:</div>
            <div>${shipment.serviceType ? this.getServiceTypeLabel(shipment.serviceType) : 'Non défini'}</div>
          </div>
          
          <div class="barcode">||||| ${shipment.trackingNumber} |||||</div>
        </div>
      </body>
      </html>
    `;
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
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
}
