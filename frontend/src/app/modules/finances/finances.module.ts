import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'tresorerie',
    loadChildren: () => import('./tresorerie/tresorerie.module').then(m => m.TresorerieModule)
  },
  {
    path: 'comptabilite',
    loadChildren: () => import('./comptabilite/comptabilite.module').then(m => m.ComptabiliteModule)
  },
  {
    path: '',
    redirectTo: 'tresorerie',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class FinancesModule { }
