import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable, Subject, BehaviorSubject, combineLatest, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, takeUntil, startWith, map, take, catchError } from 'rxjs/operators';
// Material imports will be added when Angular Material is configured

import { LTAService } from '../../../../services/lta.service';
import { CityService } from '../../../../services/city.service';
import { ClientService } from '../../../../services/client.service';
import { TariffService } from '../../../../services/tariff.service';
import { CurrencyService } from '../../../../core/services/currency.service';

import { LTA, LTARequest, LTAResponse } from '../../../../models/lta.model';
import { City, CitySearchResult } from '../../../../models/city.model';
import { Client, ClientSearchResult } from '../../../../models/client.model';
import { Package } from '../../../../models/package.model';
import { PaymentMode, LTAStatus } from '../../../../models/common.model';

@Component({
  selector: 'app-lta-create',
  templateUrl: './lta-create.component.html',
  styleUrls: ['./lta-create.component.scss']
})
export class LtaCreateComponent implements OnInit, OnDestroy {
  ltaForm: FormGroup;
  loading = false;
  isEditMode = false;
  editingLTA: LTAResponse | null = null;
  
  // Enums for template
  PaymentMode = PaymentMode;
  LTAStatus = LTAStatus;
  
  // City data
  allCities: City[] = [];
  
  // Client search and selection
  filteredClients$!: Observable<ClientSearchResult[]>;
  selectedClient: ClientSearchResult | null = null;
  showClientDropdown = false;
  isSearchingClients = false;
  currentSearchTerm = '';
  selectedIndex = -1;
  
  // Cost calculation
  calculatedCost$ = new BehaviorSubject<number | null>(null);
  calculatedCostFormatted$ = new BehaviorSubject<string>('');
  currentCurrency$ = this.currencyService.currentCurrency$;
  
  private destroy$ = new Subject<void>();

  constructor(
    private formBuilder: FormBuilder,
    private ltaService: LTAService,
    private cityService: CityService,
    private clientService: ClientService,
    private tariffService: TariffService,
    private currencyService: CurrencyService,
    private router: Router,
    private route: ActivatedRoute,
    // Material services will be injected when configured
  ) {
    this.ltaForm = this.createForm();
    this.loadAllCities();
    this.setupClientAutocomplete();
    this.setupCostCalculation();
    this.setupConditionalValidation();
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    const editId = this.route.snapshot.queryParams['edit'];
    if (editId) {
      this.isEditMode = true;
      this.loadLTAForEdit(+editId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Enhanced client search and selection methods
  onClientSearchInput(event: any): void {
    const value = event.target.value;
    if (value && value.length >= 1) {
      this.showClientDropdown = true;
    } else {
      this.showClientDropdown = false;
    }
  }

  onClientSearchFocus(): void {
    const searchValue = this.ltaForm.get('clientSearch')?.value;
    if (searchValue && searchValue.length >= 1) {
      this.showClientDropdown = true;
    }
  }

  onClientSearchBlur(): void {
    // Delay hiding dropdown to allow for click events
    setTimeout(() => {
      this.showClientDropdown = false;
      this.selectedIndex = -1;
    }, 200);
  }

  onClientSearchKeydown(event: KeyboardEvent): void {
    if (!this.showClientDropdown) return;

    this.filteredClients$.pipe(take(1)).subscribe((clients: ClientSearchResult[]) => {
      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault();
          this.selectedIndex = Math.min(this.selectedIndex + 1, clients.length - 1);
          break;
        case 'ArrowUp':
          event.preventDefault();
          this.selectedIndex = Math.max(this.selectedIndex - 1, -1);
          break;
        case 'Enter':
          event.preventDefault();
          if (this.selectedIndex >= 0 && clients[this.selectedIndex]) {
            this.selectClient(clients[this.selectedIndex]);
          }
          break;
        case 'Escape':
          event.preventDefault();
          this.showClientDropdown = false;
          this.selectedIndex = -1;
          break;
      }
    });
  }

  selectClient(client: ClientSearchResult): void {
    this.selectedClient = client;
    this.ltaForm.patchValue({
      client: client,  // Store the entire client object instead of just ID
      clientSearch: client.name
    });
    this.showClientDropdown = false;
    
    // Trigger validation update
    this.ltaForm.get('client')?.updateValueAndValidity();
  }

  clearClientSelection(): void {
    this.selectedClient = null;
    this.ltaForm.patchValue({
      client: null,
      clientSearch: ''
    });
    this.showClientDropdown = false;
  }

  trackByClientId(index: number, client: ClientSearchResult): number {
    return client.id;
  }

  highlightSearchTerm(text: string, searchTerm: string): string {
    if (!text || !searchTerm) {
      return text || '';
    }
    
    const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    return text.replace(regex, '<mark class="bg-warning text-dark">$1</mark>');
  }

  private createForm(): FormGroup {
    return this.formBuilder.group({
      // Route information
      originCity: [null, [Validators.required]],
      destinationCity: [null, [Validators.required]],
      
      // Payment and client
      paymentMode: [PaymentMode.CASH, [Validators.required]],
      client: [null], // Will be required conditionally
      clientSearch: [''], // Search field for client autocomplete
      
      // Package information
      totalWeight: ['', [Validators.required, Validators.min(0.1), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
      packageNature: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      packageCount: [1, [Validators.required, Validators.min(1), Validators.pattern(/^[1-9]\d*$/)]],
      
      // Shipper and consignee
      shipperName: ['', [Validators.maxLength(200)]],
      shipperAddress: ['', [Validators.maxLength(500)]],
      shipperPhone: ['', [Validators.maxLength(20)]],
      consigneeName: ['', [Validators.maxLength(200)]],
      consigneeAddress: ['', [Validators.maxLength(500)]],
      consigneePhone: ['', [Validators.maxLength(20)]],
      
      // Additional information
      specialInstructions: ['', [Validators.maxLength(1000)]],
      declaredValue: ['', [Validators.min(0), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
      pickupDate: [''],
      
      // Status (for edit mode)
      status: [LTAStatus.DRAFT],
      
      // Packages array
      packages: this.formBuilder.array([])
    });
  }

  private loadAllCities(): void {
    this.cityService.getAllCities().subscribe({
      next: (cities) => {
        this.allCities = cities;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des villes:', error);
      }
    });
  }

  private setupClientAutocomplete(): void {
    // Setup enhanced client search autocomplete
    this.filteredClients$ = this.ltaForm.get('clientSearch')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(value => {
        const searchTerm = typeof value === 'string' ? value.trim() : '';
        this.currentSearchTerm = searchTerm;
        this.selectedIndex = -1;
        
        if (searchTerm.length >= 1) {
          this.isSearchingClients = true;
          return this.clientService.searchClients(searchTerm).pipe(
            map(clients => {
              this.isSearchingClients = false;
              return clients;
            }),
            catchError((error: any) => {
              console.error('Erreur recherche clients:', error);
              this.isSearchingClients = false;
              return of([]);
            })
          );
        } else {
          this.isSearchingClients = false;
          return of([]);
        }
      }),
      takeUntil(this.destroy$)
    );
  }

  private setupCostCalculation(): void {
    // Watch for changes in origin, destination, weight, and currency to calculate cost
    combineLatest([
      this.ltaForm.get('originCity')!.valueChanges.pipe(startWith(null)),
      this.ltaForm.get('destinationCity')!.valueChanges.pipe(startWith(null)),
      this.ltaForm.get('totalWeight')!.valueChanges.pipe(startWith(null)),
      this.currencyService.currentCurrency$
    ]).pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(([originId, destinationId, weight, currency]) => {
        if (originId && destinationId && weight > 0) {
          return this.ltaService.calculateCost(+originId, +destinationId, weight, currency);
        }
        return of(null);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (cost) => {
        this.calculatedCost$.next(cost);
        if (cost !== null) {
          const currentCurrency = this.currencyService.getCurrentCurrency();
          const formattedCost = this.currencyService.formatAmount(cost, currentCurrency);
          this.calculatedCostFormatted$.next(formattedCost);
        } else {
          this.calculatedCostFormatted$.next('');
        }
      },
      error: (error) => {
        console.error('❌ Erreur calcul coût:', error);
        this.calculatedCost$.next(null);
        this.calculatedCostFormatted$.next('');
      }
    });

    // Watch for currency changes to update formatting
    this.currencyService.currentCurrency$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(currency => {
      const currentCost = this.calculatedCost$.value;
      if (currentCost !== null) {
        const formattedCost = this.currencyService.formatAmount(currentCost, currency);
        this.calculatedCostFormatted$.next(formattedCost);
      }
    });
  }

  private setupConditionalValidation(): void {
    // Watch payment mode changes to conditionally require client
    this.ltaForm.get('paymentMode')!.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(paymentMode => {
      const clientControl = this.ltaForm.get('client');
      if (paymentMode === PaymentMode.TO_INVOICE) {
        clientControl?.setValidators([this.clientRequiredValidator]);
      } else {
        clientControl?.clearValidators();
      }
      clientControl?.updateValueAndValidity();
    });
  }

  // Custom validator for client selection
  private clientRequiredValidator = (control: any) => {
    if (!control.value || !control.value.id) {
      return { required: true };
    }
    return null;
  };

  private loadLTAForEdit(id: number): void {
    this.loading = true;
    this.ltaService.getLTAById(id).subscribe({
      next: (lta) => {
        this.editingLTA = lta;
        this.ltaForm.patchValue({
          originCity: lta.originCity?.id,
          destinationCity: lta.destinationCity?.id,
          paymentMode: lta.paymentMode,
          client: lta.client,
          totalWeight: lta.totalWeight,
          packageNature: lta.packageNature,
          packageCount: lta.packageCount,
          shipperName: lta.shipperName,
          shipperAddress: lta.shipperAddress,
          shipperPhone: lta.shipperPhone,
          consigneeName: lta.consigneeName,
          consigneeAddress: lta.consigneeAddress,
          consigneePhone: lta.consigneePhone,
          specialInstructions: lta.specialInstructions,
          declaredValue: lta.declaredValue,
          pickupDate: lta.pickupDate,
          status: lta.status
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la LTA pour modification:', error);
        this.router.navigate(['/lta/list']);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    console.log('🚀 === DÉBUT CRÉATION LTA ===');
    console.log('📋 Formulaire valide:', this.ltaForm.valid);
    console.log('📋 Statut formulaire:', this.ltaForm.status);
    console.log('📋 Erreurs formulaire:', this.getFormErrors());
    
    if (this.ltaForm.valid) {
      this.loading = true;
      const formData = this.ltaForm.value;
      
      console.log('📦 Données brutes du formulaire:', JSON.stringify(formData, null, 2));

      // Validate business rules
      if (!this.validateBusinessRules(formData)) {
        console.log('❌ Échec validation règles métier');
        this.loading = false;
        return;
      }
      
      console.log('✅ Validation règles métier réussie');

      if (this.isEditMode && this.editingLTA) {
        console.log('🔄 Mode modification LTA ID:', this.editingLTA.id);
        // Update existing LTA
        const updateRequest: LTARequest = this.buildLTARequest(formData);
        console.log('📤 Requête de modification:', JSON.stringify(updateRequest, null, 2));

        this.ltaService.updateLTA(this.editingLTA.id!, updateRequest).subscribe({
          next: (updatedLTA) => {
            console.log('✅ LTA modifiée avec succès:', updatedLTA);
            this.loading = false;
            this.router.navigate(['/lta/detail', updatedLTA.id]);
          },
          error: (error) => {
            console.error('❌ Erreur lors de la modification de la LTA:', error);
            console.error('❌ Détails erreur:', {
              status: error.status,
              statusText: error.statusText,
              message: error.message,
              error: error.error
            });
            this.loading = false;
            this.handleError(error, 'Erreur lors de la modification de la LTA');
          }
        });
      } else {
        console.log('➕ Mode création nouvelle LTA');
        // Create new LTA
        const createRequest: LTARequest = this.buildLTARequest(formData);
        console.log('📤 Requête de création:', JSON.stringify(createRequest, null, 2));
        console.log('📤 URL endpoint:', this.ltaService.getCreateEndpoint());

        this.ltaService.createLTA(createRequest).subscribe({
          next: (createdLTA) => {
            console.log('✅ LTA créée avec succès:', createdLTA);
            console.log('✅ ID de la nouvelle LTA:', createdLTA.id);
            console.log('✅ Numéro LTA:', createdLTA.ltaNumber);
            console.log('✅ Tracking Number:', createdLTA.trackingNumber);
            this.loading = false;
            this.router.navigate(['/lta/detail', createdLTA.id]);
          },
          error: (error) => {
            console.error('❌ Erreur lors de la création de la LTA:', error);
            console.error('❌ Status HTTP:', error.status);
            console.error('❌ Status Text:', error.statusText);
            console.error('❌ Message d\'erreur:', error.message);
            console.error('❌ Détails erreur backend:', error.error);
            console.error('❌ Headers de réponse:', error.headers);
            console.error('❌ URL appelée:', error.url);
            this.loading = false;
            this.handleError(error, 'Erreur lors de la création de la LTA');
          }
        });
      }
    } else {
      console.log('❌ Formulaire invalide');
      console.log('❌ Erreurs détaillées:', this.getFormErrors());
      console.log('❌ Contrôles touchés:', this.getTouchedControls());
      this.markFormGroupTouched();
      this.scrollToFirstError();
    }
    
    console.log('🏁 === FIN TRAITEMENT CRÉATION LTA ===');
  }

  onCancel(): void {
    this.router.navigate(['/lta/list']);
  }

  onReset(): void {
    if (this.isEditMode && this.editingLTA) {
      this.loadLTAForEdit(this.editingLTA.id!);
    } else {
      this.ltaForm.reset();
      this.ltaForm.patchValue({ 
        status: LTAStatus.DRAFT,
        paymentMode: PaymentMode.CASH,
        packageCount: 1
      });
    }
  }

  // Helper methods
  displayCityFn(city: CitySearchResult): string {
    return city ? city.displayText : '';
  }

  displayClientFn(client: ClientSearchResult): string {
    return client ? client.displayText : '';
  }

  // City selection methods no longer needed for dropdowns

  get packagesFormArray(): FormArray {
    return this.ltaForm.get('packages') as FormArray;
  }

  addPackage(): void {
    const packageForm = this.formBuilder.group({
      weight: ['', [Validators.required, Validators.min(0.1)]],
      description: ['', Validators.maxLength(200)]
    });
    this.packagesFormArray.push(packageForm);
  }

  removePackage(index: number): void {
    this.packagesFormArray.removeAt(index);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.ltaForm.controls).forEach(key => {
      const control = this.ltaForm.get(key);
      control?.markAsTouched();
    });
  }

  private buildLTARequest(formData: any): LTARequest {
    // Handle city data - now they are IDs from dropdown
    const originCityId = formData.originCity ? +formData.originCity : null;
    const destinationCityId = formData.destinationCity ? +formData.destinationCity : null;

    return {
      originCityId,
      destinationCityId,
      paymentMode: formData.paymentMode,
      clientId: formData.client?.id || null,
      totalWeight: parseFloat(formData.totalWeight) || 0,
      packageNature: formData.packageNature?.trim() || '',
      packageCount: parseInt(formData.packageCount) || 1,
      shipperName: formData.shipperName?.trim() || null,
      shipperAddress: formData.shipperAddress?.trim() || null,
      shipperPhone: formData.shipperPhone?.trim() || null,
      consigneeName: formData.consigneeName?.trim() || null,
      consigneeAddress: formData.consigneeAddress?.trim() || null,
      consigneePhone: formData.consigneePhone?.trim() || null,
      specialInstructions: formData.specialInstructions?.trim() || null,
      declaredValue: formData.declaredValue ? parseFloat(formData.declaredValue) : undefined,
      pickupDate: formData.pickupDate || null,
      packages: formData.packages || []
    };
  }

  private validateBusinessRules(formData: any): boolean {
    // Check if client is required for TO_INVOICE payment mode
    if (formData.paymentMode === PaymentMode.TO_INVOICE && (!formData.client || !formData.client.id)) {
      const clientControl = this.ltaForm.get('client');
      clientControl?.setErrors({ required: true });
      clientControl?.markAsTouched();
      return false;
    }

    // Check if origin and destination cities are different
    const originCityId = formData.originCity;
    const destinationCityId = formData.destinationCity;
    
    if (originCityId && destinationCityId && originCityId === destinationCityId) {
      const destControl = this.ltaForm.get('destinationCity');
      destControl?.setErrors({ sameAsOrigin: true });
      destControl?.markAsTouched();
      return false;
    }

    return true;
  }

  private getFormErrors(): any {
    const errors: any = {};
    Object.keys(this.ltaForm.controls).forEach(key => {
      const control = this.ltaForm.get(key);
      if (control && control.errors) {
        errors[key] = control.errors;
      }
    });
    return errors;
  }

  private scrollToFirstError(): void {
    const firstErrorField = document.querySelector('.is-invalid');
    if (firstErrorField) {
      firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  private handleError(error: any, defaultMessage: string): void {
    // Handle specific HTTP error codes
    if (error.status === 400) {
      if (error.error?.message) {
        alert(`Erreur de validation: ${error.error.message}`);
      } else {
        alert('Erreur de validation: Veuillez vérifier les données saisies.');
      }
    } else if (error.status === 404) {
      alert('Erreur: Ressource non trouvée.');
    } else if (error.status === 500) {
      alert('Erreur serveur: Veuillez réessayer plus tard.');
    } else {
      alert(`${defaultMessage}: ${error.message || 'Erreur inconnue'}`);
    }
  }

  // Validation helpers
  get isClientRequired(): boolean {
    return this.ltaForm.get('paymentMode')?.value === PaymentMode.TO_INVOICE;
  }

  get canCalculateCost(): boolean {
    const form = this.ltaForm;
    return !!(form.get('originCity')?.value && 
              form.get('destinationCity')?.value && 
              form.get('totalWeight')?.value > 0);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.ltaForm.get(fieldName);
    if (!field || !field.errors || !field.touched) {
      return '';
    }

    const errors = field.errors;
    const displayName = this.getFieldDisplayName(fieldName);

    if (errors['required']) {
      return `${displayName} est requis`;
    }
    if (errors['minlength']) {
      const minLength = errors['minlength'].requiredLength;
      return `${displayName} doit contenir au moins ${minLength} caractères`;
    }
    if (errors['maxlength']) {
      const maxLength = errors['maxlength'].requiredLength;
      return `${displayName} ne peut pas dépasser ${maxLength} caractères`;
    }
    if (errors['min']) {
      const min = errors['min'].min;
      return `${displayName} doit être supérieur ou égal à ${min}`;
    }
    if (errors['pattern']) {
      if (fieldName === 'totalWeight' || fieldName === 'declaredValue') {
        return `${displayName} doit être un nombre valide (ex: 12.50)`;
      }
      if (fieldName === 'packageCount') {
        return `${displayName} doit être un nombre entier positif`;
      }
      return `${displayName} a un format invalide`;
    }
    
    return `${displayName} est invalide`;
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      originCity: 'Ville d\'origine',
      destinationCity: 'Ville de destination',
      paymentMode: 'Mode de paiement',
      client: 'Client',
      totalWeight: 'Poids total',
      packageNature: 'Nature du colis',
      packageCount: 'Nombre de colis',
      shipperName: 'Nom de l\'expéditeur',
      shipperAddress: 'Adresse de l\'expéditeur',
      shipperPhone: 'Téléphone de l\'expéditeur',
      consigneeName: 'Nom du destinataire',
      consigneeAddress: 'Adresse du destinataire',
      consigneePhone: 'Téléphone du destinataire',
      declaredValue: 'Valeur déclarée'
    };
    return displayNames[fieldName] || fieldName;
  }

  private getTouchedControls(): { [key: string]: boolean } {
    const touched: { [key: string]: boolean } = {};
    Object.keys(this.ltaForm.controls).forEach(key => {
      const control = this.ltaForm.get(key);
      touched[key] = control?.touched || false;
    });
    return touched;
  }
}
