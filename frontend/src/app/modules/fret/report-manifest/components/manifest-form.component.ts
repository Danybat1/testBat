import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ManifestService } from '../services/manifest.service';

interface ManifestFormData {
  manifestNumber?: string;
  proformaNumber: string;
  transportMode: string;
  vehicleReference: string;
  driverName: string;
  driverPhone: string;
  scheduledDeparture: Date;
  scheduledArrival: Date;
  shipper: PartyForm;
  consignee: PartyForm;
  client: PartyForm;
  agent: PartyForm;
  goods: GoodsItemForm[];
  deliveryInstructions: string;
  generalRemarks: string;
  attachments: string[];
  status: string;
}

interface PartyForm {
  name: string;
  address: string;
  contact: string;
  phone: string;
  reference?: string;
}

interface GoodsItemForm {
  trackingNumber: string;
  description: string;
  packagingType: string;
  packageCount: number;
  grossWeight: number;
  volume: number;
  volumetricWeight: number;
  declaredValue: number;
  containerNumber?: string;
  remarks?: string;
}

@Component({
  selector: 'app-manifest-form',
  templateUrl: './manifest-form.component.html',
  styleUrls: ['./manifest-form.component.scss']
})
export class ManifestFormComponent implements OnInit {
  manifestForm: FormGroup;
  isLoading = false;
  error: string | null = null;
  success: string | null = null;

  transportModes = [
    { value: 'ROAD', label: 'Transport Routier' },
    { value: 'AIR', label: 'Transport Aérien' },
    { value: 'SEA', label: 'Transport Maritime' },
    { value: 'RAIL', label: 'Transport Ferroviaire' },
    { value: 'MULTIMODAL', label: 'Transport Multimodal' }
  ];

  packagingTypes = [
    { value: 'Palettes', label: 'Palettes' },
    { value: 'Cartons', label: 'Cartons' },
    { value: 'Caisses', label: 'Caisses' },
    { value: 'Conteneurs', label: 'Conteneurs' },
    { value: 'Sacs', label: 'Sacs' },
    { value: 'Fûts', label: 'Fûts' }
  ];

  statusOptions = [
    { value: 'Brouillon', label: 'Brouillon' },
    { value: 'En préparation', label: 'En préparation' },
    { value: 'Prêt pour expédition', label: 'Prêt pour expédition' },
    { value: 'En transit', label: 'En transit' },
    { value: 'En cours de livraison', label: 'En cours de livraison' },
    { value: 'Livré', label: 'Livré' }
  ];

  constructor(
    private fb: FormBuilder,
    private manifestService: ManifestService,
    private router: Router
  ) {
    this.manifestForm = this.createForm();
  }

  ngOnInit(): void {
    // Initialiser avec un article de marchandise par défaut
    this.addGoodsItem();
  }

  createForm(): FormGroup {
    return this.fb.group({
      // Informations générales
      proformaNumber: ['', [Validators.required, Validators.minLength(3)]],
      transportMode: ['ROAD', Validators.required],
      vehicleReference: ['', Validators.required],
      driverName: ['', Validators.required],
      driverPhone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-\(\)]{8,15}$/)]],
      scheduledDeparture: ['', Validators.required],
      scheduledArrival: ['', Validators.required],
      
      // Expéditeur
      shipper: this.fb.group({
        name: ['', Validators.required],
        address: ['', Validators.required],
        contact: ['', Validators.required],
        phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-\(\)]{8,15}$/)]],
        reference: ['']
      }),
      
      // Destinataire
      consignee: this.fb.group({
        name: ['', Validators.required],
        address: ['', Validators.required],
        contact: ['', Validators.required],
        phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-\(\)]{8,15}$/)]],
        reference: ['']
      }),
      
      // Client donneur d'ordre
      client: this.fb.group({
        name: ['', Validators.required],
        address: ['', Validators.required],
        contact: ['', Validators.required],
        phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-\(\)]{8,15}$/)]],
        reference: ['']
      }),
      
      // Agent
      agent: this.fb.group({
        name: ['', Validators.required],
        address: ['', Validators.required],
        contact: ['', Validators.required],
        phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-\(\)]{8,15}$/)]],
        reference: ['']
      }),
      
      // Marchandises
      goods: this.fb.array([]),
      
      // Instructions et remarques
      deliveryInstructions: [''],
      generalRemarks: [''],
      status: ['Brouillon', Validators.required],
      
      // Pièces jointes
      attachments: this.fb.array([])
    });
  }

  get goodsFormArray(): FormArray {
    return this.manifestForm.get('goods') as FormArray;
  }

  get attachmentsFormArray(): FormArray {
    return this.manifestForm.get('attachments') as FormArray;
  }

  createGoodsItemForm(): FormGroup {
    return this.fb.group({
      trackingNumber: ['', Validators.required],
      description: ['', Validators.required],
      packagingType: ['Cartons', Validators.required],
      packageCount: [1, [Validators.required, Validators.min(1)]],
      grossWeight: [0, [Validators.required, Validators.min(0.01)]],
      volume: [0, [Validators.required, Validators.min(0.001)]],
      volumetricWeight: [0, [Validators.required, Validators.min(0.01)]],
      declaredValue: [0, [Validators.required, Validators.min(0)]],
      containerNumber: [''],
      remarks: ['']
    });
  }

  addGoodsItem(): void {
    this.goodsFormArray.push(this.createGoodsItemForm());
  }

  removeGoodsItem(index: number): void {
    if (this.goodsFormArray.length > 1) {
      this.goodsFormArray.removeAt(index);
    }
  }

  addAttachment(): void {
    this.attachmentsFormArray.push(this.fb.control('', Validators.required));
  }

  removeAttachment(index: number): void {
    this.attachmentsFormArray.removeAt(index);
  }

  calculateVolumetricWeight(index: number): void {
    const goodsItem = this.goodsFormArray.at(index);
    const volume = goodsItem.get('volume')?.value || 0;
    const volumetricWeight = volume * 166.67; // Coefficient standard pour le transport aérien
    goodsItem.get('volumetricWeight')?.setValue(volumetricWeight);
  }

  generateTrackingNumber(index: number): void {
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.random().toString(36).substring(2, 6).toUpperCase();
    const trackingNumber = `TRK${timestamp}${random}`;
    
    const goodsItem = this.goodsFormArray.at(index);
    goodsItem.get('trackingNumber')?.setValue(trackingNumber);
  }

  onSubmit(): void {
    if (this.manifestForm.valid) {
      this.isLoading = true;
      this.error = null;
      this.success = null;

      const formValue = this.manifestForm.value;
      
      // Transformer les données pour correspondre au backend
      const manifestRequest = {
        proformaNumber: formValue.proformaNumber,
        transportMode: formValue.transportMode,
        vehicleInfo: formValue.vehicleReference,
        driverName: formValue.driverName,
        departureDate: formValue.scheduledDeparture,
        arrivalDate: formValue.scheduledArrival,
        deliveryInstructions: formValue.deliveryInstructions,
        remarks: formValue.generalRemarks,
        attachments: formValue.attachments?.join(', ') || '', // Convertir array en string
        
        // Transformer les parties en array avec partyType
        parties: [
          {
            partyType: 'SHIPPER',
            companyName: formValue.shipper?.name || '',
            contactName: formValue.shipper?.contact || '',
            address: formValue.shipper?.address || '',
            phone: formValue.shipper?.phone || ''
          },
          {
            partyType: 'CONSIGNEE', 
            companyName: formValue.consignee?.name || '',
            contactName: formValue.consignee?.contact || '',
            address: formValue.consignee?.address || '',
            phone: formValue.consignee?.phone || ''
          },
          {
            partyType: 'CLIENT',
            companyName: formValue.client?.name || '',
            contactName: formValue.client?.contact || '',
            address: formValue.client?.address || '',
            phone: formValue.client?.phone || ''
          },
          {
            partyType: 'AGENT',
            companyName: formValue.agent?.name || '',
            contactName: formValue.agent?.contact || '',
            address: formValue.agent?.address || '',
            phone: formValue.agent?.phone || ''
          }
        ],
        
        // Transformer les marchandises
        goods: formValue.goods?.map((item: any) => ({
          trackingNumber: item.trackingNumber,
          description: item.description,
          packaging: item.packagingType,
          packageCount: item.packageCount,
          weight: item.grossWeight,
          volume: item.volume,
          value: item.declaredValue,
          currency: 'XAF',
          specialInstructions: item.remarks || ''
        })) || []
      };
      
      this.manifestService.createManifest(manifestRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.success = 'Manifeste créé avec succès!';
          console.log('Manifeste créé:', response);
          
          // Rediriger immédiatement vers la liste des manifestes
          setTimeout(() => {
            this.router.navigate(['/fret/report-manifest']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.error = 'Erreur lors de la création du manifeste: ' + (error.error?.message || error.message);
          console.error('Erreur création manifeste:', error);
        }
      });
    } else {
      this.markFormGroupTouched(this.manifestForm);
      this.error = 'Veuillez corriger les erreurs dans le formulaire.';
    }
  }

  onSaveDraft(): void {
    this.manifestForm.patchValue({ status: 'Brouillon' });
    this.onSubmit();
  }

  onPreview(): void {
    if (this.manifestForm.valid) {
      // Créer un manifeste temporaire pour prévisualisation
      const formData: ManifestFormData = this.manifestForm.value;
      this.router.navigate(['/fret/report-manifest'], { 
        queryParams: { preview: 'true' },
        state: { manifestData: formData }
      });
    } else {
      this.markFormGroupTouched(this.manifestForm);
      this.error = 'Veuillez remplir tous les champs obligatoires pour la prévisualisation.';
    }
  }

  onReset(): void {
    this.manifestForm.reset();
    this.goodsFormArray.clear();
    this.attachmentsFormArray.clear();
    this.addGoodsItem();
    this.error = null;
    this.success = null;
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            this.markFormGroupTouched(arrayControl);
          } else {
            arrayControl.markAsTouched();
          }
        });
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.manifestForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isNestedFieldInvalid(groupName: string, fieldName: string): boolean {
    const field = this.manifestForm.get(`${groupName}.${fieldName}`);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isGoodsFieldInvalid(index: number, fieldName: string): boolean {
    const field = this.goodsFormArray.at(index).get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.manifestForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return 'Ce champ est obligatoire';
      if (field.errors['minlength']) return `Minimum ${field.errors['minlength'].requiredLength} caractères`;
      if (field.errors['pattern']) return 'Format invalide';
      if (field.errors['min']) return `Valeur minimum: ${field.errors['min'].min}`;
    }
    return '';
  }
}
