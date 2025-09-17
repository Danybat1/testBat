import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-caisse-operation',
  templateUrl: './caisse-operation.component.html',
  styleUrls: ['./caisse-operation.component.scss']
})
export class CaisseOperationComponent implements OnInit {
  operationForm!: FormGroup;
  selectedFile: File | null = null;
  
  // Nouvelles propriétés pour LTA
  showLTASelection = false;
  unpaidLTAs: any[] = [];
  selectedLTA: any = null;
  ltaRemainingAmount = 0;

  // Options pour les selects
  operationTypes = [
    { value: 'ENCAISSEMENT', label: 'Encaissement' },
    { value: 'ENCAISSEMENT_LTA', label: 'Encaissement LTA' },
    { value: 'DECAISSEMENT', label: 'Décaissement' }
  ];

  currencies = [
    { value: 'USD', label: 'USD' },
    { value: 'EUR', label: 'EUR' },
    { value: 'CDF', label: 'CDF' }
  ];

  paymentMethods = [
    { value: 'ESPECES', label: 'Espèces' },
    { value: 'PORT_DU', label: 'Port dû' },
    { value: 'CHEQUE', label: 'Chèque' },
    { value: 'VIREMENT', label: 'Virement' },
    { value: 'MOBILE_MONEY', label: 'Mobile Money' }
  ];

  constructor(
    private fb: FormBuilder,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadUnpaidLTAs();
  }

  initializeForm(): void {
    this.operationForm = this.fb.group({
      operationDate: ['', Validators.required],
      reference: ['', Validators.required],
      operationType: ['', Validators.required],
      ltaId: [''], // Nouveau champ pour LTA
      amount: ['', [Validators.required, Validators.min(0.01)]],
      currency: ['', Validators.required],
      paymentMethod: ['', Validators.required],
      clientSupplier: ['', Validators.required],
      operationNature: ['', Validators.required],
      description: ['', Validators.required],
      observations: ['']
    });

    // Écouter les changements du type d'opération
    this.operationForm.get('operationType')?.valueChanges.subscribe(value => {
      this.onOperationTypeChange(value);
    });

    // Écouter les changements de LTA sélectionnée
    this.operationForm.get('ltaId')?.valueChanges.subscribe(ltaId => {
      this.onLTASelectionChange(ltaId);
    });
  }

  loadUnpaidLTAs(): void {
    this.http.get<any[]>('http://localhost:8080/api/lta-payments/unpaid-ltas')
      .subscribe({
        next: (ltas) => {
          this.unpaidLTAs = ltas;
          console.log('LTA non soldées chargées:', ltas.length);
        },
        error: (error) => {
          console.error('Erreur lors du chargement des LTA:', error);
        }
      });
  }

  onOperationTypeChange(operationType: string): void {
    this.showLTASelection = operationType === 'ENCAISSEMENT_LTA';
    
    if (this.showLTASelection) {
      this.operationForm.get('ltaId')?.setValidators([Validators.required]);
      // Pré-remplir certains champs pour LTA
      this.operationForm.patchValue({
        operationNature: 'Encaissement LTA',
        paymentMethod: 'ESPECES'
      });
    } else {
      this.operationForm.get('ltaId')?.clearValidators();
      this.selectedLTA = null;
      this.ltaRemainingAmount = 0;
    }
    
    this.operationForm.get('ltaId')?.updateValueAndValidity();
  }

  onLTASelectionChange(ltaId: string): void {
    if (!ltaId) {
      this.selectedLTA = null;
      this.ltaRemainingAmount = 0;
      return;
    }

    // Trouver la LTA sélectionnée
    this.selectedLTA = this.unpaidLTAs.find(lta => lta.id.toString() === ltaId);
    
    if (this.selectedLTA) {
      // Charger le montant restant
      this.http.get<any>(`http://localhost:8080/api/lta-payments/remaining-amount/${ltaId}`)
        .subscribe({
          next: (response) => {
            this.ltaRemainingAmount = response.remainingAmount;
            
            // Pré-remplir les champs
            this.operationForm.patchValue({
              amount: this.ltaRemainingAmount,
              clientSupplier: this.selectedLTA?.client?.name || this.selectedLTA?.shipperName || 'Client LTA',
              description: `Encaissement LTA ${this.selectedLTA?.ltaNumber} - ${this.selectedLTA?.originCity?.name} → ${this.selectedLTA?.destinationCity?.name}`,
              reference: `LTA-${this.selectedLTA?.ltaNumber}`
            });
          },
          error: (error) => {
            console.error('Erreur lors du chargement du montant restant:', error);
          }
        });
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onSubmit(): void {
    if (this.operationForm.valid && this.selectedLTA) {
      // Validation supplémentaire pour les paiements LTA
      const amount = this.operationForm.get('amount')?.value;
      const ltaRemainingAmount = this.ltaRemainingAmount || 0;
      
      // Validation du montant
      if (amount <= 0) {
        alert('Le montant doit être positif');
        return;
      }
      
      if (amount > ltaRemainingAmount) {
        alert(`Le montant (${amount}) dépasse le montant restant dû (${ltaRemainingAmount})`);
        return;
      }
      
      if (amount > this.selectedLTA.calculatedCost) {
        alert(`Le montant (${amount}) dépasse le coût total de la LTA (${this.selectedLTA.calculatedCost})`);
        return;
      }

      const formData = {
        operationDate: this.operationForm.get('operationDate')?.value,
        reference: this.operationForm.get('reference')?.value,
        operationType: this.operationForm.get('operationType')?.value,
        ltaId: this.operationForm.get('ltaId')?.value,
        amount: this.operationForm.get('amount')?.value,
        currency: this.operationForm.get('currency')?.value,
        paymentMethod: this.operationForm.get('paymentMethod')?.value,
        clientSupplier: this.operationForm.get('clientSupplier')?.value,
        operationNature: this.operationForm.get('operationNature')?.value,
        description: this.operationForm.get('description')?.value,
        observations: this.operationForm.get('observations')?.value,
        attachedFile: this.selectedFile ? this.selectedFile.name : null
      };

      // Si c'est un encaissement LTA, enregistrer le paiement
      if (formData.operationType === 'ENCAISSEMENT_LTA' && formData.ltaId) {
        this.recordLTAPayment(formData);
      } else {
        // Traitement normal des autres opérations - enregistrer dans la base de données
        this.recordCashOperation(formData);
      }
    } else {
      this.markFormGroupTouched(this.operationForm);
    }
  }

  recordLTAPayment(formData: any): void {
    const paymentRequest = {
      ltaId: parseInt(formData.ltaId),
      amount: parseFloat(formData.amount),
      paymentMethod: formData.paymentMethod,
      reference: formData.reference,
      notes: formData.observations,
      cashBoxId: 1 // TODO: Récupérer l'ID de la caisse active
    };

    this.http.post<any>('http://localhost:8080/api/lta-payments/record-payment', paymentRequest)
      .subscribe({
        next: (response) => {
          if (response.success) {
            // Imprimer automatiquement le reçu
            this.printCashReceipt({
              ...formData,
              referenceComptable: response.referenceComptable,
              remainingAmount: response.remainingAmount,
              operationId: response.ltaId,
              timestamp: new Date()
            });
            
            alert(`Paiement LTA enregistré avec succès!\nRéférence comptable: ${response.referenceComptable}\nMontant restant: ${response.remainingAmount}`);
            
            // Réinitialiser le formulaire et recharger les LTA
            this.onCancel();
            this.loadUnpaidLTAs();
          } else {
            alert('Erreur: ' + response.error);
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement du paiement:', error);
          alert('Erreur lors de l\'enregistrement du paiement: ' + (error.error?.error || error.message));
        }
      });
  }

  recordCashOperation(formData: any): void {
    console.log('Envoi des données d\'opération:', formData);
    
    this.http.post<any>('http://localhost:8080/api/cash-operations/record-operation', formData)
      .subscribe({
        next: (response) => {
          console.log('Réponse du serveur:', response);
          if (response.success) {
            alert('Opération enregistrée avec succès!');
            // Réinitialiser le formulaire
            this.onCancel();
          } else {
            alert('Erreur: ' + response.error);
          }
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement de l\'opération:', error);
          alert('Erreur lors de l\'enregistrement de l\'opération: ' + (error.error?.error || error.message));
        }
      });
  }

  onCancel(): void {
    this.operationForm.reset();
    this.selectedFile = null;
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  private getFormErrors(): any {
    const errors: any = {};
    Object.keys(this.operationForm.controls).forEach(key => {
      const control = this.operationForm.get(key);
      if (control && !control.valid && control.touched) {
        errors[key] = control.errors;
      }
    });
    return errors;
  }

  // Helper methods pour l'affichage des erreurs
  isFieldInvalid(fieldName: string): boolean {
    const field = this.operationForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.operationForm.get(fieldName);
    if (field && field.errors && field.touched) {
      if (field.errors['required']) {
        return 'Ce champ est obligatoire';
      }
      if (field.errors['min']) {
        return 'Le montant doit être supérieur à 0';
      }
    }
    return '';
  }

  // Méthode helper pour afficher les informations LTA
  getLTADisplayText(lta: any): string {
    return `${lta.ltaNumber} - ${lta.originCity?.name} → ${lta.destinationCity?.name} (${lta.calculatedCost} ${lta.currency || 'USD'})`;
  }

  getPaymentModeBadgeClass(paymentMode: string): string {
    switch (paymentMode) {
      case 'CASH':
        return 'badge-success';
      case 'PORT_DU':
        return 'badge-warning';
      case 'TO_INVOICE':
        return 'badge-info';
      default:
        return 'badge-secondary';
    }
  }

  getPaymentStatusBadgeClass(): string {
    if (!this.selectedLTA || !this.ltaRemainingAmount) return 'badge-secondary';
    
    const totalCost = this.selectedLTA.calculatedCost || 0;
    const remaining = this.ltaRemainingAmount || 0;
    
    if (remaining <= 0) {
      return 'badge-success'; // Entièrement payé
    } else if (remaining < totalCost) {
      return 'badge-warning'; // Partiellement payé
    } else {
      return 'badge-danger'; // Non payé
    }
  }

  getPaymentStatusIcon(): string {
    if (!this.selectedLTA || !this.ltaRemainingAmount) return 'fa-question';
    
    const totalCost = this.selectedLTA.calculatedCost || 0;
    const remaining = this.ltaRemainingAmount || 0;
    
    if (remaining <= 0) {
      return 'fa-check-circle'; // Entièrement payé
    } else if (remaining < totalCost) {
      return 'fa-clock'; // Partiellement payé
    } else {
      return 'fa-exclamation-triangle'; // Non payé
    }
  }

  getPaymentStatusText(): string {
    if (!this.selectedLTA || !this.ltaRemainingAmount) return 'Statut inconnu';
    
    const totalCost = this.selectedLTA.calculatedCost || 0;
    const remaining = this.ltaRemainingAmount || 0;
    
    if (remaining <= 0) {
      return 'Entièrement payé';
    } else if (remaining < totalCost) {
      return 'Partiellement payé';
    } else {
      return 'Non payé';
    }
  }

  markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  printCashReceipt(receiptData: any): void {
    const printWindow = window.open('', '_blank', 'width=800,height=600');
    if (!printWindow) {
      alert('Impossible d\'ouvrir la fenêtre d\'impression. Veuillez autoriser les pop-ups.');
      return;
    }

    const receiptHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Reçu de Caisse - ${receiptData.reference}</title>
        <style>
          body { 
            font-family: Arial, sans-serif; 
            margin: 20px; 
            font-size: 12px;
            line-height: 1.4;
          }
          .header { 
            text-align: center; 
            border-bottom: 2px solid #333; 
            padding-bottom: 10px; 
            margin-bottom: 20px;
          }
          .company-name { 
            font-size: 18px; 
            font-weight: bold; 
            color: #2c5aa0;
          }
          .receipt-title { 
            font-size: 16px; 
            font-weight: bold; 
            margin: 10px 0;
          }
          .receipt-info { 
            display: flex; 
            justify-content: space-between; 
            margin-bottom: 20px;
          }
          .info-section { 
            flex: 1; 
          }
          .info-row { 
            margin: 5px 0; 
          }
          .label { 
            font-weight: bold; 
            display: inline-block; 
            width: 120px;
          }
          .amount-section { 
            background-color: #f5f5f5; 
            padding: 15px; 
            border-radius: 5px; 
            margin: 20px 0;
            text-align: center;
          }
          .amount { 
            font-size: 24px; 
            font-weight: bold; 
            color: #2c5aa0;
          }
          .footer { 
            margin-top: 30px; 
            border-top: 1px solid #ccc; 
            padding-top: 10px;
            text-align: center;
            font-size: 10px;
          }
          .signature-section {
            margin-top: 40px;
            display: flex;
            justify-content: space-between;
          }
          .signature-box {
            width: 200px;
            text-align: center;
          }
          .signature-line {
            border-top: 1px solid #333;
            margin-top: 50px;
            padding-top: 5px;
          }
          @media print {
            body { margin: 0; }
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <div class="company-name">FREIGHTOPS</div>
          <div class="receipt-title">REÇU DE CAISSE</div>
        </div>

        <div class="receipt-info">
          <div class="info-section">
            <div class="info-row">
              <span class="label">Date:</span>
              <span>${new Date(receiptData.operationDate).toLocaleDateString('fr-FR')}</span>
            </div>
            <div class="info-row">
              <span class="label">Référence:</span>
              <span>${receiptData.reference}</span>
            </div>
            <div class="info-row">
              <span class="label">Type:</span>
              <span>${receiptData.operationType}</span>
            </div>
          </div>
          <div class="info-section">
            <div class="info-row">
              <span class="label">Client:</span>
              <span>${receiptData.clientSupplier}</span>
            </div>
            <div class="info-row">
              <span class="label">Méthode:</span>
              <span>${receiptData.paymentMethod}</span>
            </div>
            <div class="info-row">
              <span class="label">Devise:</span>
              <span>${receiptData.currency}</span>
            </div>
          </div>
        </div>

        <div class="amount-section">
          <div>Montant encaissé</div>
          <div class="amount">${receiptData.amount} ${receiptData.currency}</div>
        </div>

        <div class="info-row">
          <span class="label">Description:</span>
          <span>${receiptData.description}</span>
        </div>

        ${receiptData.observations ? `
        <div class="info-row">
          <span class="label">Observations:</span>
          <span>${receiptData.observations}</span>
        </div>
        ` : ''}

        ${receiptData.remainingAmount !== undefined ? `
        <div class="info-row">
          <span class="label">Montant restant:</span>
          <span>${receiptData.remainingAmount} ${receiptData.currency}</span>
        </div>
        ` : ''}

        <div class="signature-section">
          <div class="signature-box">
            <div class="signature-line">Signature du caissier</div>
          </div>
          <div class="signature-box">
            <div class="signature-line">Signature du client</div>
          </div>
        </div>

        <div class="footer">
          <div>Reçu généré le ${new Date().toLocaleString('fr-FR')}</div>
          <div>FreightOps - Système de gestion de trésorerie</div>
        </div>

        <script>
          window.onload = function() {
            window.print();
            window.onafterprint = function() {
              window.close();
            };
          };
        </script>
      </body>
      </html>
    `;

    printWindow.document.write(receiptHtml);
    printWindow.document.close();
  }
}
