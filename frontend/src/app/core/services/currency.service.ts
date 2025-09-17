import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, timer } from 'rxjs';
import { catchError, map, retry, shareReplay, switchMap, takeUntil } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Currency {
  code: string;
  name: string;
  symbol: string;
  decimalPlaces: number;
  isActive: boolean;
  isDefault: boolean;
}

export interface ExchangeRate {
  id?: number;
  fromCurrency: Currency;
  toCurrency: Currency;
  rate: number;
  effectiveDate: string;
  isActive: boolean;
}

export interface CurrencyRate {
  from: string;
  to: string;
  rate: number;
  lastUpdated: Date;
}

export interface ConversionResult {
  originalAmount: number;
  convertedAmount: number;
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  formattedAmount: string;
}

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private readonly apiUrl = `${environment.apiUrl}/currencies`;
  
  // État de la devise courante
  private currentCurrencySubject = new BehaviorSubject<string>('USD');
  public currentCurrency$ = this.currentCurrencySubject.asObservable();
  
  // Cache des devises et taux
  private currenciesCache$ = new BehaviorSubject<Currency[]>([]);
  private exchangeRatesCache$ = new BehaviorSubject<ExchangeRate[]>([]);
  
  // Mise à jour automatique des taux (toutes les 5 minutes)
  private readonly REFRESH_INTERVAL = 5 * 60 * 1000; // 5 minutes
  
  constructor(private http: HttpClient) {
    this.initializeService();
    this.startPeriodicRefresh();
  }

  /**
   * Initialiser le service
   */
  private initializeService(): void {
    // Charger la devise préférée depuis le localStorage
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const savedCurrency = localStorage.getItem('preferredCurrency');
      if (savedCurrency && ['USD', 'CDF'].includes(savedCurrency)) {
        this.currentCurrencySubject.next(savedCurrency);
      }
    }
    
    // Charger les données initiales
    this.loadCurrencies().subscribe();
    this.loadExchangeRates().subscribe();
  }

  /**
   * Démarrer la mise à jour périodique des taux
   */
  private startPeriodicRefresh(): void {
    timer(this.REFRESH_INTERVAL, this.REFRESH_INTERVAL)
      .pipe(
        switchMap(() => this.loadExchangeRates()),
        catchError(error => {
          console.warn('Erreur lors de la mise à jour des taux:', error);
          return throwError(error);
        })
      )
      .subscribe();
  }

  /**
   * Charger toutes les devises depuis le backend
   */
  loadCurrencies(): Observable<Currency[]> {
    console.log('🌐 CurrencyService: Tentative de chargement des devises depuis:', `${this.apiUrl}`);
    return this.http.get<Currency[]>(`${this.apiUrl}`)
      .pipe(
        retry(2),
        shareReplay(1),
        map(currencies => {
          console.log('✅ CurrencyService: Devises reçues du backend:', currencies);
          this.currenciesCache$.next(currencies);
          return currencies;
        }),
        catchError(error => {
          console.error('❌ CurrencyService: Erreur lors du chargement des devises:', error);
          console.error('❌ CurrencyService: URL utilisée:', `${this.apiUrl}`);
          console.error('❌ CurrencyService: Status:', error.status);
          console.error('❌ CurrencyService: Message:', error.message);
          // Fallback vers les données locales
          console.log('⚠️ CurrencyService: Utilisation des données de fallback');
          return this.getFallbackCurrencies();
        })
      );
  }

  /**
   * Charger tous les taux de change depuis le backend
   */
  loadExchangeRates(): Observable<ExchangeRate[]> {
    console.log('🌐 CurrencyService: Tentative de chargement des taux depuis:', `${this.apiUrl}/rates`);
    return this.http.get<ExchangeRate[]>(`${this.apiUrl}/rates`)
      .pipe(
        retry(2),
        shareReplay(1),
        map(rates => {
          console.log('✅ CurrencyService: Taux reçus du backend:', rates);
          this.exchangeRatesCache$.next(rates);
          return rates;
        }),
        catchError(error => {
          console.error('❌ CurrencyService: Erreur lors du chargement des taux:', error);
          console.error('❌ CurrencyService: URL utilisée:', `${this.apiUrl}/rates`);
          console.error('❌ CurrencyService: Status:', error.status);
          console.error('❌ CurrencyService: Message:', error.message);
          // Fallback vers les données locales
          console.log('⚠️ CurrencyService: Utilisation des taux de fallback');
          return this.getFallbackRates();
        })
      );
  }

  /**
   * Obtenir toutes les devises disponibles
   */
  getCurrencies(): Observable<Currency[]> {
    return this.currenciesCache$.asObservable();
  }

  /**
   * Obtenir les devises supportées (codes uniquement)
   */
  getSupportedCurrencies(): string[] {
    const currencies = this.currenciesCache$.value;
    return currencies.length > 0 
      ? currencies.map(c => c.code)
      : ['USD', 'CDF']; // Fallback
  }

  /**
   * Obtenir les informations d'une devise
   */
  getCurrencyInfo(currency: string): { code: string; symbol: string; name: string } {
    const currencies = this.currenciesCache$.value;
    const found = currencies.find(c => c.code === currency);
    
    if (found) {
      return {
        code: found.code,
        symbol: found.symbol,
        name: found.name
      };
    }
    
    // Fallback pour les devises de base
    const fallbackInfo: { [key: string]: { code: string; symbol: string; name: string } } = {
      'USD': { code: 'USD', symbol: '$', name: 'Dollar Américain' },
      'CDF': { code: 'CDF', symbol: 'FC', name: 'Franc Congolais' }
    };
    
    return fallbackInfo[currency] || { code: currency, symbol: currency, name: currency };
  }

  /**
   * Définir la devise courante
   */
  setCurrentCurrency(currency: string): void {
    if (this.getSupportedCurrencies().includes(currency)) {
      this.currentCurrencySubject.next(currency);
      if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
        localStorage.setItem('preferredCurrency', currency);
      }
    }
  }

  /**
   * Obtenir la devise courante
   */
  getCurrentCurrency(): string {
    return this.currentCurrencySubject.value;
  }

  /**
   * Convertir un montant via le backend
   */
  convert(amount: number, fromCurrency: string, toCurrency: string): Observable<ConversionResult> {
    if (fromCurrency === toCurrency) {
      return new BehaviorSubject({
        originalAmount: amount,
        convertedAmount: amount,
        fromCurrency,
        toCurrency,
        rate: 1,
        formattedAmount: this.formatAmount(amount, toCurrency)
      }).asObservable();
    }

    const request = {
      amount: amount,
      fromCurrency: fromCurrency,
      toCurrency: toCurrency
    };

    return this.http.post<ConversionResult>(`${this.apiUrl}/convert`, request)
      .pipe(
        retry(1),
        catchError(error => {
          console.error('Erreur lors de la conversion:', error);
          // Fallback vers conversion locale
          return this.convertLocally(amount, fromCurrency, toCurrency);
        })
      );
  }

  /**
   * Conversion locale en cas d'échec du backend
   */
  private convertLocally(amount: number, fromCurrency: string, toCurrency: string): Observable<ConversionResult> {
    const rate = this.getLocalExchangeRate(fromCurrency, toCurrency);
    const convertedAmount = amount * rate;
    
    return new BehaviorSubject({
      originalAmount: amount,
      convertedAmount: Math.round(convertedAmount * 100) / 100,
      fromCurrency,
      toCurrency,
      rate,
      formattedAmount: this.formatAmount(convertedAmount, toCurrency)
    }).asObservable();
  }

  /**
   * Obtenir le taux de change local (fallback)
   */
  private getLocalExchangeRate(fromCurrency: string, toCurrency: string): number {
    const rates: { [key: string]: number } = {
      'USD_CDF': 2700,
      'CDF_USD': 0.000370
    };
    
    const key = `${fromCurrency}_${toCurrency}`;
    return rates[key] || 1;
  }

  /**
   * Formater un montant avec la devise
   */
  formatAmount(amount: number, currency: string): string {
    const currencyInfo = this.getCurrencyInfo(currency);
    
    switch (currency) {
      case 'USD':
        return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      case 'CDF':
        return `${amount.toLocaleString('fr-CD', { minimumFractionDigits: 0, maximumFractionDigits: 0 })} FC`;
      default:
        return `${currencyInfo.symbol}${amount.toFixed(2)}`;
    }
  }

  /**
   * Formater un coût avec la devise courante
   */
  formatCost(amount: number): string {
    const currentCurrency = this.getCurrentCurrency();
    return this.formatAmount(amount, currentCurrency);
  }

  /**
   * Convertir et formater un montant
   */
  convertAndFormat(amount: number, fromCurrency: string, toCurrency: string): Observable<string> {
    return this.convert(amount, fromCurrency, toCurrency)
      .pipe(map(result => result.formattedAmount));
  }

  /**
   * Obtenir les taux de change actuels pour affichage
   */
  getCurrentRates(): Observable<CurrencyRate[]> {
    const rates = this.exchangeRatesCache$.value;
    
    if (rates.length > 0) {
      return new BehaviorSubject(
        rates.map(rate => ({
          from: rate.fromCurrency.code,
          to: rate.toCurrency.code,
          rate: rate.rate,
          lastUpdated: new Date(rate.effectiveDate)
        }))
      ).asObservable();
    }
    
    // Fallback vers les taux locaux
    return this.getFallbackRates().pipe(
      map(rates => rates.map(rate => ({
        from: rate.fromCurrency.code,
        to: rate.toCurrency.code,
        rate: rate.rate,
        lastUpdated: new Date()
      })))
    );
  }

  /**
   * Mettre à jour un taux de change (Admin)
   */
  updateExchangeRate(fromCurrency: string, toCurrency: string, newRate: number): Observable<ExchangeRate> {
    const request = {
      fromCurrency,
      toCurrency,
      rate: newRate,
      updatedBy: 'ADMIN'
    };

    return this.http.put<ExchangeRate>(`${this.apiUrl}/rates`, request)
      .pipe(
        map(updatedRate => {
          // Mettre à jour le cache
          this.loadExchangeRates().subscribe();
          return updatedRate;
        }),
        catchError(error => {
          console.error('Erreur lors de la mise à jour du taux:', error);
          return throwError(error);
        })
      );
  }

  /**
   * Données de fallback pour les devises
   */
  private getFallbackCurrencies(): Observable<Currency[]> {
    const fallbackCurrencies: Currency[] = [
      {
        code: 'USD',
        name: 'Dollar Américain',
        symbol: '$',
        decimalPlaces: 2,
        isActive: true,
        isDefault: true
      },
      {
        code: 'CDF',
        name: 'Franc Congolais',
        symbol: 'FC',
        decimalPlaces: 0,
        isActive: true,
        isDefault: false
      }
    ];
    
    this.currenciesCache$.next(fallbackCurrencies);
    return new BehaviorSubject(fallbackCurrencies).asObservable();
  }

  /**
   * Données de fallback pour les taux
   */
  private getFallbackRates(): Observable<ExchangeRate[]> {
    const currencies = this.currenciesCache$.value;
    const usd = currencies.find(c => c.code === 'USD');
    const cdf = currencies.find(c => c.code === 'CDF');
    
    if (!usd || !cdf) {
      return new BehaviorSubject([]).asObservable();
    }
    
    const fallbackRates: ExchangeRate[] = [
      {
        fromCurrency: usd,
        toCurrency: cdf,
        rate: 2700,
        effectiveDate: new Date().toISOString(),
        isActive: true
      },
      {
        fromCurrency: cdf,
        toCurrency: usd,
        rate: 0.000370,
        effectiveDate: new Date().toISOString(),
        isActive: true
      }
    ];
    
    this.exchangeRatesCache$.next(fallbackRates);
    return new BehaviorSubject(fallbackRates).asObservable();
  }
}
