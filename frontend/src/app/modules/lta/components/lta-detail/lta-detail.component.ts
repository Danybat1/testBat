import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LTAService } from '../../../../core/services/lta.service';
import { LTAStatus, PaymentMode } from '../../../../models/common.model';
import { LTA } from '../../../../models/lta.model';

@Component({
  selector: 'app-lta-detail',
  templateUrl: './lta-detail.component.html',
  styleUrls: ['./lta-detail.component.scss']
})
export class LtaDetailComponent implements OnInit {
  lta: LTA | null = null;
  loading = true;
  statusOptions = [
    { value: LTAStatus.DRAFT, label: 'Brouillon' },
    { value: LTAStatus.CONFIRMED, label: 'Confirmée' },
    { value: LTAStatus.IN_TRANSIT, label: 'En transit' },
    { value: LTAStatus.DELIVERED, label: 'Livrée' },
    { value: LTAStatus.CANCELLED, label: 'Annulée' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ltaService: LTAService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadLTA(+id);
    } else {
      this.router.navigate(['/lta/list']);
    }
  }

  private loadLTA(id: number): void {
    console.log('🔍 === CHARGEMENT DÉTAILS LTA ===');
    console.log('🔍 ID LTA à charger:', id);
    console.log('🔍 URL endpoint appelée: /api/lta/' + id);
    
    this.loading = true;
    this.ltaService.getLTAById(id).subscribe({
      next: (lta) => {
        console.log('✅ LTA chargée avec succès:', lta);
        console.log('✅ Type de réponse:', typeof lta);
        console.log('✅ Structure complète:', JSON.stringify(lta, null, 2));
        
        // Vérifier si c'est une ApiResponse wrappée
        if (lta && (lta as any).data) {
          console.log('✅ Données extraites de ApiResponse:', (lta as any).data);
          this.lta = (lta as any).data;
        } else {
          console.log('✅ Données directes:', lta);
          this.lta = lta;
        }
        
        console.log('✅ LTA finale assignée:', this.lta);
        console.log('✅ ID:', this.lta?.id);
        console.log('✅ Numéro LTA:', this.lta?.ltaNumber);
        console.log('✅ Tracking Number:', this.lta?.trackingNumber);
        console.log('✅ Statut:', this.lta?.status);
        console.log('✅ Ville origine:', this.lta?.originCity);
        console.log('✅ Ville destination:', this.lta?.destinationCity);
        console.log('✅ Client:', this.lta?.client);
        
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement de la LTA:', error);
        console.error('❌ Status HTTP:', error.status);
        console.error('❌ Status Text:', error.statusText);
        console.error('❌ Message d\'erreur:', error.message);
        console.error('❌ URL appelée:', error.url);
        console.error('❌ Détails erreur backend:', error.error);
        
        this.router.navigate(['/lta/list']);
        this.loading = false;
      }
    });
  }

  editLTA(): void {
    if (this.lta) {
      this.router.navigate(['/lta/create'], { 
        queryParams: { edit: this.lta.id } 
      });
    }
  }

  updateStatus(newStatus: LTAStatus): void {
    if (this.lta && this.lta.id) {
      this.ltaService.updateLTAStatus(this.lta.id, newStatus).subscribe({
        next: (updatedLTA) => {
          this.lta = updatedLTA;
          console.log('LTA status updated successfully:', updatedLTA);
        },
        error: (error) => {
          console.error('Erreur lors de la mise à jour du statut:', error);
        }
      });
    }
  }

  deleteLTA(): void {
    if (this.lta && confirm(`Êtes-vous sûr de vouloir supprimer le LTA ${this.lta.ltaNumber}?`)) {
      this.ltaService.deleteLTA(this.lta.id!).subscribe({
        next: () => {
          this.router.navigate(['/lta/list']);
        },
        error: (error) => {
          console.error('Erreur lors de la suppression de la LTA:', error);
        }
      });
    }
  }

  getStatusLabel(status: LTAStatus): string {
    const option = this.statusOptions.find((opt: any) => opt.value === status);
    return option ? option.label : status;
  }

  getStatusClass(status: LTAStatus): string {
    const statusClasses: { [key in LTAStatus]: string } = {
      [LTAStatus.DRAFT]: 'bg-secondary text-white',
      [LTAStatus.CONFIRMED]: 'bg-info text-white',
      [LTAStatus.IN_TRANSIT]: 'bg-primary text-white',
      [LTAStatus.DELIVERED]: 'bg-success text-white',
      [LTAStatus.CANCELLED]: 'bg-danger text-white'
    };
    return statusClasses[status] || 'bg-secondary text-white';
  }

  getPaymentModeLabel(paymentMode: PaymentMode): string {
    const paymentModeLabels: { [key in PaymentMode]: string } = {
      [PaymentMode.CASH]: 'Espèces',
      [PaymentMode.TO_INVOICE]: 'À facturer',
      [PaymentMode.FREIGHT_COLLECT]: 'Port dû',
      [PaymentMode.FREE]: 'Gratuit'
    };
    return paymentModeLabels[paymentMode] || paymentMode;
  }

  goBack(): void {
    this.router.navigate(['/lta/list']);
  }

  downloadPDF(): void {
    if (!this.lta?.id) {
      console.error('LTA ID is required for PDF generation');
      return;
    }

    this.ltaService.downloadLTAPdf(this.lta.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `LTA-${this.lta?.ltaNumber || this.lta?.id}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Erreur lors du téléchargement du PDF:', error);
      }
    });
  }

  /**
   * Get QR code data for tracking
   */
  getQRCodeData(): string {
    if (this.lta?.trackingNumber) {
      return this.ltaService.generateQRCodeData(this.lta.trackingNumber);
    }
    return '';
  }

  /**
   * Get tracking URL for display
   */
  getTrackingUrl(): string {
    if (this.lta?.trackingNumber) {
      return this.ltaService.getTrackingUrl(this.lta.trackingNumber);
    }
    return '';
  }

  /**
   * Copy tracking URL to clipboard
   */
  copyTrackingUrl(): void {
    const url = this.getTrackingUrl();
    if (url) {
      navigator.clipboard.writeText(url).then(() => {
        // URL copied silently
      }).catch(err => {
        console.error('Erreur lors de la copie:', err);
      });
    }
  }

  /**
   * Download QR code as image
   */
  downloadQRCode(): void {
    const qrElement = document.querySelector('qrcode');
    if (qrElement) {
      const canvas = qrElement.querySelector('canvas') as HTMLCanvasElement;
      if (canvas) {
        const link = document.createElement('a');
        link.download = `QR-LTA-${this.lta?.ltaNumber || this.lta?.id}.png`;
        link.href = canvas.toDataURL();
        link.click();
      }
    }
  }
}
