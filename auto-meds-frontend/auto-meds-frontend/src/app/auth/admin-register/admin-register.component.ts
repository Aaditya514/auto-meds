import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-register',
  templateUrl: './admin-register.component.html',
  styleUrls: ['./admin-register.component.css']
})
export class AdminRegisterComponent implements OnInit {
  adminRegisterForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Only logged-in admins should reach this page
    if (!this.authService.isLoggedIn() || !this.authService.isAdmin()) {
      this.router.navigate(['/login/admin']);
    }

    this.adminRegisterForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z\\s]+$')]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      phone: ['', [Validators.pattern('^[0-9]{10}$')]],
      address: [''],
      city: [''],
      state: [''],
      pincode: ['', [Validators.pattern('^[0-9]{6}$')]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  get f() { return this.adminRegisterForm.controls; }

  passwordMatchValidator(g: FormGroup) {
    const password = g.get('password')?.value;
    const confirmPassword = g.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.adminRegisterForm.invalid) {
      return;
    }

    this.loading = true;
    const { confirmPassword, ...payload } = this.adminRegisterForm.value;

    this.authService.registerAdmin(payload).subscribe({
      next: () => {
        this.loading = false;
        this.success = `Admin account for "${payload.email}" created successfully!`;
        this.submitted = false;
        this.adminRegisterForm.reset();
      },
      error: (err) => {
        this.error = err.message || 'Failed to create admin account. Please try again.';
        this.loading = false;
      }
    });
  }
}
