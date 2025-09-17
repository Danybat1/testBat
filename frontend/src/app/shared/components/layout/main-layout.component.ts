import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { CurrencySelectorComponent } from '../currency-selector/currency-selector.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, CurrencySelectorComponent]
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  pageTitle = 'Tableau de Bord';
  activeRoute = '';
  isMobile = false;
  
  // Dropdown states
  fretDropdownOpen = false;
  financesDropdownOpen = false;

  private destroy$ = new Subject<void>();
  private pageTitles: { [key: string]: string } = {
    '/dashboard': 'Tableau de Bord',
    '/lta/create': 'Créer LTA',
    '/lta/list': 'Liste des LTA',
    '/lta/detail': 'Détail LTA',
    '/fret/create-lta': 'Créer LTA',
    '/fret/list-lta': 'Liste des LTA',
    '/fret/report-manifest': 'Rapport Manifeste',
    '/fret/depot-management': 'Gestion Dépôts',
    '/clients': 'Gestion des Clients',
    '/tariff': 'Gestion des Tarifs',
    '/tariff/create': 'Créer Tarif',
    '/cities': 'Gestion des Villes',
    '/billetterie': 'Billetterie',
    '/billing': 'Facturation',
    '/finances/tresorerie': 'Trésorerie',
    '/treasury': 'Trésorerie',
    '/accounting': 'Comptabilité',
    '/accounting/dashboard': 'Tableau de Bord Comptable',
    '/accounting/journal-entries': 'Écritures Comptables',
    '/accounting/trial-balance': 'Balance Comptable',
    '/accounting/chart-of-accounts': 'Plan Comptable',
    '/accounting/general-ledger': 'Grand Livre',
    '/accounting/financial-reports': 'Rapports Financiers',
    '/tracking': 'Suivi Colis',
    '/reports': 'Rapports'
  };

  constructor(private router: Router, private authService: AuthService) {
    this.checkScreenSize();
  }

  ngOnInit(): void {
    // Subscribe to router events to update active route
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event) => {
        if (event instanceof NavigationEnd) {
          this.activeRoute = event.urlAfterRedirects;
          this.updatePageTitle(event.url);
          
          // Close mobile sidebar on navigation
          if (this.isMobile) {
            this.sidebarCollapsed = true;
          }
        }
      });

    // Set initial page title
    this.updatePageTitle(this.router.url);
    
    // Initialize sidebar state based on screen size
    this.initializeSidebarState();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any): void {
    this.checkScreenSize();
    this.initializeSidebarState();
  }

  private checkScreenSize(): void {
    if (typeof window !== 'undefined') {
      this.isMobile = window.innerWidth < 992;
    } else {
      // Default to desktop on server-side rendering
      this.isMobile = false;
    }
  }

  private initializeSidebarState(): void {
    if (this.isMobile) {
      // On mobile, sidebar is collapsed by default
      this.sidebarCollapsed = true;
    } else {
      // On desktop, restore previous state or default to expanded
      if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
        const savedState = localStorage.getItem('sidebar-collapsed');
        this.sidebarCollapsed = savedState ? JSON.parse(savedState) : false;
      } else {
        this.sidebarCollapsed = false;
      }
    }
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    
    // Save state for desktop
    if (!this.isMobile && typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('sidebar-collapsed', JSON.stringify(this.sidebarCollapsed));
    }
  }

  closeMobileSidebar(): void {
    if (this.isMobile) {
      this.sidebarCollapsed = true;
    }
  }

  // Toggle dropdown functions with improved logic
  toggleFretDropdown(): void {
    if (this.sidebarCollapsed && !this.isMobile) {
      // If sidebar is collapsed on desktop, expand it first
      this.sidebarCollapsed = false;
      localStorage.setItem('sidebar-collapsed', 'false');
    }
    
    this.fretDropdownOpen = !this.fretDropdownOpen;
    // Close other dropdowns
    this.financesDropdownOpen = false;
  }

  toggleFinancesDropdown(): void {
    if (this.sidebarCollapsed && !this.isMobile) {
      // If sidebar is collapsed on desktop, expand it first
      this.sidebarCollapsed = false;
      localStorage.setItem('sidebar-collapsed', 'false');
    }
    
    this.financesDropdownOpen = !this.financesDropdownOpen;
    // Close other dropdowns
    this.fretDropdownOpen = false;
  }

  // Close all dropdowns
  closeAllDropdowns(): void {
    this.fretDropdownOpen = false;
    this.financesDropdownOpen = false;
  }

  // Navigation helpers
  navigateToTariff(event: Event): void {
    event.preventDefault();
    this.closeMobileSidebar();
    this.router.navigate(['/tariff']).then(success => {
      if (!success) {
        // Fallback navigation
        setTimeout(() => {
          if (this.router.url === '/') {
            window.location.href = '/tariff';
          }
        }, 100);
      }
    }).catch(error => {
      console.error('Erreur de navigation:', error);
    });
  }

  navigateToAccounting(): void {
    this.closeMobileSidebar();
    this.router.navigateByUrl('/accounting/dashboard').then(success => {
      if (!success) {
        console.error('Navigation vers comptabilité échouée');
      }
    }).catch(error => {
      console.error('NavigateByUrl error:', error);
    });
  }

  logout(): void {
    // Close dropdowns and sidebar
    this.closeAllDropdowns();
    this.closeMobileSidebar();
    
    // Clear saved sidebar state
    localStorage.removeItem('sidebar-collapsed');
    
    // Logout
    this.authService.logout();
  }

  // Check if route is active (for highlighting)
  isRouteActive(route: string): boolean {
    return this.activeRoute === route || this.activeRoute.startsWith(route + '/');
  }

  // Check if dropdown should be active
  isDropdownActive(routes: string[]): boolean {
    return routes.some(route => this.isRouteActive(route));
  }

  private updatePageTitle(url: string): void {
    // Remove query parameters and fragments
    const cleanUrl = url.split('?')[0].split('#')[0];
    
    // Find exact match first
    if (this.pageTitles[cleanUrl]) {
      this.pageTitle = this.pageTitles[cleanUrl];
      return;
    }
    
    // Find partial match
    const matchingKey = Object.keys(this.pageTitles).find(key => 
      cleanUrl.startsWith(key) && key !== '/'
    );
    
    if (matchingKey) {
      this.pageTitle = this.pageTitles[matchingKey];
    } else {
      // Default title
      this.pageTitle = 'FreightOps';
    }
  }

  // Utility methods for template
  getSidebarClasses(): string {
    const classes = [];
    
    if (this.sidebarCollapsed) {
      classes.push('active');
    }
    
    if (!this.sidebarCollapsed && this.isMobile) {
      classes.push('mobile-open');
    }
    
    return classes.join(' ');
  }

  getContentClasses(): string {
    const classes = [];
    
    if (this.sidebarCollapsed) {
      classes.push('sidebar-collapsed');
    }
    
    return classes.join(' ');
  }

  // Accessibility helpers
  getSidebarAriaLabel(): string {
    return this.sidebarCollapsed ? 'Sidebar réduit' : 'Sidebar étendu';
  }

  getToggleButtonAriaLabel(): string {
    return this.sidebarCollapsed ? 'Étendre le sidebar' : 'Réduire le sidebar';
  }

  // Performance optimization - track by functions for ngFor
  trackByRoute(index: number, item: any): string {
    return item.route || index;
  }
}
