import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { QRCodeModule } from 'angularx-qrcode';

import { LtaRoutingModule } from './lta-routing.module';

import { LtaListComponent } from './components/lta-list/lta-list.component';
import { LtaCreateComponent } from './components/lta-create/lta-create.component';
import { LtaDetailComponent } from './components/lta-detail/lta-detail.component';
import { LtaDashboardComponent } from './components/lta-dashboard/lta-dashboard.component';

@NgModule({
  declarations: [
    LtaListComponent,
    LtaCreateComponent,
    LtaDetailComponent,
    LtaDashboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    QRCodeModule,
    LtaRoutingModule
  ]
})
export class LtaModule { }
