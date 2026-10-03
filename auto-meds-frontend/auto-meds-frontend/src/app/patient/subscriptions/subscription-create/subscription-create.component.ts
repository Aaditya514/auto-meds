import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { PrescriptionOcrResult } from '../../../core/models/prescription-ocr.model';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-subscription-create',
  templateUrl: './subscription-create.component.html',
  styleUrls: ['./subscription-create.component.css']
})
export class SubscriptionCreateComponent implements OnInit {
  subscriptionForm!: FormGroup;
  selectedFile: File | null = null;
  filePreviewUrl: SafeUrl | null = null;
  fileError = '';
  submitting = false;
  scanningOcr = false;
  ocrResult: PrescriptionOcrResult | null = null;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private subscriptionService: SubscriptionService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.subscriptionForm = this.formBuilder.group({
      doctorVisitDate: ['', Validators.required]
    });
  }

  onFileSelected(event: any): void {
    this.fileError = '';
    this.ocrResult = null;
    const file: File = event.target.files[0];
    if (file) {
      const allowedExt = ['pdf', 'jpg', 'jpeg', 'png'];
      const ext = file.name.split('.').pop()?.toLowerCase();
      if (!ext || !allowedExt.includes(ext)) {
        this.fileError = 'Invalid file type. Please upload a PDF, JPG, JPEG, or PNG file.';
        this.selectedFile = null;
        this.filePreviewUrl = null;
        return;
      }
      this.selectedFile = file;

      // Create local preview URL
      const objectUrl = URL.createObjectURL(file);
      this.filePreviewUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);

      // Trigger automatic AI Optical Scan
      this.runOcrScan(file);
    }
  }

  isImage(): boolean {
    return this.selectedFile ? this.selectedFile.type.startsWith('image/') || /\.(jpg|jpeg|png|webp)$/i.test(this.selectedFile.name) : false;
  }

  getFileSize(): string {
    if (!this.selectedFile) return '';
    const bytes = this.selectedFile.size;
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  clearSelectedFile(): void {
    this.selectedFile = null;
    this.filePreviewUrl = null;
    this.ocrResult = null;
    this.fileError = '';
  }

  runOcrScan(file: File): void {
    this.scanningOcr = true;
    this.subscriptionService.scanPrescription(file).subscribe({
      next: (res) => {
        this.scanningOcr = false;
        this.ocrResult = res;

        // Auto-populate doctor visit date if parsed from document
        if (res.prescriptionDate && !this.subscriptionForm.get('doctorVisitDate')?.value) {
          try {
            const parsed = new Date(res.prescriptionDate);
            if (!isNaN(parsed.getTime())) {
              const formatted = parsed.toISOString().split('T')[0];
              this.subscriptionForm.patchValue({ doctorVisitDate: formatted });
            }
          } catch (e) {
            // Keep default
          }
        }
      },
      error: () => {
        this.scanningOcr = false;
        // Non-blocking fallback: user can still submit document normally
      }
    });
  }

  onSubmit(): void {
    if (this.submitting) return; // Design Motion: Guard against double-tap

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
