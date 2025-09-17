import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LtaDashboardComponent } from './components/lta-dashboard/lta-dashboard.component';
import { LtaListComponent } from './components/lta-list/lta-list.component';
import { LtaCreateComponent } from './components/lta-create/lta-create.component';
import { LtaDetailComponent } from './components/lta-detail/lta-detail.component';

const routes: Routes = [
  {
    path: '',
    component: LtaDashboardComponent,
    children: [
      {
        path: '',
        redirectTo: 'list',
        pathMatch: 'full'
      },
      {
        path: 'list',
        component: LtaListComponent
      },
      {
        path: 'create',
        component: LtaCreateComponent
      }
    ]
  },
  {
    path: 'detail/:id',
    component: LtaDetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LtaRoutingModule { }
