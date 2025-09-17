import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';

import { TrackingService } from '../../services/tracking.service';
import { PublicTrackingResponse } from '../../models/public-tracking-response.model';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatChipsModule
  ],
  template: `
    <div class="tracking-container">
      <div class="container">
        <!-- Search Section -->
        <mat-card class="search-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>search</mat-icon>
              Suivre votre colis
            </mat-card-title>
            <mat-card-subtitle>
              Entrez votre numéro de suivi pour connaître l'état de votre envoi
            </mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <div class="search-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Numéro de suivi</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="trackingNumber" 
                  placeholder="Ex: TRK42630594TSCN"
                  (keyup.enter)="trackShipment()"
                  [disabled]="loading">
                <mat-icon matSuffix>local_shipping</mat-icon>
              </mat-form-field>
              
              <button 
                mat-raised-button 
                color="primary" 
                (click)="trackShipment()"
                [disabled]="!trackingNumber || loading"
                class="search-button">
                <mat-icon>search</mat-icon>
                Suivre
              </button>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Loading -->
        <div *ngIf="loading" class="loading-section">
          <mat-spinner></mat-spinner>
          <p>Recherche en cours...</p>
        </div>

        <!-- Error Message -->
        <mat-card *ngIf="error && !loading" class="error-card">
          <mat-card-content>
            <div class="error-content">
              <mat-icon color="warn">error</mat-icon>
              <div>
                <h3>Erreur</h3>
                <p>{{ error }}</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Tracking Results -->
        <mat-card *ngIf="trackingResult && !loading && !error" class="result-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>package_2</mat-icon>
              Informations de suivi
            </mat-card-title>
          </mat-card-header>
          
          <mat-card-content>
            <!-- Status Progress -->
            <div class="status-section">
              <div class="status-header">
                <mat-chip-set>
                  <mat-chip [style.background-color]="getStatusColor(trackingResult.status)" [style.color]="'white'">
                    {{ getStatusLabel(trackingResult.status) }}
                  </mat-chip>
                </mat-chip-set>
                <span class="progress-text">{{ getProgressPercentage(trackingResult.status) }}%</span>
              </div>
              <mat-progress-bar 
                mode="determinate" 
                [value]="getProgressPercentage(trackingResult.status)"
                [color]="getProgressColor(trackingResult.status)">
              </mat-progress-bar>
            </div>

            <!-- Shipment Details -->
            <div class="details-grid">
              <div class="detail-item">
                <mat-icon>confirmation_number</mat-icon>
                <div>
                  <strong>Numéro de suivi</strong>
                  <p>{{ trackingResult.trackingNumber }}</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>receipt</mat-icon>
                <div>
                  <strong>Numéro LTA</strong>
                  <p>{{ trackingResult.ltaNumber }}</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>place</mat-icon>
                <div>
                  <strong>Origine → Destination</strong>
                  <p>{{ trackingResult.originCity.name }} ({{ trackingResult.originCity.iataCode }}) → {{ trackingResult.destinationCity.name }} ({{ trackingResult.destinationCity.iataCode }})</p>
                  <small>{{ trackingResult.originCity.country }} → {{ trackingResult.destinationCity.country }}</small>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>person</mat-icon>
                <div>
                  <strong>Expéditeur</strong>
                  <p>{{ trackingResult.shipperName }}</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>person_outline</mat-icon>
                <div>
                  <strong>Destinataire</strong>
                  <p>{{ trackingResult.consigneeName }}</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>scale</mat-icon>
                <div>
                  <strong>Poids total</strong>
                  <p>{{ trackingResult.totalWeight }} kg</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>inventory_2</mat-icon>
                <div>
                  <strong>Nombre de colis</strong>
                  <p>{{ trackingResult.packageCount }}</p>
                </div>
              </div>

              <div class="detail-item">
                <mat-icon>schedule</mat-icon>
                <div>
                  <strong>Date de création</strong>
                  <p>{{ formatDate(trackingResult.createdAt) }}</p>
                </div>
              </div>

              <div class="detail-item" *ngIf="trackingResult.estimatedDelivery">
                <mat-icon>event</mat-icon>
                <div>
                  <strong>Livraison estimée</strong>
                  <p>{{ formatDate(trackingResult.estimatedDelivery) }}</p>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Status History Timeline -->
        <mat-card *ngIf="trackingResult && trackingResult.statusHistory && trackingResult.statusHistory.length > 0 && !loading && !error" class="history-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>timeline</mat-icon>
              Historique du suivi
            </mat-card-title>
            <mat-card-subtitle>
              Chronologie complète des changements de statut
            </mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <div class="timeline">
              <div *ngFor="let historyItem of trackingResult.statusHistory; let i = index" 
                   class="timeline-item" 
                   [class.current]="i === trackingResult.statusHistory.length - 1">
                
                <div class="timeline-marker">
                  <mat-icon [style.color]="getStatusColor(historyItem.newStatus)">
                    {{ getStatusIcon(historyItem.newStatus) }}
                  </mat-icon>
                </div>
                
                <div class="timeline-content">
                  <div class="timeline-header">
                    <h4>{{ historyItem.statusLabel }}</h4>
                    <span class="timeline-date">{{ formatDate(historyItem.changedAt) }}</span>
                  </div>
                  
                  <p class="timeline-description">{{ historyItem.statusDescription }}</p>
                  
                  <div class="timeline-meta">
                    <span class="changed-by">
                      <mat-icon>person</mat-icon>
                      Modifié par: {{ getChangedByLabel(historyItem.changedBy) }}
                    </span>
                    <span *ngIf="historyItem.changeReason" class="change-reason">
                      <mat-icon>info</mat-icon>
                      {{ historyItem.changeReason }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Help Section -->
        <mat-card class="help-card">
          <mat-card-content>
            <h3><mat-icon>help</mat-icon> Besoin d'aide ?</h3>
            <p>Si vous ne trouvez pas votre colis ou si vous avez des questions, contactez notre service client :</p>
            <div class="contact-info">
              <p><mat-icon>phone</mat-icon> +33 1 23 45 67 89</p>
              <p><mat-icon>email</mat-icon> support&#64;freightops.com</p>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .tracking-container {
      min-height: 100%;
      padding: 2rem 0;
    }

    .container {
      max-width: 800px;
      margin: 0 auto;
      padding: 0 1rem;
    }

    .search-card {
      margin-bottom: 2rem;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
    }

    .search-form {
      display: flex;
      gap: 1rem;
      align-items: flex-end;
    }

    .full-width {
      flex: 1;
    }

    .search-button {
      height: 56px;
      min-width: 120px;
    }

    .loading-section {
      text-align: center;
      padding: 2rem;
      color: white;
    }

    .loading-section mat-spinner {
      margin: 0 auto 1rem auto;
    }

    .error-card {
      margin-bottom: 2rem;
      background: rgba(255, 255, 255, 0.95);
    }

    .error-content {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .error-content h3 {
      margin: 0 0 0.5rem 0;
      color: #f44336;
    }

    .error-content p {
      margin: 0;
    }

    .result-card {
      margin-bottom: 2rem;
      background: rgba(255, 255, 255, 0.95);
    }

    .status-section {
      margin-bottom: 2rem;
    }

    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .progress-text {
      font-weight: 500;
      color: #666;
    }

    .details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
    }

    .detail-item {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
      padding: 1rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      background: #fafafa;
    }

    .detail-item mat-icon {
      color: #666;
      margin-top: 0.2rem;
    }

    .detail-item strong {
      display: block;
      margin-bottom: 0.25rem;
      color: #333;
    }

    .detail-item p {
      margin: 0;
      color: #666;
    }

    .help-card {
      background: rgba(255, 255, 255, 0.9);
    }

    .help-card h3 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .contact-info {
      display: flex;
      gap: 2rem;
      margin-top: 1rem;
    }

    .contact-info p {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin: 0;
    }

    .history-card {
      margin-bottom: 2rem;
      background: rgba(255, 255, 255, 0.95);
    }

    .timeline {
      padding: 1rem;
    }

    .timeline-item {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      border-bottom: 1px solid #e0e0e0;
    }

    .timeline-item.current {
      background: #f7f7f7;
    }

    .timeline-marker {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #2196F3;
      color: white;
    }

    .timeline-content {
      flex: 1;
    }

    .timeline-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .timeline-date {
      font-size: 0.8rem;
      color: #666;
    }

    .timeline-description {
      margin-bottom: 1rem;
    }

    .timeline-meta {
      display: flex;
      gap: 1rem;
      font-size: 0.8rem;
      color: #666;
    }

    @media (max-width: 768px) {
      .search-form {
        flex-direction: column;
        align-items: stretch;
      }

      .search-button {
        height: 48px;
      }

      .details-grid {
        grid-template-columns: 1fr;
      }

      .contact-info {
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  `]
})
export class TrackingComponent {
  trackingNumber: string = '';
  trackingResult: PublicTrackingResponse | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private trackingService: TrackingService) {}

  trackShipment(): void {
    if (!this.trackingNumber.trim()) {
      return;
    }

    this.loading = true;
    this.error = null;
    this.trackingResult = null;

    this.trackingService.trackShipment(this.trackingNumber.trim()).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.trackingResult = response.data || null;
        } else {
          this.error = response.error || 'Erreur inconnue';
        }
      },
      error: (error) => {
        this.loading = false;
        this.error = 'Impossible de contacter le service de suivi. Veuillez réessayer.';
        console.error('Tracking error:', error);
      }
    });
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'CREATED': 'Créé',
      'CONFIRMED': 'Confirmé',
      'IN_TRANSIT': 'En transit',
      'DELIVERED': 'Livré',
      'CANCELLED': 'Annulé'
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: string): string {
    const colorMap: { [key: string]: string } = {
      'CREATED': '#2196F3',
      'CONFIRMED': '#FF9800',
      'IN_TRANSIT': '#9C27B0',
      'DELIVERED': '#4CAF50',
      'CANCELLED': '#F44336'
    };
    return colorMap[status] || '#757575';
  }

  getProgressPercentage(status: string): number {
    const progressMap: { [key: string]: number } = {
      'CREATED': 10,
      'CONFIRMED': 25,
      'IN_TRANSIT': 75,
      'DELIVERED': 100,
      'CANCELLED': 0
    };
    return progressMap[status] || 0;
  }

  getProgressColor(status: string): 'primary' | 'accent' | 'warn' {
    if (status === 'DELIVERED') return 'primary';
    if (status === 'CANCELLED') return 'warn';
    return 'accent';
  }

  getStatusIcon(status: string): string {
    const iconMap: { [key: string]: string } = {
      'CREATED': 'create',
      'CONFIRMED': 'check_circle',
      'IN_TRANSIT': 'local_shipping',
      'DELIVERED': 'done',
      'CANCELLED': 'cancel'
    };
    return iconMap[status] || 'help';
  }

  getChangedByLabel(changedBy: string): string {
    const changedByMap: { [key: string]: string } = {
      'SYSTEM': 'Système',
      'USER': 'Utilisateur',
      'ADMIN': 'Administrateur'
    };
    return changedByMap[changedBy] || 'Inconnu';
  }

  formatDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return 'Date invalide';
      }
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'Date invalide';
    }
  }
}
