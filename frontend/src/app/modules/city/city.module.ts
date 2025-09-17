import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CityListComponent } from './components/city-list/city-list.component';
import { CityFormComponent } from './components/city-form/city-form.component';
import { CityDetailComponent } from './components/city-detail/city-detail.component';

const routes: Routes = [
  {
    path: '',
    component: CityListComponent
  },
  {
    path: 'new',
    component: CityFormComponent
  },
  {
    path: 'edit/:id',
    component: CityFormComponent
  },
  {
    path: 'detail/:id',
    component: CityDetailComponent
  }
];

@NgModule({
  declarations: [
    CityListComponent,
    CityFormComponent,
    CityDetailComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class CityModule { }
