import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TariffRoutingModule } from './tariff-routing.module';
import { TariffListComponent } from './components/tariff-list/tariff-list.component';
import { TariffFormComponent } from './components/tariff-form/tariff-form.component';
import { TariffDetailComponent } from './components/tariff-detail/tariff-detail.component';

@NgModule({
  declarations: [
    TariffListComponent,
    TariffFormComponent,
    TariffDetailComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    TariffRoutingModule
  ]
})
export class TariffModule { } 