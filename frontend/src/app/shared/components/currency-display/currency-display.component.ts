import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CurrencyService } from '../../../core/services/currency.service';
import { Subscription, BehaviorSubject } from 'rxjs';
import { switchMap, distinctUntilChanged, debounceTime } from 'rxjs/operators';

interface CurrencyDisplayResult {
  formattedAmount: string;
  isConverted: boolean;
  originalFormatted?: string;
}

@Component({
  selector: 'app-currency-display',
  templateUrl: './currency-display.component.html',
  styleUrls: ['./currency-display.component.scss'],
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CurrencyDisplayComponent implements OnInit, OnDestroy {
  
  @Input() amount: number = 0;
  @Input() sourceCurrency: string = 'USD'; // Devise source du montant
  @Input() showOriginal: boolean = false; // Afficher le montant original
  @Input() showSymbol: boolean = true; // Afficher le symbole de devise
  @Input() showConversion: boolean = true; // Afficher la conversion
  @Input() size: 'sm' | 'md' | 'lg' = 'md'; // Taille d'affichage
  @Input() color: 'primary' | 'success' | 'warning' | 'danger' | 'muted' = 'primary';
  
  displayAmount: string = '';
  originalAmount: string = '';
  isConverted: boolean = false;
  currentCurrency: string = 'USD';
  
  private subscription = new Subscription();
  private amountSubject = new BehaviorSubject<number>(0);
  
  constructor(
    private currencyService: CurrencyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // S'abonner aux changements de devise courante
    this.subscription.add(
      this.currencyService.currentCurrency$.subscribe(currency => {
        this.currentCurrency = currency;
        this.updateDisplay();
      })
    );

    // S'abonner aux changements de montant avec debounce
    this.subscription.add(
      this.amountSubject.pipe(
        distinctUntilChanged(),
        debounceTime(100),
        switchMap(amount => {
          if (this.sourceCurrency === this.currentCurrency || !this.showConversion) {
            // Pas de conversion nécessaire
            return new BehaviorSubject<CurrencyDisplayResult>({
              formattedAmount: this.currencyService.formatAmount(amount, this.currentCurrency),
              isConverted: false
            });
          } else {
            // Conversion nécessaire
            return this.currencyService.convert(amount, this.sourceCurrency, this.currentCurrency)
              .pipe(
                switchMap(result => new BehaviorSubject<CurrencyDisplayResult>({
                  formattedAmount: result.formattedAmount,
                  isConverted: true,
                  originalFormatted: this.currencyService.formatAmount(amount, this.sourceCurrency)
                }))
              );
          }
        })
      ).subscribe(result => {
        this.displayAmount = result.formattedAmount;
        this.isConverted = result.isConverted;
        if ('originalFormatted' in result && result.originalFormatted) {
          this.originalAmount = result.originalFormatted;
        }
        this.cdr.markForCheck();
      })
    );

    // Initialiser avec le montant actuel
    this.updateAmount();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  ngOnChanges(): void {
    this.updateAmount();
  }

  private updateAmount(): void {
    this.amountSubject.next(this.amount || 0);
  }

  private updateDisplay(): void {
    this.updateAmount();
  }

  /**
   * Obtenir les classes CSS pour le style
   */
  getDisplayClasses(): string {
    const classes = ['currency-display'];
    
    // Taille
    classes.push(`currency-display--${this.size}`);
    
    // Couleur
    classes.push(`currency-display--${this.color}`);
    
    // État de conversion
    if (this.isConverted) {
      classes.push('currency-display--converted');
    }
    
    return classes.join(' ');
  }

  /**
   * Obtenir le titre pour le tooltip
   */
  getTooltipTitle(): string {
    if (this.isConverted && this.showOriginal) {
      return `Montant original: ${this.originalAmount}`;
    }
    return '';
  }
}
