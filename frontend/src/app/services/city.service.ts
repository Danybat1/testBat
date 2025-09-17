import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { City, CityRequest, CityResponse, CitySearchResult } from '../models/city.model';
import { ApiResponse, PagedResponse } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CityService {
  private readonly apiUrl = `${environment.apiUrl}/api/cities`;

  // Villes de la République Démocratique du Congo - Liste étendue
  private mockCities: CityResponse[] = [
    // Principales villes
    {
      id: 1, name: 'Kinshasa', country: 'République Démocratique du Congo', iataCode: 'FIH', icaoCode: 'FZAA',
      latitude: -4.4419, longitude: 15.2663, timezone: 'Africa/Kinshasa', region: 'Kinshasa',
      description: 'Capitale de la RD Congo', notes: 'Principal hub aérien du pays', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 2, name: 'Lubumbashi', country: 'République Démocratique du Congo', iataCode: 'FBM', icaoCode: 'FZQA',
      latitude: -11.5912, longitude: 27.5308, timezone: 'Africa/Lubumbashi', region: 'Haut-Katanga',
      description: 'Capitale minière du Katanga', notes: 'Centre économique du sud-est', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 3, name: 'Kisangani', country: 'République Démocratique du Congo', iataCode: 'FKI', icaoCode: 'FZIC',
      latitude: 0.5167, longitude: 25.2000, timezone: 'Africa/Kinshasa', region: 'Tshopo',
      description: 'Ville du centre-nord', notes: 'Port fluvial important', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 4, name: 'Mbuji-Mayi', country: 'République Démocratique du Congo', iataCode: 'MJM', icaoCode: 'FZWA',
      latitude: -6.1500, longitude: 23.5900, timezone: 'Africa/Kinshasa', region: 'Kasaï-Oriental',
      description: 'Capitale du diamant', notes: 'Centre de l\'industrie diamantaire', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 5, name: 'Goma', country: 'République Démocratique du Congo', iataCode: 'GOM', icaoCode: 'FZNA',
      latitude: -1.6792, longitude: 29.2383, timezone: 'Africa/Kinshasa', region: 'Nord-Kivu',
      description: 'Ville frontalière avec le Rwanda', notes: 'Centre commercial de l\'est', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 6, name: 'Kananga', country: 'République Démocratique du Congo', iataCode: 'KGA', icaoCode: 'FZUA',
      latitude: -5.8950, longitude: 22.4692, timezone: 'Africa/Kinshasa', region: 'Kasaï-Central',
      description: 'Capitale du Kasaï-Central', notes: 'Centre administratif régional', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 7, name: 'Bukavu', country: 'République Démocratique du Congo', iataCode: 'BKY', icaoCode: 'FZMA',
      latitude: -2.3081, longitude: 28.8089, timezone: 'Africa/Kinshasa', region: 'Sud-Kivu',
      description: 'Ville du lac Kivu', notes: 'Port sur le lac Kivu', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 8, name: 'Kolwezi', country: 'République Démocratique du Congo', iataCode: 'KWZ', icaoCode: 'FZQM',
      latitude: -10.7089, longitude: 25.4672, timezone: 'Africa/Lubumbashi', region: 'Lualaba',
      description: 'Centre minier du cuivre', notes: 'Important centre minier', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 9, name: 'Mbandaka', country: 'République Démocratique du Congo', iataCode: 'MDK', icaoCode: 'FZEA',
      latitude: 0.0486, longitude: 18.2606, timezone: 'Africa/Kinshasa', region: 'Équateur',
      description: 'Port sur le fleuve Congo', notes: 'Centre de transport fluvial', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 10, name: 'Matadi', country: 'République Démocratique du Congo', iataCode: 'MAT', icaoCode: 'FZAM',
      latitude: -5.8386, longitude: 13.4400, timezone: 'Africa/Kinshasa', region: 'Kongo-Central',
      description: 'Principal port maritime', notes: 'Port d\'entrée maritime principal', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    
    // Villes importantes
    {
      id: 11, name: 'Kindu', country: 'République Démocratique du Congo', iataCode: 'KND', icaoCode: 'FZOA',
      latitude: -2.9447, longitude: 25.9231, timezone: 'Africa/Kinshasa', region: 'Maniema',
      description: 'Chef-lieu du Maniema', notes: 'Centre de transport fluvial', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 12, name: 'Gemena', country: 'République Démocratique du Congo', iataCode: 'GMA', icaoCode: 'FZFK',
      latitude: 3.2533, longitude: 19.7717, timezone: 'Africa/Kinshasa', region: 'Sud-Ubangi',
      description: 'Chef-lieu du Sud-Ubangi', notes: 'Centre agricole', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 13, name: 'Isiro', country: 'République Démocratique du Congo', iataCode: 'IRP', icaoCode: 'FZJH',
      latitude: 2.7736, longitude: 27.6158, timezone: 'Africa/Kinshasa', region: 'Haut-Uélé',
      description: 'Chef-lieu du Haut-Uélé', notes: 'Centre commercial du nord-est', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 14, name: 'Bunia', country: 'République Démocratique du Congo', iataCode: 'BUX', icaoCode: 'FZKA',
      latitude: 1.5597, longitude: 30.2522, timezone: 'Africa/Kinshasa', region: 'Ituri',
      description: 'Chef-lieu de l\'Ituri', notes: 'Centre minier aurifère', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 15, name: 'Bandundu', country: 'République Démocratique du Congo', iataCode: 'BAN', icaoCode: 'FZBO',
      latitude: -3.3167, longitude: 17.3833, timezone: 'Africa/Kinshasa', region: 'Kwilu',
      description: 'Chef-lieu du Kwilu', notes: 'Centre agricole et commercial', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    
    // Villes supplémentaires
    {
      id: 16, name: 'Kikwit', country: 'République Démocratique du Congo', iataCode: 'KKW', icaoCode: 'FZCA',
      latitude: -5.0414, longitude: 18.8161, timezone: 'Africa/Kinshasa', region: 'Kwilu',
      description: 'Grande ville du Kwilu', notes: 'Centre commercial important', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 17, name: 'Likasi', country: 'République Démocratique du Congo', iataCode: 'LIK', icaoCode: 'FZQC',
      latitude: -10.9847, longitude: 26.7384, timezone: 'Africa/Lubumbashi', region: 'Haut-Katanga',
      description: 'Ville minière du Katanga', notes: 'Production de cuivre et cobalt', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 18, name: 'Tshikapa', country: 'République Démocratique du Congo', iataCode: 'TSH', icaoCode: 'FZUK',
      latitude: -6.4167, longitude: 20.8000, timezone: 'Africa/Kinshasa', region: 'Kasaï',
      description: 'Centre diamantaire', notes: 'Exploitation diamantaire', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 19, name: 'Beni', country: 'République Démocratique du Congo', iataCode: 'BEN', icaoCode: 'FZNP',
      latitude: 0.4914, longitude: 29.4731, timezone: 'Africa/Kinshasa', region: 'Nord-Kivu',
      description: 'Ville frontalière', notes: 'Commerce transfrontalier', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 20, name: 'Uvira', country: 'République Démocratique du Congo', iataCode: 'UVR', icaoCode: 'FZMA',
      latitude: -3.3969, longitude: 29.1378, timezone: 'Africa/Kinshasa', region: 'Sud-Kivu',
      description: 'Port sur le lac Tanganyika', notes: 'Commerce lacustre', isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Get all cities with pagination
   */
  getCities(page: number = 0, size: number = 20, search?: string): Observable<PagedResponse<CityResponse>> {
    let filteredCities = [...this.mockCities];
    
    if (search) {
      filteredCities = filteredCities.filter(city =>
        city.name?.toLowerCase().includes(search.toLowerCase()) ||
        city.iataCode?.toLowerCase().includes(search.toLowerCase())
      );
    }

    const start = page * size;
    const end = start + size;
    const content = filteredCities.slice(start, end);

    return of({
      content,
      totalElements: filteredCities.length,
      totalPages: Math.ceil(filteredCities.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= filteredCities.length
    }).pipe(delay(500));
  }

  /**
   * Get city by ID
   */
  getCityById(id: number): Observable<CityResponse> {
    const city = this.mockCities.find(c => c.id === id);
    if (city) {
      return of(city).pipe(delay(300));
    }
    throw new Error('City not found');
  }

  /**
   * Create new city
   */
  createCity(city: CityRequest): Observable<CityResponse> {
    const newCity: CityResponse = {
      ...city,
      id: Math.max(...this.mockCities.map(c => c.id)) + 1,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.mockCities.push(newCity);
    return of(newCity).pipe(delay(500));
  }

  /**
   * Update existing city
   */
  updateCity(id: number, city: CityRequest): Observable<CityResponse> {
    const index = this.mockCities.findIndex(c => c.id === id);
    if (index !== -1) {
      this.mockCities[index] = {
        ...this.mockCities[index],
        ...city,
        updatedAt: new Date().toISOString()
      };
      return of(this.mockCities[index]).pipe(delay(500));
    }
    throw new Error('City not found');
  }

  /**
   * Delete city
   */
  deleteCity(id: number): Observable<void> {
    const index = this.mockCities.findIndex(c => c.id === id);
    if (index !== -1) {
      this.mockCities.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }
    throw new Error('City not found');
  }

  /**
   * Search cities for autocomplete (by name or IATA code)
   */
  searchCities(query: string): Observable<CitySearchResult[]> {
    const results = this.mockCities
      .filter(city =>
        city.name?.toLowerCase().includes(query.toLowerCase()) ||
        city.iataCode?.toLowerCase().includes(query.toLowerCase())
      )
      .map(city => ({
        id: city.id!,
        name: city.name!,
        iataCode: city.iataCode!,
        country: city.country,
        displayText: `${city.name} (${city.iataCode})${city.country ? ' - ' + city.country : ''}`
      }));
    
    return of(results).pipe(delay(300));
  }

  /**
   * Get all cities (simple list without pagination)
   */
  getAllCities(): Observable<City[]> {
    return of([...this.mockCities]).pipe(delay(500));
  }

  /**
   * Get active cities only
   */
  getActiveCities(): Observable<CityResponse[]> {
    const activeCities = this.mockCities.filter(city => city.isActive);
    return of(activeCities).pipe(delay(500));
  }

  /**
   * Get cities by region
   */
  getCitiesByRegion(region: string): Observable<CityResponse[]> {
    const citiesByRegion = this.mockCities.filter(city => 
      city.region?.toLowerCase().includes(region.toLowerCase())
    );
    return of(citiesByRegion).pipe(delay(300));
  }

  /**
   * Get major cities (top 10)
   */
  getMajorCities(): Observable<CityResponse[]> {
    const majorCities = this.mockCities.slice(0, 10);
    return of(majorCities).pipe(delay(300));
  }

  /**
   * Check if IATA code is available
   */
  checkIataCodeAvailability(iataCode: string, excludeId?: number): Observable<boolean> {
    const exists = this.mockCities.some(city => 
      city.iataCode?.toUpperCase() === iataCode.toUpperCase() && 
      city.id !== excludeId
    );
    return of(!exists).pipe(delay(200));
  }
}
