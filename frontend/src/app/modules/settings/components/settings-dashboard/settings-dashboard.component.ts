import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

interface AppSettings {
  companyName: string;
  companyAddress: string;
  companyPhone: string;
  companyEmail: string;
  defaultCurrency: string;
  timeZone: string;
  language: string;
  emailNotifications: boolean;
  smsNotifications: boolean;
  autoBackup: boolean;
}

@Component({
  selector: 'app-settings-dashboard',
  templateUrl: './settings-dashboard.component.html',
  styleUrls: ['./settings-dashboard.component.scss']
})
export class SettingsDashboardComponent implements OnInit {
  settingsForm: FormGroup;
  loading = false;
  saving = false;
  
  currencies = ['EUR', 'USD', 'MAD', 'XOF'];
  timeZones = [
    'Europe/Paris',
    'Europe/London', 
    'America/New_York',
    'Africa/Casablanca',
    'Africa/Abidjan'
  ];
  languages = [
    { code: 'fr', name: 'Français' },
    { code: 'en', name: 'English' },
    { code: 'ar', name: 'العربية' }
  ];

  constructor(private fb: FormBuilder) {
    this.settingsForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadSettings();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      companyName: ['FreightOps', [Validators.required]],
      companyAddress: ['', [Validators.required]],
      companyPhone: ['', [Validators.required]],
      companyEmail: ['', [Validators.required, Validators.email]],
      defaultCurrency: ['EUR', [Validators.required]],
      timeZone: ['Europe/Paris', [Validators.required]],
      language: ['fr', [Validators.required]],
      emailNotifications: [true],
      smsNotifications: [false],
      autoBackup: [true]
    });
  }

  private loadSettings(): void {
    this.loading = true;
    // Simulate API call
    setTimeout(() => {
      const mockSettings: AppSettings = {
        companyName: 'FreightOps',
        companyAddress: '123 Rue de la Logistique, 75001 Paris, France',
        companyPhone: '+33 1 23 45 67 89',
        companyEmail: 'contact@freightops.com',
        defaultCurrency: 'EUR',
        timeZone: 'Europe/Paris',
        language: 'fr',
        emailNotifications: true,
        smsNotifications: false,
        autoBackup: true
      };
      this.settingsForm.patchValue(mockSettings);
      this.loading = false;
    }, 1000);
  }

  onSave(): void {
    if (this.settingsForm.valid) {
      this.saving = true;
      const settings: AppSettings = this.settingsForm.value;
      
      // Simulate API call
      setTimeout(() => {
        this.saving = false;
        // Show success message
      }, 1000);
    }
  }

  onReset(): void {
    this.loadSettings();
  }

  getFieldError(fieldName: string): string {
    const field = this.settingsForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `Ce champ est requis`;
      }
      if (field.errors['email']) {
        return 'Format d\'email invalide';
      }
    }
    return '';
  }
}
