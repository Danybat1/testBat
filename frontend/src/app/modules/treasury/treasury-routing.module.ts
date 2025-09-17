import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TreasuryDashboardComponent } from './components/treasury-dashboard/treasury-dashboard.component';
import { CaisseOperationComponent } from './components/caisse-operation/caisse-operation.component';
import { CashStatementComponent } from './components/cash-statement/cash-statement.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: TreasuryDashboardComponent
  },
  {
    path: 'caisse-operation',
    component: CaisseOperationComponent
  },
  {
    path: 'cash-statement',
    component: CashStatementComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TreasuryRoutingModule { }
