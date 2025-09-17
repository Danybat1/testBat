import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

interface User {
  id?: number;
  username: string;
  email: string;
  roles: string[];
  active: boolean;
}

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  userForm: FormGroup;
  isEditMode = false;
  userId?: number;
  loading = false;
  submitting = false;
  
  availableRoles = ['ADMIN', 'AGENT', 'FINANCE'];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.userForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.userId = +params['id'];
        this.loadUser();
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      roles: [[], [Validators.required]],
      active: [true]
    });
  }

  private loadUser(): void {
    if (!this.userId) return;
    
    this.loading = true;
    // Simulate API call
    setTimeout(() => {
      const mockUser: User = {
        id: this.userId,
        username: 'agent1',
        email: 'agent1@freightops.com',
        roles: ['AGENT'],
        active: true
      };
      this.userForm.patchValue(mockUser);
      this.loading = false;
    }, 1000);
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.submitting = true;
      const userData: User = this.userForm.value;

      // Simulate API call
      setTimeout(() => {
        this.submitting = false;
        this.router.navigate(['/users']);
      }, 1000);
    }
  }

  onCancel(): void {
    this.router.navigate(['/users']);
  }

  onRoleChange(role: string, checked: boolean): void {
    const roles = this.userForm.get('roles')?.value || [];
    if (checked) {
      if (!roles.includes(role)) {
        roles.push(role);
      }
    } else {
      const index = roles.indexOf(role);
      if (index > -1) {
        roles.splice(index, 1);
      }
    }
    this.userForm.patchValue({ roles });
  }

  isRoleSelected(role: string): boolean {
    const roles = this.userForm.get('roles')?.value || [];
    return roles.includes(role);
  }

  getFieldError(fieldName: string): string {
    const field = this.userForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `Le champ ${fieldName} est requis`;
      }
      if (field.errors['minlength']) {
        return `Le champ doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
      }
      if (field.errors['email']) {
        return 'Format d\'email invalide';
      }
    }
    return '';
  }
}
