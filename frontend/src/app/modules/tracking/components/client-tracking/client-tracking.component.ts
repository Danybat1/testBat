import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { 
  Shipment, 
  ShipmentStatus, 
  SHIPMENT_STATUS_LABELS, 
  SHIPMENT_STATUS_COLORS 
} from '../../../../models/shipment.model';
import { ShipmentService } from '../../../../services/shipment.service';

@Component({
  selector: 'app-client-tracking',
  templateUrl: './client-tracking.component.html',
  styleUrls: ['./client-tracking.component.scss']
})
export class ClientTrackingComponent implements OnInit {
  trackingForm: FormGroup;
  shipment: Shipment | null = null;
  loading = false;
  searched = false;
  error: string | null = null;
  
  // Expose enums and constants to template
  ShipmentStatus = ShipmentStatus;
  statusLabels = SHIPMENT_STATUS_LABELS;
  statusColors = SHIPMENT_STATUS_COLORS;

  constructor(
    private fb: FormBuilder,
    private shipmentService: ShipmentService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.trackingForm = this.fb.group({
      trackingNumber: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  ngOnInit(): void {
    // Check if tracking number is provided in URL
    const trackingNumber = this.route.snapshot.paramMap.get('trackingNumber');
    if (trackingNumber) {
      this.trackingForm.patchValue({ trackingNumber });
      this.onSearch();
    }
  }

  onSearch(): void {
    if (this.trackingForm.invalid) {
      this.trackingForm.markAllAsTouched();
      return;
    }

    const trackingNumber = this.trackingForm.get('trackingNumber')?.value?.trim();
    if (!trackingNumber) return;

    this.loading = true;
    this.searched = true;
    this.error = null;
    this.shipment = null;

    this.shipmentService.trackShipment(trackingNumber).subscribe({
      next: (shipment) => {
        this.shipment = shipment;
        this.loading = false;
        
        // Update URL without reloading
        this.router.navigate(['/tracking', trackingNumber], { replaceUrl: true });
      },
      error: (error) => {
        console.error('Error tracking shipment:', error);
        this.error = 'Numéro de suivi introuvable. Vérifiez le numéro et réessayez.';
        this.shipment = null;
        this.loading = false;
      }
    });
  }

  onReset(): void {
    this.trackingForm.reset();
    this.shipment = null;
    this.searched = false;
    this.error = null;
    this.router.navigate(['/tracking'], { replaceUrl: true });
  }

  getStatusLabel(status: ShipmentStatus): string {
    return this.statusLabels[status] || status;
  }

  getStatusColor(status: ShipmentStatus): string {
    return this.statusColors[status] || '#6c757d';
  }

  getStatusProgress(): number {
    if (!this.shipment) return 0;
    return this.shipmentService.getStatusProgress(this.shipment.status);
  }

  isDelivered(): boolean {
    return this.shipment?.status === ShipmentStatus.DELIVERED;
  }

  isInTransit(): boolean {
    if (!this.shipment) return false;
    return [
      ShipmentStatus.PICKUP_SCHEDULED,
      ShipmentStatus.PICKED_UP,
      ShipmentStatus.IN_TRANSIT,
      ShipmentStatus.OUT_FOR_DELIVERY
    ].includes(this.shipment.status);
  }

  isException(): boolean {
    if (!this.shipment) return false;
    return this.shipmentService.isExceptionStatus(this.shipment.status);
  }

  isFinal(): boolean {
    if (!this.shipment) return false;
    return this.shipmentService.isFinalStatus(this.shipment.status);
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'Non défini';
    return new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  formatDateShort(date: Date | undefined): string {
    if (!date) return 'Non défini';
    return new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(date);
  }

  getEstimatedDelivery(): string {
    if (!this.shipment?.expectedDeliveryDate) return 'Non défini';
    
    const now = new Date();
    const expected = this.shipment.expectedDeliveryDate;
    const diffDays = Math.ceil((expected.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) {
      return 'Retard de livraison';
    } else if (diffDays === 0) {
      return 'Aujourd\'hui';
    } else if (diffDays === 1) {
      return 'Demain';
    } else {
      return `Dans ${diffDays} jours`;
    }
  }

  getProgressSteps(): Array<{status: ShipmentStatus, label: string, icon: string, completed: boolean, current: boolean, active: boolean}> {
    if (!this.shipment) return [];

    const allSteps = [
      { status: ShipmentStatus.PENDING, label: 'En attente', icon: 'fas fa-clock' },
      { status: ShipmentStatus.CONFIRMED, label: 'Confirmé', icon: 'fas fa-check-circle' },
      { status: ShipmentStatus.PICKED_UP, label: 'Enlevé', icon: 'fas fa-truck-pickup' },
      { status: ShipmentStatus.IN_TRANSIT, label: 'En transit', icon: 'fas fa-shipping-fast' },
      { status: ShipmentStatus.OUT_FOR_DELIVERY, label: 'En livraison', icon: 'fas fa-truck' },
      { status: ShipmentStatus.DELIVERED, label: 'Livré', icon: 'fas fa-box-check' }
    ];

    const currentStatusIndex = allSteps.findIndex(step => step.status === this.shipment!.status);
    
    return allSteps.map((step, index) => ({
      ...step,
      completed: index < currentStatusIndex,
      current: index === currentStatusIndex,
      active: index <= currentStatusIndex
    }));
  }

  trackAnother(): void {
    this.onReset();
  }
}
