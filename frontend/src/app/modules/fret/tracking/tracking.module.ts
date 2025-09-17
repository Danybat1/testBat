import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { TrackingComponent } from './components/tracking.component';

const routes: Routes = [
  {
    path: '',
    component: TrackingComponent
  }
];

@NgModule({
  declarations: [
    TrackingComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class TrackingModule { }
