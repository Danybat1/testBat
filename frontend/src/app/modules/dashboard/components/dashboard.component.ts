import { Component, OnInit } from '@angular/core';
import { LoggerService } from '../../../services/logger.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  constructor(private logger: LoggerService) { }

  ngOnInit(): void {
    this.logger.info('Dashboard component initialized');
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.logger.group('Dashboard Data Loading');
    
    try {
      this.logger.debug('Starting dashboard data fetch');
      
      // Simulation du chargement de données
      const mockData = {
        totalLTAs: 150,
        pendingLTAs: 25,
        completedLTAs: 125,
        revenue: 45000
      };
      
      this.logger.log('Dashboard data loaded successfully', mockData);
      this.logger.table(mockData, 'Dashboard Statistics');
      
    } catch (error) {
      this.logger.error('Failed to load dashboard data', error);
    } finally {
      this.logger.groupEnd();
    }
  }

  onRefreshData(): void {
    this.logger.log('User requested dashboard refresh');
    this.loadDashboardData();
  }

  onExportData(): void {
    this.logger.debug('Export function called');
    try {
      // Simulation d'export
      this.logger.info('Data export completed successfully');
    } catch (error) {
      this.logger.error('Export failed', error);
    }
  }

  private handleWarning(message: string): void {
    this.logger.warn(`Dashboard warning: ${message}`);
  }
}
