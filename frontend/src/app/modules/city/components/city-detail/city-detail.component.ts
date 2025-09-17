import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CityService } from '../../../../services/city.service';
import { City } from '../../../../models/city.model';

@Component({
  selector: 'app-city-detail',
  templateUrl: './city-detail.component.html',
  styleUrls: ['./city-detail.component.scss']
})
export class CityDetailComponent implements OnInit {
  city: City | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cityService: CityService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadCity(+id);
    } else {
      this.router.navigate(['/cities']);
    }
  }

  private loadCity(id: number): void {
    this.loading = true;
    this.cityService.getCityById(id).subscribe({
      next: (city) => {
        this.city = city;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la ville:', error);
        alert('Erreur lors du chargement de la ville');
        this.router.navigate(['/cities']);
        this.loading = false;
      }
    });
  }

  editCity(): void {
    if (this.city) {
      this.router.navigate(['/cities/edit', this.city.id]);
    }
  }

  deleteCity(): void {
    if (this.city && confirm(`Êtes-vous sûr de vouloir supprimer la ville ${this.city.name}?`)) {
      this.cityService.deleteCity(this.city.id!).subscribe({
        next: () => {
          alert('Ville supprimée avec succès');
          this.router.navigate(['/cities']);
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/cities']);
  }

  getStatusClass(isActive: boolean | undefined): string {
    return isActive ? 'bg-success text-white' : 'bg-danger text-white';
  }

  getStatusLabel(isActive: boolean | undefined): string {
    return isActive ? 'Active' : 'Inactive';
  }

  getCountryFlag(countryCode: string | undefined): string {
    if (!countryCode) return '';
    // Convert country code to flag emoji
    const codePoints = countryCode
      .toUpperCase()
      .split('')
      .map(char => 127397 + char.charCodeAt(0));
    return String.fromCodePoint(...codePoints);
  }
} 