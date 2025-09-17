import { Component, OnInit } from '@angular/core';
import { InvoiceService } from '../../services/invoice.service';
import { QuoteService } from '../../services/quote.service';
import { PaymentService } from '../../services/payment.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-billing-dashboard',
  templateUrl: './billing-dashboard.component.html',
  styleUrls: ['./billing-dashboard.component.scss']
})
export class BillingDashboardComponent implements OnInit {
  stats = {
    totalInvoices: 0,
    totalQuotes: 0,
    totalPaidAmount: 0,
    totalRemainingAmount: 0,
    overdueInvoices: 0,
    expiredQuotes: 0,
    recentPayments: 0
  };

  recentInvoices: any[] = [];
  recentQuotes: any[] = [];
  recentPayments: any[] = [];
  overdueInvoices: any[] = [];

  loading = true;

  constructor(
    private invoiceService: InvoiceService,
    private quoteService: QuoteService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    const today = new Date();
    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const endOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);

    const startDate = startOfMonth.toISOString().split('T')[0];
    const endDate = endOfMonth.toISOString().split('T')[0];

    forkJoin({
      allInvoices: this.invoiceService.getAllInvoices(),
      allQuotes: this.quoteService.getAllQuotes(),
      overdueInvoices: this.invoiceService.getOverdueInvoices(),
      expiredQuotes: this.quoteService.getExpiredQuotes(),
      totalPaidAmount: this.invoiceService.getTotalPaidAmount(startDate, endDate),
      totalRemainingAmount: this.invoiceService.getTotalRemainingAmount(),
      recentPayments: this.paymentService.getPaymentsByDateRange(startDate, endDate)
    }).subscribe({
      next: (data) => {
        this.stats.totalInvoices = data.allInvoices.length;
        this.stats.totalQuotes = data.allQuotes.length;
        this.stats.totalPaidAmount = data.totalPaidAmount;
        this.stats.totalRemainingAmount = data.totalRemainingAmount;
        this.stats.overdueInvoices = data.overdueInvoices.length;
        this.stats.expiredQuotes = data.expiredQuotes.length;
        this.stats.recentPayments = data.recentPayments.length;

        // Recent data (last 10 items)
        this.recentInvoices = data.allInvoices
          .sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime())
          .slice(0, 10);

        this.recentQuotes = data.allQuotes
          .sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime())
          .slice(0, 10);

        this.recentPayments = data.recentPayments
          .sort((a, b) => new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime())
          .slice(0, 10);

        this.overdueInvoices = data.overdueInvoices.slice(0, 10);

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PAID': return 'primary';
      case 'SENT': return 'accent';
      case 'OVERDUE': return 'warn';
      case 'DRAFT': return 'basic';
      case 'CANCELLED': return 'warn';
      default: return 'basic';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'XAF'
    }).format(amount);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }
}
