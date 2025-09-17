import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { LoginRequest } from '../../../../models/auth.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  hidePassword = true;
  returnUrl = '/lta';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    // Get return url from route parameters or default to '/lta'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/lta';

    // Redirect if already logged in
    this.authService.isAuthenticated$.subscribe(isAuth => {
      if (isAuth) {
        this.router.navigate([this.returnUrl]);
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      const credentials: LoginRequest = this.loginForm.value;

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.loading = false;
          alert('Connexion réussie!');
          this.router.navigate([this.returnUrl]);
        },
        error: (error) => {
          this.loading = false;
          console.error('🚨 Login component error:', error);
          
          let errorMessage = 'Erreur de connexion. Veuillez réessayer.';
          
          if (error.status === 401) {
            errorMessage = 'Nom d\'utilisateur ou mot de passe incorrect.';
          } else if (error.status === 0) {
            errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré sur le port 8080.';
          } else if (error.status === 404) {
            errorMessage = 'Endpoint d\'authentification non trouvé. Vérifiez l\'URL de l\'API.';
          }

          alert(errorMessage);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} est requis`;
    }
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `${this.getFieldDisplayName(fieldName)} doit contenir au moins ${minLength} caractères`;
    }
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      username: 'Nom d\'utilisateur',
      password: 'Mot de passe'
    };
    return displayNames[fieldName] || fieldName;
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  // Demo credentials helper
  fillDemoCredentials(role: 'admin' | 'agent' | 'finance'): void {
    const credentials = {
      admin: { username: 'admin', password: 'admin123' },
      agent: { username: 'agent', password: 'agent123' },
      finance: { username: 'finance', password: 'finance123' }
    };

    this.loginForm.patchValue(credentials[role]);
  }
}
