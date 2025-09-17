import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Angular Material
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';

// Shared Module
import { SharedModule } from '../shared/shared.module';

// Components
import { AccountingDashboardComponent } from './components/dashboard/accounting-dashboard.component';
import { JournalEntriesComponent } from './components/journal-entries/journal-entries.component';
import { TrialBalanceComponent } from './components/trial-balance/trial-balance.component';
import { ChartOfAccountsComponent } from './components/chart-of-accounts/chart-of-accounts.component';
import { GeneralLedgerComponent } from './components/general-ledger/general-ledger.component';
import { FinancialReportsComponent } from './components/financial-reports/financial-reports.component';
// import { JournalEntryDetailComponent } from './components/journal-entry-detail/journal-entry-detail.component';
// import { AccountingRulesComponent } from './components/accounting-rules/accounting-rules.component';
// import { FiscalPeriodsComponent } from './components/fiscal-periods/fiscal-periods.component';
// import { AuditLogComponent } from './components/audit-log/audit-log.component';

// Services
// import { AccountingService } from './services/accounting.service';
// import { JournalEntryService } from './services/journal-entry.service';
// import { AccountService } from './services/account.service';
// import { FiscalYearService } from './services/fiscal-year.service';

// Routing
import { AccountingRoutingModule } from './accounting-routing.module';

@NgModule({
  declarations: [
    AccountingDashboardComponent,
    JournalEntriesComponent,
    TrialBalanceComponent,
    ChartOfAccountsComponent,
    GeneralLedgerComponent,
    FinancialReportsComponent
    // JournalEntryDetailComponent,
    // AccountingRulesComponent,
    // FiscalPeriodsComponent,
    // AuditLogComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModule,
    AccountingRoutingModule,
    RouterModule,
    
    // Angular Material
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTabsModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTooltipModule,
    MatExpansionModule
  ],
  providers: [
    // AccountingService,
    // JournalEntryService,
    // AccountService,
    // FiscalYearService
  ]
})
export class AccountingModule { }
