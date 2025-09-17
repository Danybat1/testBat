import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

// Angular Material
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';

// Routing
import { TreasuryRoutingModule } from './treasury-routing.module';

// Components
import { TreasuryDashboardComponent } from './components/treasury-dashboard/treasury-dashboard.component';
import { CashBoxListComponent } from './components/cash-box-list/cash-box-list.component';
import { BankAccountListComponent } from './components/bank-account-list/bank-account-list.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { CaisseOperationComponent } from './components/caisse-operation/caisse-operation.component';
import { CashStatementComponent } from './components/cash-statement/cash-statement.component';

// Services
import { CashBoxService } from './services/cash-box.service';

@NgModule({
  declarations: [
    TreasuryDashboardComponent,
    CashBoxListComponent,
    BankAccountListComponent,
    TransactionListComponent,
    CaisseOperationComponent,
    CashStatementComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    TreasuryRoutingModule,
    
    // Angular Material
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatChipsModule,
    MatTabsModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatDividerModule,
    HttpClientModule
  ],
  providers: [
    CashBoxService
  ]
})
export class TreasuryModule { }
