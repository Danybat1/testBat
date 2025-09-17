import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface User {
  id?: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  isActive: boolean;
  lastLoginAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/api/users`;

  // Mock data for development
  private mockUsers: User[] = [
    { 
      id: 1, 
      username: 'admin', 
      email: 'admin@freightops.com', 
      firstName: 'Admin',
      lastName: 'User',
      roles: ['ADMIN'], 
      isActive: true,
      lastLoginAt: new Date().toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    { 
      id: 2, 
      username: 'agent1', 
      email: 'agent1@freightops.com', 
      firstName: 'Agent',
      lastName: 'One',
      roles: ['AGENT'], 
      isActive: true,
      lastLoginAt: new Date(Date.now() - 86400000).toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    { 
      id: 3, 
      username: 'finance', 
      email: 'finance@freightops.com', 
      firstName: 'Finance',
      lastName: 'User',
      roles: ['FINANCE'], 
      isActive: true,
      lastLoginAt: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Get all users
   */
  getAllUsers(): Observable<User[]> {
    // Simulate API call
    return of([...this.mockUsers]).pipe(delay(500));
  }

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<User> {
    const user = this.mockUsers.find(u => u.id === id);
    if (user) {
      return of(user).pipe(delay(300));
    }
    throw new Error('User not found');
  }

  /**
   * Create new user
   */
  createUser(user: Omit<User, 'id'>): Observable<User> {
    const newUser: User = {
      ...user,
      id: Math.max(...this.mockUsers.map(u => u.id || 0)) + 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.mockUsers.push(newUser);
    return of(newUser).pipe(delay(500));
  }

  /**
   * Update user
   */
  updateUser(id: number, user: Partial<User>): Observable<User> {
    const index = this.mockUsers.findIndex(u => u.id === id);
    if (index !== -1) {
      this.mockUsers[index] = {
        ...this.mockUsers[index],
        ...user,
        updatedAt: new Date().toISOString()
      };
      return of(this.mockUsers[index]).pipe(delay(500));
    }
    throw new Error('User not found');
  }

  /**
   * Delete user
   */
  deleteUser(id: number): Observable<void> {
    const index = this.mockUsers.findIndex(u => u.id === id);
    if (index !== -1) {
      this.mockUsers.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }
    throw new Error('User not found');
  }
} 