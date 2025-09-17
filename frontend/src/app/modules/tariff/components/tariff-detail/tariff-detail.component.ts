import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TariffService } from '../../../../services/tariff.service';
import { Tariff } from '../../../../models/tariff.model';

@Component({
  selector: 'app-tariff-detail',
  templateUrl: './tariff-detail.component.html',
  styleUrls: ['./tariff-detail.component.scss']
})
export class TariffDetailComponent implements OnInit {
  tariff: Tariff | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tariffService: TariffService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadTariff(+id);
    } else {
      this.router.navigate(['/tariffs']);
    }
  }

  private loadTariff(id: number): void {
    this.loading = true;
    this.tariffService.getTariffById(id).subscribe({
      next: (tariff: Tariff) => {
        this.tariff = tariff;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement du tarif:', error);
        alert('Erreur lors du chargement du tarif');
        this.router.navigate(['/tariffs']);
        this.loading = false;
      }
    });
  }

  editTariff(): void {
    if (this.tariff) {
      this.router.navigate(['/tariffs/edit', this.tariff.id]);
    }
  }

  deleteTariff(): void {
    if (this.tariff && confirm(`Êtes-vous sûr de vouloir supprimer ce tarif ?`)) {
      this.tariffService.deleteTariff(this.tariff.id!).subscribe({
        next: () => {
          alert('Tarif supprimé avec succès');
          this.router.navigate(['/tariffs']);
        },
        error: (error: any) => {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/tariffs']);
  }

  getStatusClass(isActive: boolean): string {
    return isActive ? 'bg-success text-white' : 'bg-danger text-white';
  }

  getStatusLabel(isActive: boolean): string {
    return isActive ? 'Actif' : 'Inactif';
  }

  getEffectiveDateRange(): string {
    if (!this.tariff) return '';
    
    const from = this.tariff.effectiveFrom ? new Date(this.tariff.effectiveFrom).toLocaleDateString('fr-FR') : 'Non défini';
    const until = this.tariff.effectiveUntil ? new Date(this.tariff.effectiveUntil).toLocaleDateString('fr-FR') : 'Non défini';
    
    return `${from} - ${until}`;
  }
} 