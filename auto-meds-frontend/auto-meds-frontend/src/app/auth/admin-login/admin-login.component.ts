import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-login',
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.css']
})
export class AdminLoginComponent implements OnInit {
  adminLoginForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  returnUrl = '/admin/dashboard';

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn() && this.authService.isAdmin()) {
      this.router.navigate(['/admin/dashboard']);
      return;
    }

    // Auto-populate credentials for Admin
    this.adminLoginForm = this.formBuilder.group({
      email: ['admin@automeds.com', [Validators.required, Validators.email]],
      password: ['admin123', Validators.required]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/admin/dashboard';
  }

  get f() { return this.adminLoginForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.adminLoginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.login(this.adminLoginForm.value).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.role === 'ADMIN') {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.authService.logout();
          this.error = 'Access denied. This portal is for administrators only.';
        }
      },
      error: (err) => {
        this.error = err.message || 'Admin login failed. Please check credentials.';
        this.loading = false;
      }
    });
  }
}
