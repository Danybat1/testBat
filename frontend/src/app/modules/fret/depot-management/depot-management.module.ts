import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { DepotManagementComponent } from './components/depot-management.component';

const routes: Routes = [
  {
    path: '',
    component: DepotManagementComponent
  }
];

@NgModule({
  declarations: [
    DepotManagementComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class DepotManagementModule { }
