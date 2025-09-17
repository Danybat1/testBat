import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface ModuleCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  requiredRole?: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class HomeComponent {
  
  modules: ModuleCard[] = [
    {
      title: 'Gestion LTA',
      description: 'Gérer les lettres de transport aérien, créer, modifier et suivre les expéditions',
      icon: 'airplane',
      route: '/lta',
      color: 'blue',
      requiredRole: 'AGENT'
    },
    {
      title: 'Gestion Clients',
      description: 'Gérer la base de données clients, ajouter et modifier les informations',
      icon: 'people',
      route: '/clients',
      color: 'green'
    },
    {
      title: 'Gestion Utilisateurs',
      description: 'Administrer les comptes utilisateurs et leurs permissions',
      icon: 'person-gear',
      route: '/users',
      color: 'purple',
      requiredRole: 'ADMIN'
    },
    {
      title: 'Gestion Villes',
      description: 'Gérer les destinations et les villes de transport',
      icon: 'geo-alt',
      route: '/cities',
      color: 'orange'
    },
    {
      title: 'Rapports',
      description: 'Consulter les statistiques et générer des rapports',
      icon: 'graph-up',
      route: '/reports',
      color: 'red',
      requiredRole: 'FINANCE'
    },
    {
      title: 'Paramètres',
      description: 'Configuration générale de l\'application',
      icon: 'gear',
      route: '/settings',
      color: 'gray',
      requiredRole: 'ADMIN'
    }
  ];

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  navigateToModule(route: string): void {
    this.router.navigate([route]);
  }

  canAccessModule(module: ModuleCard): boolean {
    if (!module.requiredRole) {
      return true;
    }
    
    return this.authService.hasRole(module.requiredRole);
  }

  getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
