import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TariffService } from '../../../../services/tariff.service';
import { CityService } from '../../../../services/city.service';
import { Tariff, TariffRequest } from '../../../../models/tariff.model';
import { City } from '../../../../models/city.model';

@Component({
  selector: 'app-tariff-form',
  templateUrl: './tariff-form.component.html',
  styleUrls: ['./tariff-form.component.scss']
})
export class TariffFormComponent implements OnInit {
  tariffForm: FormGroup;
  isEditMode = false;
  tariffId?: number;
  loading = false;
  submitting = false;
  cities: City[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private tariffService: TariffService,
    private cityService: CityService
  ) {
    this.tariffForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadCities();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.tariffId = +params['id'];
        this.loadTariff(this.tariffId);
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      originCityId: ['', Validators.required],
      destinationCityId: ['', Validators.required],
      kgRate: ['', [Validators.required, Validators.min(0)]],
      volumeCoeffV1: [0],
      volumeCoeffV2: [0],
      volumeCoeffV3: [0],
      isActive: [true],
      effectiveFrom: [''],
      effectiveUntil: ['']
    });
  }

  private loadCities(): void {
    this.cityService.getAllCities().subscribe({
      next: (cities) => {
        this.cities = cities;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des villes:', error);
      }
    });
  }

  private loadTariff(id: number): void {
    this.loading = true;
    this.tariffService.getTariffById(id).subscribe({
      next: (tariff) => {
        this.tariffForm.patchValue({
          originCityId: tariff.originCity.id,
          destinationCityId: tariff.destinationCity.id,
          kgRate: tariff.kgRate,
          volumeCoeffV1: tariff.volumeCoeffV1,
          volumeCoeffV2: tariff.volumeCoeffV2,
          volumeCoeffV3: tariff.volumeCoeffV3,
          isActive: tariff.isActive,
          effectiveFrom: tariff.effectiveFrom,
          effectiveUntil: tariff.effectiveUntil
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement du tarif:', error);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.tariffForm.valid) {
      this.submitting = true;
      const tariffData: TariffRequest = this.tariffForm.value;

      const operation = this.isEditMode 
        ? this.tariffService.updateTariff(this.tariffId!, tariffData)
        : this.tariffService.createTariff(tariffData);

      operation.subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/tariff']);
        },
        error: (error) => {
          console.error('Erreur lors de la sauvegarde du tarif:', error);
          this.submitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/tariff']);
  }
} 