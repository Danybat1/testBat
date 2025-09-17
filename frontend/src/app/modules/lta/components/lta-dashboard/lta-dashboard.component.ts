import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { LTAService } from '../../../../core/services/lta.service';
import { LTAStats, LTAResponse } from '../../../../models/lta.model';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-lta-dashboard',
  templateUrl: './lta-dashboard.component.html',
  styleUrls: ['./lta-dashboard.component.scss']
})
export class LtaDashboardComponent implements OnInit {
  stats: LTAStats | null = null;
  recentLTAs: any[] = [];
  loading = true;
  loadingRecent = true;
  error: string | null = null;        // Message d'erreur global
  recentError: string | null = null;  // Erreur spécifique aux récents
  showDashboardContent = true;        // Contrôle l'affichage du contenu dashboard

  constructor(
    private ltaService: LTAService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentLTAs();
    
    // Écouter les changements de route pour masquer/afficher le contenu dashboard
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event) => {
      const navigationEvent = event as NavigationEnd;
      // Masquer le contenu dashboard seulement sur les routes de création/édition
      // Permettre l'affichage sur dashboard principal, liste et détails
      this.showDashboardContent = !navigationEvent.url.includes('/create') && 
                                  !navigationEvent.url.includes('/edit');
    });
  }

  private loadStats(): void {
    this.error = null;
    this.loading = true;

    this.ltaService.getLTAStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.loading = false;
        this.error = this.handleHttpError(error, 'les statistiques');
      }
    });
  }

  private loadRecentLTAs(): void {
    this.recentError = null;
    this.loadingRecent = true;

    this.ltaService.getRecentLTAs(5).subscribe({
      next: (ltas) => {
        this.recentLTAs = ltas;
        this.loadingRecent = false;
      },
      error: (error: unknown) => {
        this.loadingRecent = false;
        this.recentError = this.handleHttpError(error, 'les LTA récents');
      }
    });
  }

  /**
   * Gère les erreurs HTTP et retourne un message utilisateur
   */
  private handleHttpError(error: unknown, context: string): string {
    let message = '';

    if (error instanceof HttpErrorResponse) {
      // Erreur retournée par le backend (ou parsing)
      if (error.error && typeof error.error === 'object' && 'message' in error.error) {
        // Erreur côté client (ex: réseau)
        message = `Erreur réseau : impossible de charger ${context}. Vérifiez votre connexion.`;
      } else {
        // Erreur côté serveur
        switch (error.status) {
          case 0:
            message = `Impossible de se connecter au serveur. Vérifiez que le backend est lancé.`;
            break;
          case 404:
            message = `Endpoint non trouvé : impossible de charger ${context}.`;
            break;
          case 500:
            message = `Erreur serveur interne lors du chargement de ${context}.`;
            break;
          case 401:
          case 403:
            message = `Accès refusé lors du chargement de ${context}.`;
            break;
          default:
            // Vérifier si la réponse est du HTML (erreur courante quand Angular sert index.html)
            if (typeof error.error === 'string' && error.error.startsWith('<!DOCTYPE')) {
              message = `Erreur : une page HTML a été reçue au lieu des données JSON. 
                        Vérifiez que le backend est lancé et que le proxy est configuré.`;
            } else {
              message = `Erreur inattendue [${error.status}] lors du chargement de ${context}.`;
            }
            break;
        }
      }
    } else {
      // Erreur inconnue
      message = `Une erreur inconnue est survenue lors du chargement de ${context}.`;
    }

    console.error(`Erreur [${context}] :`, error);
    return message;
  }

  onCreateLTA(): void {
    this.router.navigate(['/lta/create']);
  }

  onViewAllLTAs(): void {
    this.router.navigate(['/lta/list']);
  }

  onViewLTA(id: number): void {
    this.router.navigate(['/lta/detail', id]);
  }

  refreshStats(): void {
    this.error = null;
    this.recentError = null;
    this.loadStats();
    this.loadRecentLTAs();
  }
}