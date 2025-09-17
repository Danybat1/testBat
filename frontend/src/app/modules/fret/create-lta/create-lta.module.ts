import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// Import existing LTA module to reuse components
import { LtaModule } from '../../lta/lta.module';

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('../../lta/lta.module').then(m => m.LtaModule)
  }
];

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    LtaModule
  ]
})
export class CreateLtaModule { }
