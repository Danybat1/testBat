import { City } from './city.model';

export interface Tariff {
  id?: number;
  originCity: City;
  destinationCity: City;
  kgRate: number;
  volumeCoeffV1?: number;
  volumeCoeffV2?: number;
  volumeCoeffV3?: number;
  isActive?: boolean;
  effectiveFrom?: string;
  effectiveUntil?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TariffRequest {
  originCityId: number;
  destinationCityId: number;
  kgRate: number;
  volumeCoeffV1?: number;
  volumeCoeffV2?: number;
  volumeCoeffV3?: number;
  effectiveFrom?: string;
  effectiveUntil?: string;
}

export interface TariffResponse extends Tariff {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export interface TariffSearchResult {
  id: number;
  originCity: City;
  destinationCity: City;
  kgRate: number;
  displayText: string; // For display: "CDG → JFK (€2.50/kg)"
}

export interface CostCalculation {
  tariffId: number;
  originCity: City;
  destinationCity: City;
  weight: number;
  baseCost: number;
  volumeCost: number;
  totalCost: number;
  currency: string;
}
