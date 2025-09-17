import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { HomeComponent } from './home.component';
import { AuthService } from '../../core/services/auth.service';

// Interface pour les modules de test
interface TestModuleCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  requiredRole?: string;
}

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['hasRole', 'getCurrentUser', 'logout']);

    await TestBed.configureTestingModule({
      imports: [HomeComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to module when clicked', () => {
    component.navigateToModule('/test-route');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/test-route']);
  });

  it('should check module access correctly', () => {
    const moduleWithRole: TestModuleCard = { title: 'Test', description: 'Test', icon: 'test', route: '/test', color: 'blue', requiredRole: 'ADMIN' };
    const moduleWithoutRole: TestModuleCard = { title: 'Test', description: 'Test', icon: 'test', route: '/test', color: 'blue' };

    mockAuthService.hasRole.and.returnValue(true);
    expect(component.canAccessModule(moduleWithRole)).toBe(true);
    expect(component.canAccessModule(moduleWithoutRole)).toBe(true);

    mockAuthService.hasRole.and.returnValue(false);
    expect(component.canAccessModule(moduleWithRole)).toBe(false);
    expect(component.canAccessModule(moduleWithoutRole)).toBe(true);
  });

  it('should get current user', () => {
    const mockUser = { username: 'test', roles: ['ROLE_ADMIN'] };
    mockAuthService.getCurrentUser.and.returnValue(mockUser);
    
    expect(component.getCurrentUser()).toBe(mockUser);
    expect(mockAuthService.getCurrentUser).toHaveBeenCalled();
  });

  it('should logout and navigate to login', () => {
    component.logout();
    
    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
