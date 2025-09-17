import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, User } from '../../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly TOKEN_KEY = 'freightops_token';
  private readonly USER_KEY = 'freightops_user';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private router: Router,
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Pour le développement, toujours simuler un utilisateur connecté
    this.isAuthenticatedSubject.next(true);
    this.currentUserSubject.next({ username: 'dev-user', roles: ['ROLE_ADMIN'] });
    
    // Only initialize authentication state in browser
    if (isPlatformBrowser(this.platformId)) {
      // Skip normal auth initialization in development
      if (environment.production) {
        this.initializeAuthState();
      }
    }
  }

  private initializeAuthState(): void {
    const hasToken = this.hasToken();
    const user = this.getCurrentUserFromStorage();
    
    this.isAuthenticatedSubject.next(hasToken);
    this.currentUserSubject.next(user);
    
    // Check token validity on service initialization
    this.checkTokenValidity();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials)
      .pipe(
        tap(response => {
          // Backend returns accessToken directly in response
          this.setToken(response.accessToken);
          const user: User = {
            username: response.username,
            roles: response.roles
          };
          this.setUser(user);
          this.isAuthenticatedSubject.next(true);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    this.removeToken();
    this.removeUser();
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
    
    // Ne pas rediriger automatiquement en mode développement
    if (environment.production) {
      this.router.navigate(['/auth/login']);
    }
  }

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user ? user.roles.includes(`ROLE_${role}`) : false;
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isAgent(): boolean {
    return this.hasRole('AGENT');
  }

  isFinance(): boolean {
    return this.hasRole('FINANCE');
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private setToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  private removeToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
  }

  private setUser(user: User): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }
  }

  private removeUser(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.USER_KEY);
    }
  }

  private getCurrentUserFromStorage(): User | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    const userStr = localStorage.getItem(this.USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  }

  private checkTokenValidity(): void {
    const token = this.getToken();
    
    if (token) {
      // Skip token validation in development mode
      if (!environment.production) {
        return;
      }
      
      // Validate token with backend
      this.http.post(`${this.API_URL}/auth/validate`, { token })
        .subscribe({
          next: (response: any) => {
            if (!response.valid) {
              this.logout();
            }
          },
          error: (error) => {
            this.logout();
          }
        });
    }
  }
}
