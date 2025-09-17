import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SettingsDashboardComponent } from './components/settings-dashboard/settings-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: SettingsDashboardComponent
  }
];

@NgModule({
  declarations: [
    SettingsDashboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class SettingsModule { }
