import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomeComponent } from './components/home/home.component';

export const routes: Routes = [
  // Redirect root to dashboard
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  
  // Dashboard Module (lazy-loaded)
  {
    path: 'dashboard',
    loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [authGuard]
  },
  
  // Fret Module (lazy-loaded with sub-routes)
  {
    path: 'fret',
    loadChildren: () => import('./modules/fret/fret.module').then(m => m.FretModule),
    canActivate: [authGuard]
  },
  
  // Billetrerie Module (lazy-loaded)
  {
    path: 'billetrerie',
    loadChildren: () => import('./modules/billetrerie/billetrerie.module').then(m => m.BilleterieModule),
    canActivate: [authGuard]
  },
  
  // Facturation Module (lazy-loaded)
  {
    path: 'facturation',
    loadChildren: () => import('./modules/facturation/facturation.module').then(m => m.FacturationModule),
    canActivate: [authGuard]
  },
  
  // Finances Module (lazy-loaded with sub-routes)
  {
    path: 'finances',
    loadChildren: () => import('./modules/finances/finances.module').then(m => m.FinancesModule),
    canActivate: [authGuard]
  },
  
  // Legacy routes for backward compatibility
  {
    path: 'lta',
    loadChildren: () => import('./modules/lta/lta.module').then(m => m.LtaModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'clients',
    loadChildren: () => import('./modules/client/client.module').then(m => m.ClientModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'tariff',
    loadChildren: () => import('./modules/tariff/tariff.module').then(m => m.TariffModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'cities',
    loadChildren: () => import('./modules/city/city.module').then(m => m.CityModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'billing',
    loadChildren: () => import('./modules/billing/billing.module').then(m => m.BillingModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'treasury',
    loadChildren: () => import('./modules/treasury/treasury.module').then(m => m.TreasuryModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'accounting',
    loadChildren: () => import('./modules/accounting/accounting.module').then(m => m.AccountingModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'reports',
    loadChildren: () => import('./modules/report/report.module').then(m => m.ReportModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'tracking',
    loadChildren: () => import('./modules/tracking/tracking.module').then(m => m.TrackingModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'settings',
    loadChildren: () => import('./modules/settings/settings.module').then(m => m.SettingsModule),
    canActivate: [authGuard]
  },
  
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
  },
  
  // Wildcard route - must be last
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
