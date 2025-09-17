import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'create-lta',
    loadChildren: () => import('./create-lta/create-lta.module').then(m => m.CreateLtaModule)
  },
  {
    path: 'list-lta',
    loadChildren: () => import('./list-lta/list-lta.module').then(m => m.ListLtaModule)
  },
  {
    path: 'create-manifest',
    loadChildren: () => import('./create-manifest/create-manifest.module').then(m => m.CreateManifestModule)
  },
  {
    path: 'report-manifest',
    loadChildren: () => import('./report-manifest/report-manifest.module').then(m => m.ReportManifestModule)
  },
  {
    path: 'tracking',
    loadChildren: () => import('./tracking/tracking.module').then(m => m.TrackingModule)
  },
  {
    path: 'depot-management',
    loadChildren: () => import('./depot-management/depot-management.module').then(m => m.DepotManagementModule)
  },
  {
    path: '',
    redirectTo: 'list-lta',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class FretModule { }
