export interface City {
  id?: number;
  name?: string;
  country?: string;
  iataCode?: string;
  icaoCode?: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;
  region?: string;
  description?: string;
  notes?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CityRequest {
  name: string;
  iataCode: string;
  country?: string;
}

export interface CityResponse extends City {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export interface CitySearchResult {
  id: number;
  name: string;
  iataCode: string;
  country?: string;
  displayText: string; // For autocomplete display: "Paris (CDG) - France"
}
