import { Pipe, PipeTransform, OnDestroy } from '@angular/core';
import { CurrencyService } from '../../core/services/currency.service';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';

@Pipe({
  name: 'currencyFormat',
  standalone: true,
  pure: false // Pipe impur pour réagir aux changements de devise
})
export class CurrencyFormatPipe implements PipeTransform, OnDestroy {
  
  private destroy$ = new BehaviorSubject<boolean>(false);
  private lastAmount: number | null = null;
  private lastSourceCurrency: string | null = null;
  private lastResult: string = '';

  constructor(private currencyService: CurrencyService) {}

  transform(
    amount: number | null | undefined,
    sourceCurrency: string = 'USD',
    showOriginal: boolean = false
  ): Observable<string> | string {
    
    // Validation des entrées
    if (amount === null || amount === undefined || isNaN(amount)) {
      return '0';
    }

    // Optimisation: éviter les recalculs inutiles
    if (amount === this.lastAmount && sourceCurrency === this.lastSourceCurrency) {
      return this.lastResult;
    }

    this.lastAmount = amount;
    this.lastSourceCurrency = sourceCurrency;

    // Combiner la devise courante avec la conversion
    return combineLatest([
      this.currencyService.currentCurrency$,
      this.currencyService.convert(amount, sourceCurrency, this.currencyService.getCurrentCurrency())
    ]).pipe(
      takeUntil(this.destroy$),
      map(([currentCurrency, conversionResult]) => {
        let result = conversionResult.formattedAmount;
        
        // Ajouter le montant original si demandé et converti
        if (showOriginal && sourceCurrency !== currentCurrency) {
          const originalFormatted = this.currencyService.formatAmount(amount, sourceCurrency);
          result += ` (${originalFormatted})`;
        }
        
        this.lastResult = result;
        return result;
      })
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
