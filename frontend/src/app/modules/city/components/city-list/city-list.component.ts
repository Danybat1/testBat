import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CityService } from '../../../../services/city.service';
import { City } from '../../../../models/city.model';

@Component({
  selector: 'app-city-list',
  templateUrl: './city-list.component.html',
  styleUrls: ['./city-list.component.scss']
})
export class CityListComponent implements OnInit {
  cities: City[] = [];
  loading = false;
  searchTerm = '';

  constructor(
    private cityService: CityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCities();
  }

  loadCities(): void {
    this.loading = true;
    this.cityService.getAllCities().subscribe({
      next: (cities: City[]) => {
        this.cities = cities;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des villes:', error);
        this.loading = false;
      }
    });
  }

  navigateToNewCity(): void {
    this.router.navigate(['/cities/new']);
  }

  viewCity(id: number): void {
    this.router.navigate(['/cities/detail', id]);
  }

  editCity(id: number): void {
    this.router.navigate(['/cities/edit', id]);
  }

  deleteCity(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette ville ?')) {
      this.cityService.deleteCity(id).subscribe({
        next: () => {
          this.loadCities();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
        }
      });
    }
  }

  get filteredCities(): City[] {
    if (!this.searchTerm) {
      return this.cities;
    }
    return this.cities.filter(city =>
      (city.name?.toLowerCase().includes(this.searchTerm.toLowerCase()) || false) ||
      (city.iataCode?.toLowerCase().includes(this.searchTerm.toLowerCase()) || false)
    );
  }
}
