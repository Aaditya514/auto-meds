import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  user: User | null = null;
  loading = true;
  saving = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadProfile();
  }

  private initForm(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      email: [{ value: '', disabled: true }],
      phone: ['', [Validators.maxLength(20)]],
      address: ['', [Validators.maxLength(255)]],
      city: ['', [Validators.maxLength(100)]],
      state: ['', [Validators.maxLength(100)]],
      pincode: ['', [Validators.maxLength(20)]],
      allergies: ['', [Validators.maxLength(255)]],
      chronicConditions: ['', [Validators.maxLength(255)]]
    });
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.getProfile().subscribe({
      next: (userData) => {
        this.user = userData;
        this.profileForm.patchValue({
          name: userData.name || '',
          email: userData.email || '',
          phone: userData.phone || '',
          address: userData.address || '',
          city: userData.city || '',
          state: userData.state || '',
          pincode: userData.pincode || '',
          allergies: userData.allergies || '',
          chronicConditions: userData.chronicConditions || ''
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load user profile.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload = {
      name: this.profileForm.get('name')?.value,
      phone: this.profileForm.get('phone')?.value,
      address: this.profileForm.get('address')?.value,
      city: this.profileForm.get('city')?.value,
      state: this.profileForm.get('state')?.value,
      pincode: this.profileForm.get('pincode')?.value,
      allergies: this.profileForm.get('allergies')?.value,
      chronicConditions: this.profileForm.get('chronicConditions')?.value
    };

    this.authService.updateProfile(payload).subscribe({
      next: (updated) => {
        this.user = updated;
        this.saving = false;
        this.successMessage = 'Profile updated successfully!';
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to update profile.';
        this.saving = false;
      }
    });
  }
}
