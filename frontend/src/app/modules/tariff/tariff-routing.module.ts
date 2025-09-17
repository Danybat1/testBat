import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TariffListComponent } from './components/tariff-list/tariff-list.component';
import { TariffFormComponent } from './components/tariff-form/tariff-form.component';
import { TariffDetailComponent } from './components/tariff-detail/tariff-detail.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'list',
    pathMatch: 'full'
  },
  {
    path: 'list',
    component: TariffListComponent,
    data: { title: 'Liste des Tarifs' }
  },
  {
    path: 'create',
    component: TariffFormComponent,
    data: { title: 'Créer un Tarif' }
  },
  {
    path: 'edit/:id',
    component: TariffFormComponent,
    data: { title: 'Modifier un Tarif' }
  },
  {
    path: 'detail/:id',
    component: TariffDetailComponent,
    data: { title: 'Détail du Tarif' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TariffRoutingModule { }
