import { Component, OnInit } from '@angular/core';

export interface FinancialReport {
  id: string;
  name: string;
  description: string;
  category: ReportCategory;
  icon: string;
  lastGenerated?: Date;
  isAvailable: boolean;
}

export enum ReportCategory {
  BALANCE_SHEET = 'BALANCE_SHEET',
  INCOME_STATEMENT = 'INCOME_STATEMENT',
  CASH_FLOW = 'CASH_FLOW',
  ANALYSIS = 'ANALYSIS'
}

export interface ReportData {
  title: string;
  period: string;
  generatedAt: Date;
  data: any;
}

@Component({
  selector: 'app-financial-reports',
  templateUrl: './financial-reports.component.html',
  styleUrls: ['./financial-reports.component.scss']
})
export class FinancialReportsComponent implements OnInit {
  reports: FinancialReport[] = [];
  selectedReport: FinancialReport | null = null;
  reportData: ReportData | null = null;
  loading = false;
  selectedPeriod = 'current_month';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;

  periods = [
    { value: 'current_month', label: 'Mois en cours' },
    { value: 'last_month', label: 'Mois dernier' },
    { value: 'current_quarter', label: 'Trimestre en cours' },
    { value: 'last_quarter', label: 'Trimestre dernier' },
    { value: 'current_year', label: 'Année en cours' },
    { value: 'last_year', label: 'Année dernière' },
    { value: 'custom', label: 'Période personnalisée' }
  ];

  reportCategories = Object.values(ReportCategory);

  constructor() { }

  ngOnInit(): void {
    this.loadAvailableReports();
  }

  loadAvailableReports(): void {
    this.reports = [
      {
        id: 'balance_sheet',
        name: 'Bilan comptable',
        description: 'État de la situation financière à une date donnée',
        category: ReportCategory.BALANCE_SHEET,
        icon: 'account_balance',
        lastGenerated: new Date('2024-01-31'),
        isAvailable: true
      },
      {
        id: 'income_statement',
        name: 'Compte de résultat',
        description: 'Résultat des opérations sur une période',
        category: ReportCategory.INCOME_STATEMENT,
        icon: 'trending_up',
        lastGenerated: new Date('2024-01-31'),
        isAvailable: true
      },
      {
        id: 'cash_flow',
        name: 'Tableau de flux de trésorerie',
        description: 'Mouvements de trésorerie par activité',
        category: ReportCategory.CASH_FLOW,
        icon: 'account_balance_wallet',
        isAvailable: true
      },
      {
        id: 'trial_balance_report',
        name: 'Balance générale',
        description: 'Balance de tous les comptes',
        category: ReportCategory.ANALYSIS,
        icon: 'balance',
        lastGenerated: new Date('2024-01-30'),
        isAvailable: true
      },
      {
        id: 'aging_report',
        name: 'Balance âgée clients',
        description: 'Analyse des créances par ancienneté',
        category: ReportCategory.ANALYSIS,
        icon: 'schedule',
        isAvailable: true
      },
      {
        id: 'profitability_analysis',
        name: 'Analyse de rentabilité',
        description: 'Analyse des marges par service',
        category: ReportCategory.ANALYSIS,
        icon: 'analytics',
        isAvailable: false
      }
    ];
  }

  selectReport(report: FinancialReport): void {
    if (!report.isAvailable) return;
    
    this.selectedReport = report;
    this.generateReport();
  }

  generateReport(): void {
    if (!this.selectedReport) return;
    
    this.loading = true;
    
    // Mock data generation
    setTimeout(() => {
      this.reportData = this.generateMockReportData(this.selectedReport!);
      this.loading = false;
    }, 1500);
  }

  generateMockReportData(report: FinancialReport): ReportData {
    const period = this.getPeriodLabel();
    
    switch (report.id) {
      case 'balance_sheet':
        return {
          title: 'Bilan comptable',
          period: period,
          generatedAt: new Date(),
          data: {
            assets: {
              current: [
                { account: '411 - Clients', amount: 125000 },
                { account: '512 - Banque', amount: 85000 },
                { account: '531 - Caisse', amount: 15000 },
                { account: '31 - Stocks', amount: 45000 }
              ],
              fixed: [
                { account: '211 - Terrains', amount: 200000 },
                { account: '213 - Constructions', amount: 350000 },
                { account: '218 - Matériel transport', amount: 85000 }
              ]
            },
            liabilities: {
              current: [
                { account: '445 - TVA collectée', amount: 32400 },
                { account: '421 - Fournisseurs', amount: 45000 }
              ],
              equity: [
                { account: '101 - Capital social', amount: 500000 },
                { account: '106 - Réserves', amount: 125000 },
                { account: '12 - Résultat exercice', amount: 202600 }
              ]
            }
          }
        };
        
      case 'income_statement':
        return {
          title: 'Compte de résultat',
          period: period,
          generatedAt: new Date(),
          data: {
            revenue: [
              { account: '701 - Ventes transport', amount: 180000 },
              { account: '706 - Services additionnels', amount: 25000 }
            ],
            expenses: [
              { account: '607 - Achats carburant', amount: 95000 },
              { account: '621 - Personnel', amount: 45000 },
              { account: '622 - Charges sociales', amount: 12400 },
              { account: '625 - Entretien véhicules', amount: 18000 }
            ],
            netIncome: 34600
          }
        };
        
      case 'cash_flow':
        return {
          title: 'Tableau de flux de trésorerie',
          period: period,
          generatedAt: new Date(),
          data: {
            operating: [
              { description: 'Encaissements clients', amount: 180000 },
              { description: 'Paiements fournisseurs', amount: -120000 },
              { description: 'Paiements salaires', amount: -57400 }
            ],
            investing: [
              { description: 'Acquisition matériel', amount: -25000 }
            ],
            financing: [
              { description: 'Apport capital', amount: 50000 }
            ],
            netCashFlow: 27600
          }
        };
        
      default:
        return {
          title: report.name,
          period: period,
          generatedAt: new Date(),
          data: { message: 'Données non disponibles pour ce rapport' }
        };
    }
  }

  getPeriodLabel(): string {
    const selected = this.periods.find(p => p.value === this.selectedPeriod);
    return selected ? selected.label : 'Période personnalisée';
  }

  getCategoryLabel(category: ReportCategory): string {
    const labels = {
      [ReportCategory.BALANCE_SHEET]: 'Bilan',
      [ReportCategory.INCOME_STATEMENT]: 'Résultat',
      [ReportCategory.CASH_FLOW]: 'Trésorerie',
      [ReportCategory.ANALYSIS]: 'Analyse'
    };
    return labels[category];
  }

  getReportsByCategory(category: ReportCategory): FinancialReport[] {
    return this.reports.filter(report => report.category === category);
  }

  exportReport(format: 'pdf' | 'excel'): void {
    // TODO: Export du rapport
  }

  printReport(): void {
    window.print();
  }

  goBackToReports(): void {
    this.selectedReport = null;
    this.reportData = null;
  }

  onPeriodChange(): void {
    if (this.selectedReport) {
      this.generateReport();
    }
  }

  getSubtotal(items: any[]): number {
    return items.reduce((sum, item) => sum + Math.abs(item.amount), 0);
  }

  getTotalAssets(): number {
    if (!this.reportData?.data?.assets) return 0;
    return this.getSubtotal(this.reportData.data.assets.fixed) + 
           this.getSubtotal(this.reportData.data.assets.current);
  }

  getTotalLiabilities(): number {
    if (!this.reportData?.data?.liabilities) return 0;
    return this.getSubtotal(this.reportData.data.liabilities.equity) + 
           this.getSubtotal(this.reportData.data.liabilities.current);
  }
}
