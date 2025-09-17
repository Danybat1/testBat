import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AccountingDashboardComponent } from './components/dashboard/accounting-dashboard.component';
import { JournalEntriesComponent } from './components/journal-entries/journal-entries.component';
import { TrialBalanceComponent } from './components/trial-balance/trial-balance.component';
import { ChartOfAccountsComponent } from './components/chart-of-accounts/chart-of-accounts.component';
import { GeneralLedgerComponent } from './components/general-ledger/general-ledger.component';
import { FinancialReportsComponent } from './components/financial-reports/financial-reports.component';

const routes: Routes = [
  { path: '', component: AccountingDashboardComponent },
  { path: 'dashboard', component: AccountingDashboardComponent },
  { path: 'journal-entries', component: JournalEntriesComponent },
  { path: 'trial-balance', component: TrialBalanceComponent },
  { path: 'chart-of-accounts', component: ChartOfAccountsComponent },
  { path: 'general-ledger', component: GeneralLedgerComponent },
  { path: 'financial-reports', component: FinancialReportsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountingRoutingModule { }
