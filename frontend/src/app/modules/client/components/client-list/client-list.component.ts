import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ClientService } from '../../../../services/client.service';
import { Client } from '../../../../models/client.model';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.scss']
})
export class ClientListComponent implements OnInit {
  clients: Client[] = [];
  loading = false;
  searchTerm = '';

  constructor(
    private clientService: ClientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.loading = true;
    this.clientService.getAllClients().subscribe({
      next: (clients: Client[]) => {
        this.clients = clients;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des clients:', error);
        this.loading = false;
      }
    });
  }

  navigateToNewClient(): void {
    this.router.navigate(['/clients/new']);
  }

  viewClient(id: number): void {
    this.router.navigate(['/clients/detail', id]);
  }

  editClient(id: number): void {
    this.router.navigate(['/clients/edit', id]);
  }

  deleteClient(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
      this.clientService.deleteClient(id).subscribe({
        next: () => {
          this.loadClients();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
        }
      });
    }
  }

  get filteredClients(): Client[] {
    if (!this.searchTerm) {
      return this.clients;
    }
    return this.clients.filter(client =>
      client.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (client.contactNumber && client.contactNumber.includes(this.searchTerm))
    );
  }
}
