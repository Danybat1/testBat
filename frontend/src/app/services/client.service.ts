import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Client, ClientRequest, ClientResponse, ClientSearchResult, ClientWithStats } from '../models/client.model';
import { ApiResponse, PagedResponse } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private readonly apiUrl = `${environment.apiUrl}/api/clients`;

  // Clients congolais pour le développement
  private mockClients: ClientResponse[] = [
    {
      id: 1,
      name: 'SONAS - Société Nationale d\'Assurance',
      address: 'Avenue Kasa-Vubu, Kinshasa, RD Congo',
      contactNumber: '+243 81 234 5678',
      email: 'contact@sonas.cd',
      contactPerson: 'Jean-Baptiste Mukendi',
      city: {
        id: 1,
        name: 'Kinshasa',
        country: 'République Démocratique du Congo',
        iataCode: 'FIH',
        isActive: true
      },
      postalCode: '12345',
      description: 'Société nationale d\'assurance de la RD Congo',
      notes: 'Client institutionnel majeur',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 2,
      name: 'Banque Centrale du Congo',
      address: 'Boulevard du 30 Juin, Kinshasa, RD Congo',
      contactNumber: '+243 81 345 6789',
      email: 'info@bcc.cd',
      contactPerson: 'Marie Tshisekedi',
      city: {
        id: 1,
        name: 'Kinshasa',
        country: 'République Démocratique du Congo',
        iataCode: 'FIH',
        isActive: true
      },
      postalCode: '12346',
      description: 'Banque centrale de la République Démocratique du Congo',
      notes: 'Institution financière gouvernementale',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 3,
      name: 'Tenke Fungurume Mining',
      address: 'Avenue Mobutu, Lubumbashi, RD Congo',
      contactNumber: '+243 82 234 5678',
      email: 'info@tfm.cd',
      contactPerson: 'Patrick Kalala',
      city: {
        id: 2,
        name: 'Lubumbashi',
        country: 'République Démocratique du Congo',
        iataCode: 'FBM',
        isActive: true
      },
      postalCode: '54321',
      description: 'Société minière spécialisée dans l\'extraction du cuivre',
      notes: 'Client industriel majeur du Katanga',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 4,
      name: 'Gécamines SARL',
      address: 'Avenue de la Libération, Kinshasa, RD Congo',
      contactNumber: '+243 81 456 7890',
      email: 'gecamines@gecamines.cd',
      contactPerson: 'Joseph Kabila',
      city: {
        id: 1,
        name: 'Kinshasa',
        country: 'République Démocratique du Congo',
        iataCode: 'FIH',
        isActive: true
      },
      postalCode: '12347',
      description: 'Générale des Carrières et des Mines',
      notes: 'Société minière historique de la RD Congo',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 5,
      name: 'Banque Commerciale du Congo - Goma',
      address: 'Avenue de l\'Indépendance, Goma, RD Congo',
      contactNumber: '+243 83 234 5678',
      email: 'goma@bcdc.cd',
      contactPerson: 'Esperance Nzeyimana',
      city: {
        id: 5,
        name: 'Goma',
        country: 'République Démocratique du Congo',
        iataCode: 'GOM',
        isActive: true
      },
      postalCode: '67890',
      description: 'Agence bancaire de Goma',
      notes: 'Services bancaires pour la région du Nord-Kivu',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 6,
      name: 'Diamond Trading Company',
      address: 'Avenue des Diamants, Mbuji-Mayi, RD Congo',
      contactNumber: '+243 85 234 5678',
      email: 'contact@dtc.cd',
      contactPerson: 'Alphonse Tshombe',
      city: {
        id: 4,
        name: 'Mbuji-Mayi',
        country: 'République Démocratique du Congo',
        iataCode: 'MJM',
        isActive: true
      },
      postalCode: '98765',
      description: 'Société de négoce de diamants',
      notes: 'Spécialisée dans l\'exportation de diamants du Kasaï',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 7,
      name: 'Port Autonome de Matadi',
      address: 'Boulevard Maritime, Matadi, RD Congo',
      contactNumber: '+243 88 234 5678',
      email: 'port@matadi.cd',
      contactPerson: 'André Kimbembe',
      city: {
        id: 10,
        name: 'Matadi',
        country: 'République Démocratique du Congo',
        iataCode: 'MAT',
        isActive: true
      },
      postalCode: '11111',
      description: 'Port principal de la RD Congo',
      notes: 'Infrastructure portuaire stratégique',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: 8,
      name: 'Société de Transport Fluvial',
      address: 'Port de Kisangani, RD Congo',
      contactNumber: '+243 84 234 5678',
      email: 'transport@stf.cd',
      contactPerson: 'Celestin Lumumba',
      city: {
        id: 3,
        name: 'Kisangani',
        country: 'République Démocratique du Congo',
        iataCode: 'FKI',
        isActive: true
      },
      postalCode: '33333',
      description: 'Transport fluvial sur le fleuve Congo',
      notes: 'Liaison fluviale principale de l\'est',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Get all clients with pagination
   */
  getClients(page: number = 0, size: number = 20, search?: string): Observable<PagedResponse<ClientResponse>> {
    let filteredClients = [...this.mockClients];
    
    if (search) {
      filteredClients = filteredClients.filter(client =>
        client.name.toLowerCase().includes(search.toLowerCase()) ||
        client.contactNumber?.toLowerCase().includes(search.toLowerCase()) ||
        client.email?.toLowerCase().includes(search.toLowerCase())
      );
    }

    const start = page * size;
    const end = start + size;
    const content = filteredClients.slice(start, end);

    return of({
      content,
      totalElements: filteredClients.length,
      totalPages: Math.ceil(filteredClients.length / size),
      size,
      number: page,
      first: page === 0,
      last: end >= filteredClients.length
    }).pipe(delay(500));
  }

  /**
   * Get client by ID
   */
  getClientById(id: number): Observable<ClientResponse> {
    const client = this.mockClients.find(c => c.id === id);
    if (client) {
      return of(client).pipe(delay(300));
    }
    throw new Error('Client not found');
  }

  /**
   * Create new client
   */
  createClient(client: ClientRequest): Observable<ClientResponse> {
    const newClient: ClientResponse = {
      ...client,
      id: Math.max(...this.mockClients.map(c => c.id)) + 1,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.mockClients.push(newClient);
    return of(newClient).pipe(delay(500));
  }

  /**
   * Update existing client
   */
  updateClient(id: number, client: ClientRequest): Observable<ClientResponse> {
    const index = this.mockClients.findIndex(c => c.id === id);
    if (index !== -1) {
      this.mockClients[index] = {
        ...this.mockClients[index],
        ...client,
        updatedAt: new Date().toISOString()
      };
      return of(this.mockClients[index]).pipe(delay(500));
    }
    throw new Error('Client not found');
  }

  /**
   * Delete client
   */
  deleteClient(id: number): Observable<void> {
    const index = this.mockClients.findIndex(c => c.id === id);
    if (index !== -1) {
      this.mockClients.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }
    throw new Error('Client not found');
  }

  /**
   * Search clients for autocomplete
   */
  searchClients(query: string): Observable<ClientSearchResult[]> {
    const results = this.mockClients
      .filter(client =>
        client.name.toLowerCase().includes(query.toLowerCase()) ||
        client.contactNumber?.toLowerCase().includes(query.toLowerCase()) ||
        client.email?.toLowerCase().includes(query.toLowerCase())
      )
      .map(client => ({
        id: client.id || 0,
        name: client.name,
        contactNumber: client.contactNumber,
        email: client.email,
        displayText: `${client.name}${client.contactNumber ? ' (' + client.contactNumber + ')' : ''}${client.email ? ' - ' + client.email : ''}`
      }));
    
    return of(results).pipe(delay(300));
  }

  /**
   * Get all clients (simple list without pagination)
   */
  getAllClients(): Observable<ClientResponse[]> {
    return of([...this.mockClients]).pipe(delay(500));
  }

  /**
   * Get active clients only
   */
  getActiveClients(): Observable<ClientResponse[]> {
    const activeClients = this.mockClients.filter(client => client.isActive);
    return of(activeClients).pipe(delay(500));
  }

  /**
   * Get clients with LTA statistics
   */
  getClientsWithStats(): Observable<ClientWithStats[]> {
    const clientsWithStats = this.mockClients.map(client => ({
      ...client,
      ltaCount: Math.floor(Math.random() * 10) + 1,
      hasActiveLtas: Math.random() > 0.3
    }));
    return of(clientsWithStats).pipe(delay(500));
  }

  /**
   * Check if client has active LTAs
   */
  hasActiveLtas(clientId: number): Observable<boolean> {
    const client = this.mockClients.find(c => c.id === clientId);
    if (client) {
      return of(Math.random() > 0.3).pipe(delay(200));
    }
    return of(false).pipe(delay(200));
  }
}
