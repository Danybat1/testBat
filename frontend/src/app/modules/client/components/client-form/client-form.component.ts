import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClientService } from '../../../../services/client.service';
import { ClientRequest, ClientResponse } from '../../../../models/client.model';

@Component({
  selector: 'app-client-form',
  templateUrl: './client-form.component.html',
  styleUrls: ['./client-form.component.scss']
})
export class ClientFormComponent implements OnInit {
  clientForm: FormGroup;
  isEditMode = false;
  clientId?: number;
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.clientForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.clientId = +params['id'];
        this.loadClient();
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      contactNumber: ['', [Validators.required, Validators.pattern(/^[\d\s\-\+\(\)]+$/)]]
    });
  }

  private loadClient(): void {
    if (!this.clientId) return;
    
    this.loading = true;
    this.clientService.getClientById(this.clientId).subscribe({
      next: (client: ClientResponse) => {
        this.clientForm.patchValue(client);
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement du client:', error);
        this.loading = false;
        this.router.navigate(['/clients']);
      }
    });
  }

  onSubmit(): void {
    if (this.clientForm.valid) {
      this.submitting = true;
      const clientData: ClientRequest = {
        name: this.clientForm.value.name,
        address: this.clientForm.value.address,
        contactNumber: this.clientForm.value.contactNumber
      };

      const operation = this.isEditMode 
        ? this.clientService.updateClient(this.clientId!, clientData)
        : this.clientService.createClient(clientData);

      operation.subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/clients']);
        },
        error: (error: any) => {
          console.error('Erreur lors de la sauvegarde:', error);
          this.submitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/clients']);
  }

  getFieldError(fieldName: string): string {
    const field = this.clientForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `Le champ ${fieldName} est requis`;
      }
      if (field.errors['minlength']) {
        return `Le champ ${fieldName} doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
      }
      if (field.errors['pattern']) {
        return 'Format de numéro de téléphone invalide';
      }
    }
    return '';
  }
}
