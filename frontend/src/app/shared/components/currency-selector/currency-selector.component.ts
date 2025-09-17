import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CurrencyService, Currency, CurrencyRate } from '../../../core/services/currency.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-currency-selector',
  templateUrl: './currency-selector.component.html',
  styleUrls: ['./currency-selector.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class CurrencySelectorComponent implements OnInit, OnDestroy {
  currentCurrency = 'USD';
  currencies: Currency[] = [];
  supportedCurrencies: string[] = [];
  displayRates: CurrencyRate[] = [];
  isLoading = false;
  
  private subscription = new Subscription();

  constructor(private currencyService: CurrencyService) {}

  ngOnInit(): void {
    this.isLoading = true;
    console.log('🔄 CurrencySelector: Initialisation du composant');
    
    // Charger les devises disponibles
    this.subscription.add(
      this.currencyService.getCurrencies().subscribe({
        next: (currencies) => {
          console.log('✅ CurrencySelector: Devises chargées:', currencies);
          this.currencies = currencies;
          this.supportedCurrencies = currencies.map(c => c.code);
          console.log('✅ CurrencySelector: Devises supportées:', this.supportedCurrencies);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('❌ CurrencySelector: Erreur chargement devises:', error);
          this.isLoading = false;
          // Fallback vers les devises par défaut
          this.supportedCurrencies = ['USD', 'CDF'];
          console.log('⚠️ CurrencySelector: Utilisation fallback devises:', this.supportedCurrencies);
        }
      })
    );
    
    // S'abonner aux changements de devise courante
    this.subscription.add(
      this.currencyService.currentCurrency$.subscribe(currency => {
        console.log('🔄 CurrencySelector: Changement devise courante:', currency);
        this.currentCurrency = currency;
        this.updateDisplayRates();
      })
    );

    // Charger les taux de change pour affichage
    this.subscription.add(
      this.currencyService.getCurrentRates().subscribe({
        next: (rates) => {
          console.log('✅ CurrencySelector: Taux chargés:', rates);
          this.updateDisplayRatesFromData(rates);
        },
        error: (error) => {
          console.error('❌ CurrencySelector: Erreur chargement taux:', error);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Sélectionner une nouvelle devise
   */
  selectCurrency(currency: string): void {
    if (this.supportedCurrencies.includes(currency)) {
      this.currencyService.setCurrentCurrency(currency);
    }
  }

  /**
   * Obtenir les informations de la devise courante
   */
  getCurrentCurrencyInfo(): { code: string; symbol: string; name: string } {
    return this.currencyService.getCurrencyInfo(this.currentCurrency);
  }

  /**
   * Obtenir les informations d'une devise
   */
  getCurrencyInfo(currency: string): { code: string; symbol: string; name: string } {
    return this.currencyService.getCurrencyInfo(currency);
  }

  /**
   * Obtenir l'objet Currency complet
   */
  getCurrencyObject(code: string): Currency | undefined {
    return this.currencies.find(c => c.code === code);
  }

  /**
   * Formater un taux de change pour l'affichage
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
   * Obtenir le libellé d'une paire de devises
   */
  getCurrencyPairLabel(from: string, to: string): string {
    const fromInfo = this.getCurrencyInfo(from);
    const toInfo = this.getCurrencyInfo(to);
    return `${fromInfo.symbol} → ${toInfo.symbol}`;
  }

  /**
   * Vérifier si une devise est la devise par défaut
   */
  isDefaultCurrency(code: string): boolean {
    const currency = this.getCurrencyObject(code);
    return currency?.isDefault || false;
  }

  /**
   * Obtenir la classe CSS pour une devise
   */
  getCurrencyClass(code: string): string {
    const classes = ['currency-option'];
    
    if (code === this.currentCurrency) {
      classes.push('currency-option--active');
    }
    
    if (this.isDefaultCurrency(code)) {
      classes.push('currency-option--default');
    }
    
    return classes.join(' ');
  }

  /**
   * Mettre à jour les taux affichés
   */
  private updateDisplayRates(): void {
    this.currencyService.getCurrentRates().subscribe(rates => {
      this.updateDisplayRatesFromData(rates);
    });
  }

  /**
   * Mettre à jour les taux affichés à partir des données
   */
  private updateDisplayRatesFromData(rates: CurrencyRate[]): void {
    // Filtrer les taux pertinents pour la devise courante
    this.displayRates = rates.filter(rate => 
      rate.from === this.currentCurrency || rate.to === this.currentCurrency
    ).slice(0, 3); // Limiter à 3 taux pour l'affichage
  }

  /**
   * Rafraîchir les données
   */
  refreshData(): void {
    this.isLoading = true;
    
    // Recharger les devises
    this.currencyService.loadCurrencies().subscribe({
      next: () => {
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du rafraîchissement:', error);
        this.isLoading = false;
      }
    });
    
    // Recharger les taux
    this.currencyService.loadExchangeRates().subscribe();
  }

  /**
   * Obtenir le statut de connexion au backend
   */
  getConnectionStatus(): 'connected' | 'disconnected' | 'loading' {
    if (this.isLoading) return 'loading';
    return this.currencies.length > 0 ? 'connected' : 'disconnected';
  }

  /**
   * Obtenir l'icône pour le statut de connexion
   */
  getConnectionIcon(): string {
    switch (this.getConnectionStatus()) {
      case 'connected': return 'fas fa-check-circle text-success';
      case 'disconnected': return 'fas fa-exclamation-triangle text-warning';
      case 'loading': return 'fas fa-spinner fa-spin text-info';
      default: return 'fas fa-question-circle text-muted';
    }
  }

  /**
   * Obtenir le message de statut
   */
  getStatusMessage(): string {
    switch (this.getConnectionStatus()) {
      case 'connected': return 'Connecté au serveur';
      case 'disconnected': return 'Mode hors ligne';
      case 'loading': return 'Chargement...';
      default: return 'Statut inconnu';
    }
  }
}
