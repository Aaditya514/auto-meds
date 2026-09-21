import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SubscriptionService } from '../../../core/services/subscription.service';

@Component({
  selector: 'app-subscription-create',
  templateUrl: './subscription-create.component.html',
  styleUrls: ['./subscription-create.component.css']
})
export class SubscriptionCreateComponent implements OnInit {
  subscriptionForm!: FormGroup;
  selectedFile: File | null = null;
  fileError = '';
  submitting = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private subscriptionService: SubscriptionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subscriptionForm = this.formBuilder.group({
      doctorVisitDate: ['', Validators.required]
    });
  }

  onFileSelected(event: any): void {
    this.fileError = '';
    const file: File = event.target.files[0];
    if (file) {
      const allowedExt = ['pdf', 'jpg', 'jpeg', 'png'];
      const ext = file.name.split('.').pop()?.toLowerCase();
      if (!ext || !allowedExt.includes(ext)) {
        this.fileError = 'Invalid file type. Please upload a PDF, JPG, JPEG, or PNG file.';
        this.selectedFile = null;
        return;
      }
      this.selectedFile = file;
    }
  }

  onSubmit(): void {
    if (this.subscriptionForm.invalid) {
      return;
    }

    if (!this.selectedFile) {
      this.fileError = 'Prescription file upload is required for subscription requests.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const val = this.subscriptionForm.value;

    this.subscriptionService.createSubscription(
      null, // Medicine will be reviewed and assigned by Pharmacist
      null,
      null,
      null,
      this.selectedFile,
      val.doctorVisitDate
    ).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/subscriptions']);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to submit subscription request.';
        this.submitting = false;
      }
    });
  }
}
