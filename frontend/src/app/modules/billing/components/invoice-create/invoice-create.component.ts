import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { InvoiceService } from '../../../../core/services/invoice.service';
import { ClientService } from '../../../../services/client.service';
import { LTAService } from '../../../../core/services/lta.service';
import { Client } from '../../../../models/client.model';
import { LTA } from '../../../../models/lta.model';
import { 
  InvoiceType, 
  PaymentTerms, 
  InvoiceRequest,
  InvoiceItem,
  InvoiceAddress,
  Currency
} from '../../../../models/invoice.model';

@Component({
  selector: 'app-invoice-create',
  templateUrl: './invoice-create.component.html',
  styleUrls: ['./invoice-create.component.scss']
})
export class InvoiceCreateComponent implements OnInit {
  invoiceForm!: FormGroup;
  loading = false;
  clients: Client[] = [];
  availableLTAs: LTA[] = [];
  selectedLTAs: LTA[] = [];
  
  // Preview properties
  previewLoading = false;
  previewError: string | null = null;
  previewHtml: SafeHtml | null = null;
  createdInvoiceId: number | null = null;
  
  // Enums for templates
  invoiceTypes = Object.values(InvoiceType);
  paymentTerms = Object.values(PaymentTerms);
  currencies = Object.values(Currency);
  
  // Form calculations
  subtotal = 0;
  totalTax = 0;
  totalAmount = 0;
  
  // Default company info (should come from settings)
  defaultCompanyInfo: InvoiceAddress = {
    name: 'FreightOps Transport',
    address: '123 Avenue de la Liberté',
    city: 'Kinshasa',
    postalCode: '10000',
    country: 'République Démocratique du Congo',
    taxId: 'CD123456789',
    email: 'contact@freightops.com',
    phone: '+243 81 123 45 67'
  };

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private invoiceService: InvoiceService,
    private clientService: ClientService,
    private ltaService: LTAService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadClients();
    this.loadAvailableLTAs();
    this.setupFormSubscriptions();
    
    // Check if creating from LTA
    const ltaId = this.route.snapshot.queryParams['ltaId'];
    if (ltaId) {
      this.loadLTAForInvoice(+ltaId);
    }
  }

  private initializeForm(): void {
    const today = new Date().toISOString().split('T')[0];
    
    this.invoiceForm = this.fb.group({
      // Basic info
      clientId: ['', Validators.required],
      invoiceDate: [today, Validators.required],
      dueDate: ['', Validators.required],
      type: [InvoiceType.TRANSPORT, Validators.required],
      paymentTerms: [PaymentTerms.NET_30, Validators.required],
      currency: [Currency.CDF, Validators.required],
      
      // Billing address
      billingAddress: this.fb.group({
        name: ['', Validators.required],
        address: ['', Validators.required],
        city: ['', Validators.required],
        postalCode: [''],
        country: ['République Démocratique du Congo', Validators.required],
        taxId: [''],
        email: ['', [this.optionalEmailValidator]],
        phone: ['']
      }),
      
      // Items with custom validator
      items: this.fb.array([], [this.itemsValidator]),
      
      // Notes
      notes: [''],
      
      // LTA selection
      selectedLTAIds: [[]]
    });
  }

  // Custom validator for optional email
  private optionalEmailValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value || control.value.trim() === '') {
      return null; // Empty is valid
    }
    return Validators.email(control);
  }

  // Custom validator to ensure at least one item exists
  private itemsValidator(control: AbstractControl): ValidationErrors | null {
    const items = control as FormArray;
    if (items.length === 0) {
      return { noItems: true };
    }
    return null;
  }

  private setupFormSubscriptions(): void {
    // Auto-calculate due date when payment terms or invoice date changes
    this.invoiceForm.get('paymentTerms')?.valueChanges.subscribe(() => {
      this.updateDueDate();
    });
    
    this.invoiceForm.get('invoiceDate')?.valueChanges.subscribe(() => {
      this.updateDueDate();
    });
    
    // Auto-fill billing address when client changes
    this.invoiceForm.get('clientId')?.valueChanges.subscribe((clientId: number) => {
      if (clientId) {
        this.fillBillingAddressFromClient(clientId);
        this.loadClientLTAs(clientId);
      }
    });
    
    // Recalculate totals when items change
    this.items.valueChanges.subscribe(() => {
      this.calculateTotals();
    });
  }

  get items(): FormArray {
    return this.invoiceForm.get('items') as FormArray;
  }

  private loadClients(): void {
    this.clientService.getClients().subscribe({
      next: (response: any) => {
        this.clients = response.content || response;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des clients:', error);
      }
    });
  }

  private loadAvailableLTAs(): void {
    this.ltaService.getLTAs({ status: 'CONFIRMED' }).subscribe({
      next: (response: any) => {
        this.availableLTAs = response.content || response;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des LTA:', error);
      }
    });
  }

  private loadClientLTAs(clientId: number): void {
    this.ltaService.getLTAs({ clientId, status: 'CONFIRMED' }).subscribe({
      next: (response: any) => {
        this.availableLTAs = response.content || response;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des LTA client:', error);
      }
    });
  }

  private loadLTAForInvoice(ltaId: number): void {
    this.ltaService.getLTAById(ltaId).subscribe({
      next: (lta: LTA) => {
        // Pre-fill form with LTA data
        this.invoiceForm.patchValue({
          clientId: lta.client?.id,
          type: InvoiceType.TRANSPORT,
          selectedLTAIds: [ltaId]
        });
        
        // Add LTA as invoice item
        this.addLTAItem(lta);
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement de la LTA:', error);
      }
    });
  }

  private fillBillingAddressFromClient(clientId: number): void {
    const client = this.clients.find(c => c.id === clientId);
    if (client) {
      this.invoiceForm.get('billingAddress')?.patchValue({
        name: client.name,
        address: client.address,
        city: client.city?.name || '',
        postalCode: client.postalCode || '',
        country: client.country || 'République Démocratique du Congo',
        taxId: client.taxId || '',
        email: client.email,
        phone: client.phone || client.contactNumber
      });
    }
  }

  private updateDueDate(): void {
    const invoiceDate = this.invoiceForm.get('invoiceDate')?.value;
    const paymentTerms = this.invoiceForm.get('paymentTerms')?.value;
    
    if (invoiceDate && paymentTerms) {
      const dueDate = this.invoiceService.calculateDueDate(invoiceDate, paymentTerms);
      this.invoiceForm.get('dueDate')?.setValue(dueDate);
    }
  }

  addItem(): void {
    const itemGroup = this.fb.group({
      description: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      taxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
      ltaId: [''],
      ltaNumber: ['']
    });
    
    this.items.push(itemGroup);
  }

  addLTAItem(lta: LTA): void {
    const itemGroup = this.fb.group({
      description: [`Transport - LTA ${lta.ltaNumber || lta.id}`, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [lta.totalCost || lta.calculatedCost || 0, [Validators.required, Validators.min(0)]],
      taxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
      ltaId: [lta.id],
      ltaNumber: [lta.ltaNumber || '']
    });
    
    this.items.push(itemGroup);
    this.calculateTotals();
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.calculateTotals();
  }

  onLTACheckboxChange(event: any, ltaId: number): void {
    const selectedLTAIds = this.invoiceForm.get('selectedLTAIds')?.value || [];
    
    if (event.target.checked) {
      // Add LTA to selection
      if (!selectedLTAIds.includes(ltaId)) {
        selectedLTAIds.push(ltaId);
        const lta = this.availableLTAs.find(l => l.id === ltaId);
        if (lta) {
          this.addLTAItem(lta);
        }
      }
    } else {
      // Remove LTA from selection
      const index = selectedLTAIds.indexOf(ltaId);
      if (index > -1) {
        selectedLTAIds.splice(index, 1);
        // Remove corresponding item from form
        for (let i = this.items.length - 1; i >= 0; i--) {
          if (this.items.at(i).get('ltaId')?.value === ltaId) {
            this.items.removeAt(i);
            break;
          }
        }
        this.calculateTotals();
      }
    }
    
    this.invoiceForm.get('selectedLTAIds')?.setValue(selectedLTAIds);
  }

  calculateTotals(): void {
    const items = this.items.value;
    const totals = this.invoiceService.calculateInvoiceTotals(items);
    
    this.subtotal = totals.subtotal;
    this.totalTax = totals.totalTax;
    this.totalAmount = totals.totalAmount;
  }

  onSubmit(): void {
    if (this.invoiceForm.valid) {
      this.loading = true;
      
      const formValue = this.invoiceForm.value;
      
      // Prepare invoice items with calculated totals
      const items: Omit<InvoiceItem, 'id'>[] = formValue.items.map((item: any) => ({
        description: item.description,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        totalPrice: item.quantity * item.unitPrice,
        taxRate: item.taxRate,
        taxAmount: (item.quantity * item.unitPrice) * (item.taxRate / 100),
        ltaId: item.ltaId || undefined,
        ltaNumber: item.ltaNumber || undefined
      }));
      
      const invoiceRequest: InvoiceRequest = {
        clientId: formValue.clientId,
        invoiceDate: formValue.invoiceDate,
        dueDate: formValue.dueDate,
        type: formValue.type,
        paymentTerms: formValue.paymentTerms.toString(), 
        billingAddress: formValue.billingAddress,
        items: items,
        notes: formValue.notes,
        currency: formValue.currency.toString(), 
        ltaIds: formValue.selectedLTAIds.length > 0 ? formValue.selectedLTAIds : undefined
      };
      
      this.invoiceService.createInvoice(invoiceRequest).subscribe({
        next: (invoice: any) => {
          this.loading = false;
          this.createdInvoiceId = invoice.id;
          
          // Show success message with print options
          const result = confirm(
            `Facture ${invoice.invoiceNumber || invoice.id} créée avec succès!\n\n` +
            `Voulez-vous l'imprimer maintenant?\n\n` +
            `OK = Imprimer\nAnnuler = Continuer sans imprimer`
          );
          
          if (result) {
            this.printInvoice(invoice.id);
          }
          
          this.router.navigate(['/billing/invoice', invoice.id]);
        },
        error: (error: any) => {
          console.error('Erreur lors de la création de la facture:', error);
          this.loading = false;
        }
      });
    } else {
      this.markFormGroupTouched();
      this.showValidationErrors();
    }
  }

  previewInvoice(): void {
    if (!this.invoiceForm.valid) {
      this.invoiceForm.markAllAsTouched();
      return;
    }

    this.previewLoading = true;
    this.previewError = null;
    this.previewHtml = null;

    const formValue = this.invoiceForm.value;
    
    // Prepare invoice items with calculated totals
    const items: Omit<InvoiceItem, 'id'>[] = formValue.items.map((item: any) => ({
      description: item.description,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      totalPrice: item.quantity * item.unitPrice,
      taxRate: item.taxRate,
      taxAmount: (item.quantity * item.unitPrice) * (item.taxRate / 100),
      ltaId: item.ltaId || undefined,
      ltaNumber: item.ltaNumber || undefined
    }));
    
    const invoiceRequest: InvoiceRequest = {
      clientId: formValue.clientId,
      invoiceDate: formValue.invoiceDate,
      dueDate: formValue.dueDate,
      type: formValue.type,
      paymentTerms: formValue.paymentTerms.toString(), 
      billingAddress: formValue.billingAddress,
      items: items,
      notes: formValue.notes,
      currency: formValue.currency.toString(), 
      ltaIds: formValue.selectedLTAIds.length > 0 ? formValue.selectedLTAIds : undefined
    };

    console.log('Données de la facture pour aperçu:', invoiceRequest);

    // Generate preview without creating invoice in database
    this.invoiceService.generateInvoicePreview(invoiceRequest).subscribe({
      next: (htmlContent: string) => {
        console.log('Aperçu généré avec succès');
        console.log('Longueur du contenu HTML:', htmlContent.length);
        console.log('Début du contenu HTML:', htmlContent.substring(0, 200));
        this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(htmlContent);
        console.log('previewHtml assigné:', this.previewHtml);
        this.previewLoading = false;
        
        // Show modal using native Bootstrap
        this.showPreviewModal();
      },
      error: (error: any) => {
        console.error('Erreur lors de la génération de l\'aperçu:', error);
        let errorMessage = 'Erreur lors de la génération de l\'aperçu';
        
        if (error.error?.message) {
          errorMessage += ': ' + error.error.message;
        } else if (error.message) {
          errorMessage += ': ' + error.message;
        }
        
        this.previewError = errorMessage;
        this.previewLoading = false;
        this.showPreviewModal();
      }
    });
  }

  /**
   * Show preview modal using native Bootstrap
   */
  private showPreviewModal(): void {
    const modalElement = document.getElementById('previewModal');
    if (modalElement) {
      // Use native Bootstrap modal if available
      if (typeof (window as any).bootstrap !== 'undefined') {
        const modal = new (window as any).bootstrap.Modal(modalElement);
        modal.show();
      } else {
        // Fallback: show modal manually
        modalElement.classList.add('show');
        modalElement.style.display = 'block';
        document.body.classList.add('modal-open');
        
        // Add backdrop
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop fade show';
        document.body.appendChild(backdrop);
      }
    }
  }

  /**
   * Close preview modal
   */
  closePreviewModal(): void {
    const modalElement = document.getElementById('previewModal');
    if (modalElement) {
      if (typeof (window as any).bootstrap !== 'undefined') {
        const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
        if (modal) {
          modal.hide();
        }
      } else {
        // Manual close
        modalElement.classList.remove('show');
        modalElement.style.display = 'none';
        document.body.classList.remove('modal-open');
        
        // Remove backdrop
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) {
          backdrop.remove();
        }
      }
    }
  }

  /**
   * Download PDF from preview
   */
  downloadPdf(): void {
    if (!this.createdInvoiceId) {
      return;
    }

    this.invoiceService.generateInvoicePDF(this.createdInvoiceId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `facture-preview.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Erreur lors du téléchargement:', error);
        alert('Erreur lors de la génération du PDF');
      }
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.invoiceForm.controls).forEach(key => {
      const control = this.invoiceForm.get(key);
      control?.markAsTouched();
      
      if (control instanceof FormGroup) {
        this.markNestedFormGroupTouched(control);
      } else if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          arrayControl.markAsTouched();
          if (arrayControl instanceof FormGroup) {
            this.markNestedFormGroupTouched(arrayControl);
          }
        });
      }
    });
  }

  private markNestedFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      
      if (control instanceof FormGroup) {
        this.markNestedFormGroupTouched(control);
      }
    });
  }

  private showValidationErrors(): void {
    const errors: string[] = [];
    
    if (this.invoiceForm.get('clientId')?.invalid) {
      errors.push('Veuillez sélectionner un client');
    }
    
    if (this.invoiceForm.get('billingAddress.name')?.invalid) {
      errors.push('Le nom/raison sociale est requis');
    }
    
    if (this.invoiceForm.get('billingAddress.address')?.invalid) {
      errors.push('L\'adresse est requise');
    }
    
    if (this.invoiceForm.get('billingAddress.city')?.invalid) {
      errors.push('La ville est requise');
    }
    
    if (this.invoiceForm.get('items')?.hasError('noItems')) {
      errors.push('Veuillez ajouter au moins un article à la facture');
    }
    
    // Check individual items validation
    this.items.controls.forEach((item, index) => {
      if (item.get('description')?.invalid) {
        errors.push(`Article ${index + 1}: Description requise`);
      }
      if (item.get('quantity')?.invalid) {
        errors.push(`Article ${index + 1}: Quantité invalide`);
      }
      if (item.get('unitPrice')?.invalid) {
        errors.push(`Article ${index + 1}: Prix unitaire invalide`);
      }
    });
    
    if (errors.length > 0) {
      console.error('Erreurs de validation:', errors);
      // You could also show these errors in a toast or alert
      alert('Erreurs de validation:\n' + errors.join('\n'));
    }
  }

  onCancel(): void {
    this.router.navigate(['/billing/invoice']);
  }

  /**
   * Print invoice after creation
   */
  printInvoice(invoiceId: number): void {
    this.invoiceService.generateInvoicePDF(invoiceId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const printWindow = window.open(url, '_blank');
        if (printWindow) {
          printWindow.onload = () => {
            printWindow.print();
          };
        }
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Erreur lors de l\'impression:', error);
        alert('Erreur lors de la génération du PDF pour impression');
      }
    });
  }

  /**
   * Download invoice PDF
   */
  downloadInvoicePDF(invoiceId: number, invoiceNumber: string): void {
    this.invoiceService.generateInvoicePDF(invoiceId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `facture-${invoiceNumber}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Erreur lors du téléchargement:', error);
        alert('Erreur lors de la génération du PDF');
      }
    });
  }
}
