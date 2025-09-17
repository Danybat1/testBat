import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CurrencyService, Currency, ExchangeRate } from '../../../core/services/currency.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-currency-admin',
  templateUrl: './currency-admin.component.html',
  styleUrls: ['./currency-admin.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class CurrencyAdminComponent implements OnInit, OnDestroy {
  
  currencies: Currency[] = [];
  exchangeRates: ExchangeRate[] = [];
  updateForm: FormGroup;
  isLoading = false;
  message = '';
  messageType: 'success' | 'error' | 'info' = 'info';
  
  private subscription = new Subscription();

  constructor(
    private currencyService: CurrencyService,
    private fb: FormBuilder
  ) {
    this.updateForm = this.fb.group({
      fromCurrency: ['', Validators.required],
      toCurrency: ['', Validators.required],
      rate: ['', [Validators.required, Validators.min(0.000001)]]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Charger toutes les données
   */
  loadData(): void {
    this.isLoading = true;
    
    // Charger les devises
    this.subscription.add(
      this.currencyService.getCurrencies().subscribe({
        next: (currencies) => {
          this.currencies = currencies;
          this.isLoading = false;
        },
        error: (error) => {
          this.showMessage('Erreur lors du chargement des devises', 'error');
          this.isLoading = false;
        }
      })
    );
    
    // Charger les taux de change
    this.subscription.add(
      this.currencyService.getCurrentRates().subscribe({
        next: (rates) => {
          // Convertir CurrencyRate[] vers ExchangeRate[] pour l'affichage
          this.exchangeRates = rates.map(rate => ({
            fromCurrency: this.getCurrencyObject(rate.from)!,
            toCurrency: this.getCurrencyObject(rate.to)!,
            rate: rate.rate,
            effectiveDate: rate.lastUpdated.toISOString(),
            isActive: true
          }));
        },
        error: (error) => {
          this.showMessage('Erreur lors du chargement des taux', 'error');
        }
      })
    );
  }

  /**
   * Mettre à jour un taux de change
   */
  updateExchangeRate(): void {
    if (this.updateForm.valid) {
      this.isLoading = true;
      const formValue = this.updateForm.value;
      
      this.subscription.add(
        this.currencyService.updateExchangeRate(
          formValue.fromCurrency,
          formValue.toCurrency,
          parseFloat(formValue.rate)
        ).subscribe({
          next: (updatedRate) => {
            this.showMessage(
              `Taux ${formValue.fromCurrency}/${formValue.toCurrency} mis à jour avec succès`,
              'success'
            );
            this.updateForm.reset();
            this.loadData(); // Recharger les données
          },
          error: (error) => {
            this.showMessage('Erreur lors de la mise à jour du taux', 'error');
            this.isLoading = false;
          }
        })
      );
    } else {
      this.showMessage('Veuillez remplir tous les champs correctement', 'error');
    }
  }

  /**
   * Rafraîchir les données
   */
  refreshData(): void {
    this.loadData();
    this.showMessage('Données rafraîchies', 'info');
  }

  /**
   * Obtenir l'objet Currency par code
   */
  getCurrencyObject(code: string): Currency | undefined {
    return this.currencies.find(c => c.code === code);
  }

  /**
   * Formater un taux pour l'affichage
   */
  formatRate(rate: number): string {
    if (rate < 1) {
      return rate.toFixed(6);
    } else if (rate < 100) {
      return rate.toFixed(2);
    } else {
      return rate.toLocaleString('fr-FR', { maximumFractionDigits: 0 });
    }
  }

  /**
   * Obtenir la paire de devises inverse
   */
  getInverseRate(rate: ExchangeRate): string {
    const inverseRate = 1 / rate.rate;
    return `${rate.toCurrency.code}/${rate.fromCurrency.code}: ${this.formatRate(inverseRate)}`;
  }

  /**
   * Vérifier si un taux existe déjà
   */
  rateExists(fromCurrency: string, toCurrency: string): boolean {
    return this.exchangeRates.some(rate => 
      rate.fromCurrency.code === fromCurrency && rate.toCurrency.code === toCurrency
    );
  }

  /**
   * Obtenir le taux actuel pour une paire
   */
  getCurrentRate(fromCurrency: string, toCurrency: string): number | null {
    const rate = this.exchangeRates.find(rate => 
      rate.fromCurrency.code === fromCurrency && rate.toCurrency.code === toCurrency
    );
    return rate ? rate.rate : null;
  }

  /**
   * Pré-remplir le formulaire avec un taux existant
   */
  editRate(rate: ExchangeRate): void {
    this.updateForm.patchValue({
      fromCurrency: rate.fromCurrency.code,
      toCurrency: rate.toCurrency.code,
      rate: rate.rate
    });
  }

  /**
   * Afficher un message
   */
  private showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    this.message = message;
    this.messageType = type;
    
    // Masquer le message après 5 secondes
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  /**
   * Obtenir la classe CSS pour le message
   */
  getMessageClass(): string {
    switch (this.messageType) {
      case 'success': return 'alert alert-success';
      case 'error': return 'alert alert-danger';
      case 'info': return 'alert alert-info';
      default: return 'alert alert-info';
    }
  }

  /**
   * Calculer le taux de change suggéré (inverse)
   */
  calculateSuggestedRate(): number | null {
    const formValue = this.updateForm.value;
    if (formValue.fromCurrency && formValue.toCurrency) {
      const inverseRate = this.getCurrentRate(formValue.toCurrency, formValue.fromCurrency);
      return inverseRate ? (1 / inverseRate) : null;
    }
    return null;
  }

  /**
   * Utiliser le taux suggéré
   */
  useSuggestedRate(): void {
    const suggestedRate = this.calculateSuggestedRate();
    if (suggestedRate) {
      this.updateForm.patchValue({ rate: suggestedRate.toFixed(6) });
    }
  }
}
