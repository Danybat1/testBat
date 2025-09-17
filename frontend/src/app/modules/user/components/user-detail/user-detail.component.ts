import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService, User } from '../../../../services/user.service';

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {
  user: User | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadUser(+id);
    } else {
      this.router.navigate(['/users']);
    }
  }

  private loadUser(id: number): void {
    this.loading = true;
    this.userService.getUserById(id).subscribe({
      next: (user: User) => {
        this.user = user;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement de l\'utilisateur:', error);
        alert('Erreur lors du chargement de l\'utilisateur');
        this.router.navigate(['/users']);
        this.loading = false;
      }
    });
  }

  editUser(): void {
    if (this.user) {
      this.router.navigate(['/users/edit', this.user.id]);
    }
  }

  deleteUser(): void {
    if (this.user && confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur ${this.user.username}?`)) {
      this.userService.deleteUser(this.user.id!).subscribe({
        next: () => {
          alert('Utilisateur supprimé avec succès');
          this.router.navigate(['/users']);
        },
        error: (error: any) => {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/users']);
  }

  getStatusClass(isActive: boolean): string {
    return isActive ? 'bg-success text-white' : 'bg-danger text-white';
  }

  getStatusLabel(isActive: boolean): string {
    return isActive ? 'Actif' : 'Inactif';
  }

  getRoleClass(role: string): string {
    const roleClasses: { [key: string]: string } = {
      'ADMIN': 'bg-danger text-white',
      'AGENT': 'bg-primary text-white',
      'FINANCE': 'bg-warning text-dark'
    };
    return roleClasses[role] || 'bg-secondary text-white';
  }

  getRoleLabel(role: string): string {
    const roleLabels: { [key: string]: string } = {
      'ADMIN': 'Administrateur',
      'AGENT': 'Agent',
      'FINANCE': 'Finance'
    };
    return roleLabels[role] || role;
  }

  getLastLoginDate(lastLoginAt: string | null): string {
    if (!lastLoginAt) {
      return 'Jamais connecté';
    }
    return new Date(lastLoginAt).toLocaleString('fr-FR');
  }
} 