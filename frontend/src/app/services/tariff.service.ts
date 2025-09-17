import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Tariff, TariffRequest, TariffResponse, TariffSearchResult, CostCalculation } from '../models/tariff.model';
import { ApiResponse, PagedResponse } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TariffService {
  private readonly apiUrl = `${environment.apiUrl}/api/tariffs`;

  // Tarifs pour les routes de la RD Congo
  private mockTariffs: TariffResponse[] = [
    // Routes principales depuis Kinshasa
    {
      id: 1, originCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      destinationCity: { id: 2, name: 'Lubumbashi', country: 'RD Congo', iataCode: 'FBM' },
      kgRate: 2.50, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 2, originCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      destinationCity: { id: 3, name: 'Kisangani', country: 'RD Congo', iataCode: 'FKI' },
      kgRate: 2.20, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 3, originCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      destinationCity: { id: 4, name: 'Mbuji-Mayi', country: 'RD Congo', iataCode: 'MJM' },
      kgRate: 2.30, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 4, originCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      destinationCity: { id: 5, name: 'Goma', country: 'RD Congo', iataCode: 'GOM' },
      kgRate: 2.80, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 5, originCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      destinationCity: { id: 10, name: 'Matadi', country: 'RD Congo', iataCode: 'MAT' },
      kgRate: 1.80, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    
    // Routes depuis Lubumbashi
    {
      id: 6, originCity: { id: 2, name: 'Lubumbashi', country: 'RD Congo', iataCode: 'FBM' },
      destinationCity: { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      kgRate: 2.50, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 7, originCity: { id: 2, name: 'Lubumbashi', country: 'RD Congo', iataCode: 'FBM' },
      destinationCity: { id: 8, name: 'Kolwezi', country: 'RD Congo', iataCode: 'KWZ' },
      kgRate: 1.50, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 8, originCity: { id: 2, name: 'Lubumbashi', country: 'RD Congo', iataCode: 'FBM' },
      destinationCity: { id: 17, name: 'Likasi', country: 'RD Congo', iataCode: 'LIK' },
      kgRate: 1.20, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    
    // Routes régionales
    {
      id: 9, originCity: { id: 5, name: 'Goma', country: 'RD Congo', iataCode: 'GOM' },
      destinationCity: { id: 7, name: 'Bukavu', country: 'RD Congo', iataCode: 'BKY' },
      kgRate: 1.60, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    },
    {
      id: 10, originCity: { id: 4, name: 'Mbuji-Mayi', country: 'RD Congo', iataCode: 'MJM' },
      destinationCity: { id: 6, name: 'Kananga', country: 'RD Congo', iataCode: 'KGA' },
      kgRate: 1.70, volumeCoeffV1: 1.0, volumeCoeffV2: 1.2, volumeCoeffV3: 1.5, isActive: true,
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Get all tariffs with pagination
   */
  getTariffs(page: number = 0, size: number = 20, search?: string): Observable<PagedResponse<TariffResponse>> {
    let filteredTariffs = [...this.mockTariffs];
    
    if (search) {
      filteredTariffs = filteredTariffs.filter(tariff =>
        tariff.originCity?.name?.toLowerCase().includes(search.toLowerCase()) ||
        tariff.destinationCity?.name?.toLowerCase().includes(search.toLowerCase())
      );
    }

    const start = page * size;
    const end = start + size;
    const content = filteredTariffs.slice(start, end);

    return of({
      content,
      totalElements: filteredTariffs.length,
      totalPages: Math.ceil(filteredTariffs.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= filteredTariffs.length
    }).pipe(delay(500));
  }

  /**
   * Get tariff by ID
   */
  getTariffById(id: number): Observable<TariffResponse> {
    const tariff = this.mockTariffs.find(t => t.id === id);
    if (tariff) {
      return of(tariff).pipe(delay(300));
    }
    throw new Error('Tariff not found');
  }

  /**
   * Create new tariff
   */
  createTariff(tariff: TariffRequest): Observable<TariffResponse> {
    // Mock cities for the new tariff
    const mockCities = [
      { id: 1, name: 'Kinshasa', country: 'RD Congo', iataCode: 'FIH' },
      { id: 2, name: 'Lubumbashi', country: 'RD Congo', iataCode: 'FBM' },
      { id: 3, name: 'Kisangani', country: 'RD Congo', iataCode: 'FKI' },
      { id: 4, name: 'Mbuji-Mayi', country: 'RD Congo', iataCode: 'MJM' },
      { id: 5, name: 'Goma', country: 'RD Congo', iataCode: 'GOM' }
    ];
    
    const originCity = mockCities.find(c => c.id === tariff.originCityId);
    const destinationCity = mockCities.find(c => c.id === tariff.destinationCityId);
    
    if (!originCity || !destinationCity) {
      throw new Error('Invalid city IDs');
    }
    
    const newTariff: TariffResponse = {
      id: Math.max(...this.mockTariffs.map(t => t.id)) + 1,
      originCity,
      destinationCity,
      kgRate: tariff.kgRate,
      volumeCoeffV1: tariff.volumeCoeffV1,
      volumeCoeffV2: tariff.volumeCoeffV2,
      volumeCoeffV3: tariff.volumeCoeffV3,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.mockTariffs.push(newTariff);
    return of(newTariff).pipe(delay(500));
  }

  /**
   * Update existing tariff
   */
  updateTariff(id: number, tariff: TariffRequest): Observable<TariffResponse> {
    const index = this.mockTariffs.findIndex(t => t.id === id);
    if (index !== -1) {
      this.mockTariffs[index] = {
        ...this.mockTariffs[index],
        ...tariff,
        updatedAt: new Date().toISOString()
      };
      return of(this.mockTariffs[index]).pipe(delay(500));
    }
    throw new Error('Tariff not found');
  }

  /**
   * Delete tariff
   */
  deleteTariff(id: number): Observable<void> {
    const index = this.mockTariffs.findIndex(t => t.id === id);
    if (index !== -1) {
      this.mockTariffs.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }
    throw new Error('Tariff not found');
  }

  /**
   * Find tariff by origin and destination
   */
  findTariffByRoute(originCityId: number, destinationCityId: number): Observable<TariffResponse | null> {
    const tariff = this.mockTariffs.find(t => 
      t.originCity.id === originCityId && t.destinationCity.id === destinationCityId
    );
    return of(tariff || null).pipe(delay(300));
  }

  /**
   * Get active tariffs
   */
  getActiveTariffs(): Observable<TariffResponse[]> {
    const activeTariffs = this.mockTariffs.filter(tariff => tariff.isActive);
    return of(activeTariffs).pipe(delay(500));
  }

  /**
   * Calculate cost for a route
   */
  calculateCost(originCityId: number, destinationCityId: number, weight: number): Observable<number> {
    const tariff = this.mockTariffs.find(t => 
      t.originCity.id === originCityId && t.destinationCity.id === destinationCityId
    );
    
    if (tariff) {
      const baseCost = tariff.kgRate * weight;
      const volumeCost = (tariff.volumeCoeffV1 || 0) * weight * 0.1;
      const totalCost = Math.max(baseCost, volumeCost);
      return of(totalCost).pipe(delay(300));
    }
    
    // Calculate default rate based on distance estimation
    const defaultRate = this.estimateRateByDistance(originCityId, destinationCityId);
    const defaultCost = defaultRate * weight;
    return of(defaultCost).pipe(delay(300));
  }

  /**
   * Estimate rate based on city distance (simplified)
   */
  private estimateRateByDistance(originId: number, destinationId: number): number {
    // Major cities get lower rates due to volume
    const majorCities = [1, 2, 3, 4, 5]; // Kinshasa, Lubumbashi, Kisangani, Mbuji-Mayi, Goma
    
    if (majorCities.includes(originId) && majorCities.includes(destinationId)) {
      return 2.40; // Major route rate
    } else if (majorCities.includes(originId) || majorCities.includes(destinationId)) {
      return 2.80; // Semi-major route rate
    } else {
      return 3.20; // Regional route rate
    }
  }

  /**
   * Get available routes from a city
   */
  getRoutesFromCity(cityId: number): Observable<TariffResponse[]> {
    const routes = this.mockTariffs.filter(tariff => tariff.originCity.id === cityId);
    return of(routes).pipe(delay(300));
  }

  /**
   * Get popular routes (most used)
   */
  getPopularRoutes(): Observable<TariffResponse[]> {
    // Return routes involving major cities
    const popularRoutes = this.mockTariffs.filter(tariff => 
      [1, 2, 3, 4, 5].includes(tariff.originCity.id || 0) && 
      [1, 2, 3, 4, 5].includes(tariff.destinationCity.id || 0)
    );
    return of(popularRoutes).pipe(delay(300));
  }

  /**
   * Get cheapest routes
   */
  getCheapestRoutes(): Observable<TariffSearchResult[]> {
    const cheapestTariffs = this.mockTariffs
      .sort((a, b) => a.kgRate - b.kgRate)
      .slice(0, 5);
    const results = cheapestTariffs.map(tariff => ({
      id: tariff.id || 0,
      originCity: tariff.originCity,
      destinationCity: tariff.destinationCity,
      kgRate: tariff.kgRate,
      displayText: `${tariff.originCity.iataCode} → ${tariff.destinationCity.iataCode} (${tariff.kgRate}$/kg)`
    }));
    return of(results);
  }

  /**
   * Search tariffs by destination city
   */
  getTariffsByDestination(destinationCityId: number): Observable<TariffResponse[]> {
    const tariffs = this.mockTariffs.filter(t => t.destinationCity?.id === destinationCityId);
    return of(tariffs).pipe(delay(500));
  }

  /**
   * Check if route already exists (for validation)
   */
  checkRouteExists(originCityId: number, destinationCityId: number, excludeId?: number): Observable<boolean> {
    const exists = this.mockTariffs.some(t => 
      t.originCity?.id === originCityId && 
      t.destinationCity?.id === destinationCityId &&
      t.id !== excludeId
    );
    return of(exists).pipe(delay(200));
  }
}
