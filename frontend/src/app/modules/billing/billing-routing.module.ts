import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BillingDashboardComponent } from './components/billing-dashboard/billing-dashboard.component';
import { InvoiceListComponent } from './components/invoice-list/invoice-list.component';
import { InvoiceCreateComponent } from './components/invoice-create/invoice-create.component';

const routes: Routes = [
  { path: '', component: BillingDashboardComponent },
  { path: 'dashboard', component: BillingDashboardComponent },
  { path: 'invoices', component: InvoiceListComponent },
  { path: 'invoice/create', component: InvoiceCreateComponent },
  { path: 'invoice/new', component: InvoiceCreateComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BillingRoutingModule { }
