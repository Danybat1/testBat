import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: 'accounting',
    loadChildren: () => import('./modules/accounting/accounting.module').then(m => m.AccountingModule),
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./shared/shared.module').then(m => m.SharedModule),
    canActivate: [authGuard]
  },
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'track',
    loadChildren: () => import('./modules/tracking/tracking.module').then(m => m.TrackingModule)
  },
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
    path: 'cities',
    loadChildren: () => import('./modules/city/city.module').then(m => m.CityModule),
    canActivate: [authGuard]
  },
  {
    path: 'users',
    loadChildren: () => import('./modules/user/user.module').then(m => m.UserModule),
    canActivate: [authGuard]
  },
  {
    path: 'tariff',
    loadChildren: () => import('./modules/tariff/tariff.module').then(m => m.TariffModule),
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
    path: 'tracking',
    loadChildren: () => import('./modules/tracking/tracking.module').then(m => m.TrackingModule)
  },
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    enableTracing: false,
    scrollPositionRestoration: 'top'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
