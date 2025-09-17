import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientService } from '../../../../services/client.service';
import { Client } from '../../../../models/client.model';

@Component({
  selector: 'app-client-detail',
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.scss']
})
export class ClientDetailComponent implements OnInit {
  client: Client | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clientService: ClientService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadClient(+id);
    } else {
      this.router.navigate(['/clients']);
    }
  }

  private loadClient(id: number): void {
    this.loading = true;
    this.clientService.getClientById(id).subscribe({
      next: (client) => {
        this.client = client;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du client:', error);
        alert('Erreur lors du chargement du client');
        this.router.navigate(['/clients']);
        this.loading = false;
      }
    });
  }

  editClient(): void {
    if (this.client) {
      this.router.navigate(['/clients/edit', this.client.id]);
    }
  }

  deleteClient(): void {
    if (this.client && confirm(`Êtes-vous sûr de vouloir supprimer le client ${this.client.name}?`)) {
      this.clientService.deleteClient(this.client.id!).subscribe({
        next: () => {
          alert('Client supprimé avec succès');
          this.router.navigate(['/clients']);
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/clients']);
  }

  getStatusClass(isActive: boolean | undefined): string {
    return isActive ? 'bg-success text-white' : 'bg-danger text-white';
  }

  getStatusLabel(isActive: boolean | undefined): string {
    return isActive ? 'Actif' : 'Inactif';
  }
} 