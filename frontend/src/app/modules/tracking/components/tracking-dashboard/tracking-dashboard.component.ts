import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LTAService } from '../../../../core/services/lta.service';
import { LTA } from '../../../../models/lta.model';
import { LTAStatus } from '../../../../models/common.model';

@Component({
  selector: 'app-tracking-dashboard',
  templateUrl: './tracking-dashboard.component.html',
  styleUrls: ['./tracking-dashboard.component.scss']
})
export class TrackingDashboardComponent implements OnInit {
  searchTerm = '';
  trackingInfo: LTA | null = null;
  loading = false;
  searched = false;
  error: string | null = null;

  constructor(
    private ltaService: LTAService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check if tracking number is provided in URL
    const trackingNumber = this.route.snapshot.paramMap.get('trackingNumber');
    if (trackingNumber) {
      this.searchTerm = trackingNumber;
      this.onSearch();
    }
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) return;
    
    this.loading = true;
    this.searched = true;
    this.error = null;
    
    this.ltaService.getLTAByTrackingNumber(this.searchTerm.trim()).subscribe({
      next: (lta) => {
        this.trackingInfo = lta;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching tracking info:', error);
        this.error = 'LTA non trouvée. Vérifiez le numéro de suivi.';
        this.trackingInfo = null;
        this.loading = false;
      }
    });
  }

  getStatusLabel(status: LTAStatus): string {
    return this.ltaService.getLTAStatusOptions()
      .find(option => option.value === status)?.label || status;
  }

  getStatusClass(status: LTAStatus): string {
    const classMap: { [key in LTAStatus]: string } = {
      [LTAStatus.DRAFT]: 'status-draft',
      [LTAStatus.CONFIRMED]: 'status-confirmed',
      [LTAStatus.IN_TRANSIT]: 'status-transit',
      [LTAStatus.DELIVERED]: 'status-delivered',
      [LTAStatus.CANCELLED]: 'status-cancelled'
    };
    return classMap[status] || 'status-default';
  }

  getProgressPercentage(status: LTAStatus): number {
    const progressMap: { [key in LTAStatus]: number } = {
      [LTAStatus.DRAFT]: 10,
      [LTAStatus.CONFIRMED]: 25,
      [LTAStatus.IN_TRANSIT]: 65,
      [LTAStatus.DELIVERED]: 100,
      [LTAStatus.CANCELLED]: 0
    };
    return progressMap[status] || 0;
  }
}
