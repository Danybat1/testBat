import { Component, OnInit } from '@angular/core';
import { LTAService } from '../../../services/lta.service';
import { ClientService } from '../../../services/client.service';
import { LTA } from '../../../models/lta.model';

interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;
}

interface StatusOverview {
  label: string;
  count: number;
  percentage: number;
  class: string;
}

interface RoutePerformance {
  origin: string;
  destination: string;
  shipmentCount: number;
  totalWeight: number;
  revenue: number;
  averageRate: number;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  // Statistics
  totalLTAs = 0;
  totalClients = 0;
  totalRevenue = 0;
  pendingShipments = 0;

  // Recent LTAs
  recentLTAs: LTA[] = [];

  // Status Overview
  statusOverview: StatusOverview[] = [];

  // Top Routes
  topRoutes: RoutePerformance[] = [];

  constructor(
    private ltaService: LTAService,
    private clientService: ClientService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.loadLTAStatistics();
    this.loadClientStatistics();
    this.loadRecentLTAs();
    this.loadStatusOverview();
    this.loadRoutePerformance();
  }

  private loadLTAStatistics(): void {
    this.ltaService.getLTAs().subscribe({
      next: (response: any) => {
        const ltas = response.content || response || [];
        this.totalLTAs = ltas.length;
        this.pendingShipments = ltas.filter((lta: LTA) => 
          lta.status === 'IN_TRANSIT' || lta.status === 'CONFIRMED'
        ).length;
        this.totalRevenue = ltas.reduce((sum: number, lta: LTA) => 
          sum + (lta.calculatedCost || 0), 0
        );
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques LTA:', error);
        // Set default values
        this.totalLTAs = 0;
        this.pendingShipments = 0;
        this.totalRevenue = 0;
      }
    });
  }

  private loadClientStatistics(): void {
    this.clientService.getClients().subscribe({
      next: (response: any) => {
        const clients = response.content || response || [];
        this.totalClients = clients.length;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques clients:', error);
        this.totalClients = 0;
      }
    });
  }

  private loadRecentLTAs(): void {
    this.ltaService.getLTAs().subscribe({
      next: (response: any) => {
        this.recentLTAs = response.content || response || [];
      },
      error: (error) => {
        console.error('Erreur lors du chargement des LTA récentes:', error);
        this.recentLTAs = [];
      }
    });
  }

  private loadStatusOverview(): void {
    this.ltaService.getLTAs().subscribe({
      next: (response: any) => {
        const ltas = response.content || response || [];
        const statusCounts = this.calculateStatusCounts(ltas);
        this.statusOverview = this.buildStatusOverview(statusCounts, ltas.length);
      },
      error: (error) => {
        console.error('Erreur lors du chargement de l\'aperçu des statuts:', error);
        this.statusOverview = [];
      }
    });
  }

  private calculateStatusCounts(ltas: LTA[]): { [key: string]: number } {
    return ltas.reduce((counts: { [key: string]: number }, lta: LTA) => {
      const status = lta.status || 'DRAFT';
      counts[status] = (counts[status] || 0) + 1;
      return counts;
    }, {});
  }

  private buildStatusOverview(statusCounts: { [key: string]: number }, total: number): StatusOverview[] {
    const statusConfig = [
      { key: 'DRAFT', label: 'Brouillon', class: 'bg-secondary' },
      { key: 'CONFIRMED', label: 'Confirmé', class: 'bg-primary' },
      { key: 'IN_TRANSIT', label: 'En Transit', class: 'bg-warning' },
      { key: 'DELIVERED', label: 'Livré', class: 'bg-success' },
      { key: 'CANCELLED', label: 'Annulé', class: 'bg-danger' }
    ];

    return statusConfig.map(config => ({
      label: config.label,
      count: statusCounts[config.key] || 0,
      percentage: total > 0 ? ((statusCounts[config.key] || 0) / total) * 100 : 0,
      class: config.class
    }));
  }

  private loadRoutePerformance(): void {
    this.ltaService.getLTAs().subscribe({
      next: (response: any) => {
        const ltas = response.content || response || [];
        this.topRoutes = this.calculateRoutePerformance(ltas);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des performances des routes:', error);
        this.topRoutes = [];
      }
    });
  }

  private calculateRoutePerformance(ltas: LTA[]): RoutePerformance[] {
    const routeMap = new Map<string, RoutePerformance>();

    ltas.forEach(lta => {
      if (lta.originCity && lta.destinationCity) {
        const routeKey = `${lta.originCity.iataCode}-${lta.destinationCity.iataCode}`;
        
        if (!routeMap.has(routeKey)) {
          routeMap.set(routeKey, {
            origin: lta.originCity.iataCode || '',
            destination: lta.destinationCity.iataCode || '',
            shipmentCount: 0,
            totalWeight: 0,
            revenue: 0,
            averageRate: 0
          });
        }

        const route = routeMap.get(routeKey)!;
        route.shipmentCount++;
        route.totalWeight += lta.totalWeight || 0;
        route.revenue += lta.calculatedCost || 0;
      }
    });

    // Calculate average rates and sort by revenue
    const routes = Array.from(routeMap.values());
    routes.forEach(route => {
      route.averageRate = route.totalWeight > 0 ? route.revenue / route.totalWeight : 0;
    });

    return routes.sort((a, b) => b.revenue - a.revenue).slice(0, 5);
  }

  getStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'DRAFT': 'bg-secondary',
      'CONFIRMED': 'bg-primary',
      'IN_TRANSIT': 'bg-warning',
      'DELIVERED': 'bg-success',
      'CANCELLED': 'bg-danger'
    };
    return statusClasses[status] || 'bg-secondary';
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      'DRAFT': 'Brouillon',
      'CONFIRMED': 'Confirmé',
      'IN_TRANSIT': 'En Transit',
      'DELIVERED': 'Livré',
      'CANCELLED': 'Annulé'
    };
    return statusLabels[status] || status;
  }
}
