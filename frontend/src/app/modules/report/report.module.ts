import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ReportDashboardComponent } from './components/report-dashboard/report-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: ReportDashboardComponent
  }
];

@NgModule({
  declarations: [
    ReportDashboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class ReportModule { }
