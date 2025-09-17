import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { ReportManifestComponent } from './components/report-manifest.component';
import { ManifestFormComponent } from './components/manifest-form.component';
import { ManifestService } from './services/manifest.service';

const routes: Routes = [
  {
    path: '',
    component: ReportManifestComponent
  },
  {
    path: 'create',
    component: ManifestFormComponent
  },
  {
    path: 'edit/:id',
    component: ManifestFormComponent
  }
];

@NgModule({
  declarations: [
    ReportManifestComponent,
    ManifestFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ],
  providers: [
    ManifestService
  ]
})
export class ReportManifestModule { }
