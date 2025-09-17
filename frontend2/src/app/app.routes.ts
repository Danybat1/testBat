import { Routes } from '@angular/router';
import { TrackingComponent } from './components/tracking/tracking.component';

export const routes: Routes = [
  { path: '', component: TrackingComponent },
  { path: 'tracking', component: TrackingComponent },
  { path: '**', redirectTo: '' }
];
