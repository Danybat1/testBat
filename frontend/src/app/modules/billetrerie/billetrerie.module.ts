import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { BilleterieComponent } from './components/billetrerie.component';

const routes: Routes = [
  {
    path: '',
    component: BilleterieComponent
  }
];

@NgModule({
  declarations: [
    BilleterieComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class BilleterieModule { }
