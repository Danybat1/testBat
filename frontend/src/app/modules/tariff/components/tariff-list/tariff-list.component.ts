import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { TariffService } from '../../../../services/tariff.service';
import { CurrencyService } from '../../../../core/services/currency.service';
import { Tariff } from '../../../../models/tariff.model';
import { Subscription, Observable } from 'rxjs';

@Component({
  selector: 'app-tariff-list',
  templateUrl: './tariff-list.component.html',
  styleUrls: ['./tariff-list.component.scss']
})
export class TariffListComponent implements OnInit, OnDestroy {
  tariffs: Tariff[] = [];
  loading = false;
  searchTerm = '';
  currentCurrency = 'USD';
  
  private subscription = new Subscription();

  constructor(
    private tariffService: TariffService,
    private currencyService: CurrencyService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTariffs();
    
    // S'abonner aux changements de devise
    this.subscription.add(
      this.currencyService.currentCurrency$.subscribe(currency => {
        this.currentCurrency = currency;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  loadTariffs(): void {
    this.loading = true;
    
    this.tariffService.getTariffs().subscribe({
      next: (response: any) => {
        this.tariffs = response.content || response || [];
        this.loading = false;
      },
      error: (error: any) => {
        console.error('❌ Erreur lors du chargement des tarifs:', error);
        this.loading = false;
        this.tariffs = [];
      }
    });
  }

  navigateToNewTariff(): void {
    this.router.navigate(['/tariff/create']);
  }

  viewTariff(id: number): void {
    this.router.navigate(['/tariff/detail', id]);
  }

  editTariff(id: number): void {
    this.router.navigate(['/tariff/edit', id]);
  }

  deleteTariff(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce tarif ?')) {
      this.tariffService.deleteTariff(id).subscribe({
        next: () => {
          this.loadTariffs();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression du tarif:', error);
        }
      });
    }
  }

  get filteredTariffs(): Tariff[] {
    if (!this.searchTerm) {
      return this.tariffs;
    }
    
    return this.tariffs.filter(tariff =>
      (tariff.originCity?.name?.toLowerCase().includes(this.searchTerm.toLowerCase()) || false) ||
      (tariff.destinationCity?.name?.toLowerCase().includes(this.searchTerm.toLowerCase()) || false)
    );
  }

  formatTariffRate(rate: number): Observable<string> {
    return this.currencyService.convertAndFormat(rate, 'USD', this.currentCurrency);
  }

  getCurrencySymbol(): string {
    const currencyInfo = this.currencyService.getCurrencyInfo(this.currentCurrency);
    return currencyInfo ? currencyInfo.symbol : '$';
  }

  trackByTariffId(index: number, tariff: Tariff): number {
    return tariff.id || index;
  }
} 