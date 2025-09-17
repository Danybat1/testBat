import { City } from './city.model';

export interface Client {
  id?: number;
  name: string;
  address?: string;
  contactNumber?: string;
  email?: string;
  contactPerson?: string;
  city?: City;
  postalCode?: string;
  country?: string;
  taxId?: string;
  phone?: string;
  description?: string;
  notes?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ClientRequest {
  name: string;
  address?: string;
  contactNumber?: string;
  email?: string;
  contactPerson?: string;
  cityId?: number;
  postalCode?: string;
  country?: string;
  taxId?: string;
  phone?: string;
  description?: string;
  notes?: string;
}

export interface ClientResponse extends Client {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export interface ClientSearchResult {
  id: number;
  name: string;
  contactNumber?: string;
  email?: string;
  displayText: string; // For autocomplete display: "Company Name (contact@email.com)"
}

export interface ClientWithStats extends ClientResponse {
  ltaCount: number;
  hasActiveLtas: boolean;
}
