import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CityService } from '../../../../services/city.service';
import { CityRequest } from '../../../../models/city.model';

@Component({
  selector: 'app-city-form',
  templateUrl: './city-form.component.html',
  styleUrls: ['./city-form.component.scss']
})
export class CityFormComponent implements OnInit {
  cityForm: FormGroup;
  isEditMode = false;
  cityId?: number;
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private cityService: CityService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.cityForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.cityId = +params['id'];
        this.loadCity();
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      iataCode: ['', [
        Validators.required, 
        Validators.pattern(/^[A-Z]{3}$/),
        Validators.minLength(3),
        Validators.maxLength(3)
      ]]
    });
  }

  private loadCity(): void {
    if (!this.cityId) return;
    
    this.loading = true;
    this.cityService.getCityById(this.cityId).subscribe({
      next: (city) => {
        this.cityForm.patchValue(city);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de la ville:', error);
        this.loading = false;
        this.router.navigate(['/cities']);
      }
    });
  }

  onSubmit(): void {
    if (this.cityForm.valid) {
      this.submitting = true;
      const cityData: CityRequest = {
        name: this.cityForm.value.name,
        iataCode: this.cityForm.value.iataCode.toUpperCase()
      };

      const operation = this.isEditMode 
        ? this.cityService.updateCity(this.cityId!, cityData)
        : this.cityService.createCity(cityData);

      operation.subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/cities']);
        },
        error: (error) => {
          console.error('Erreur lors de la sauvegarde:', error);
          this.submitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/cities']);
  }

  onIataCodeInput(event: any): void {
    // Convert to uppercase automatically
    const value = event.target.value.toUpperCase();
    this.cityForm.patchValue({ iataCode: value });
  }

  getFieldError(fieldName: string): string {
    const field = this.cityForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `Le champ ${fieldName === 'iataCode' ? 'code IATA' : fieldName} est requis`;
      }
      if (field.errors['minlength']) {
        return `Le champ doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
      }
      if (field.errors['maxlength']) {
        return `Le champ doit contenir au maximum ${field.errors['maxlength'].requiredLength} caractères`;
      }
      if (field.errors['pattern']) {
        return 'Le code IATA doit contenir exactement 3 lettres majuscules (ex: CDG, JFK)';
      }
    }
    return '';
  }
}
