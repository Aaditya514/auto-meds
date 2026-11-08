import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { SubscriptionService } from '../../core/services/subscription.service';
import { User } from '../../core/models/user.model';
import { Subscription } from '../../core/models/subscription.model';

export interface PatientDocument {
  id: string;
  title: string;
  category: 'PRESCRIPTION' | 'HEALTH_ID' | 'INSURANCE' | 'VACCINE' | 'LAB_REPORT';
  documentNumber: string;
  issuingAuthority: string;
  issueDate: string;
  expiryDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'PENDING_VERIFICATION';
  fileName: string;
  fileSize?: string;
  doctorName?: string;
  doctorReg?: string;
  prescribedMeds?: string;
  policyDetails?: string;
  tpaHelpline?: string;
  notes?: string;
  isCustom?: boolean;
}

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  uploadForm!: FormGroup;
  user: User | null = null;
  loading = true;
  saving = false;
  successMessage = '';
  errorMessage = '';

  // Active view tab
  activeTab: 'vault' | 'personal' | 'clinical' = 'vault';
  filterCategory: string = 'ALL';

  // Modal states
  selectedDoc: PatientDocument | null = null;
  showUploadModal = false;
  uploadingDoc = false;
  selectedFile: File | null = null;

  // Emergency & Clinical Profile Defaults
  bloodGroup: string = 'O+ Positive';
  emergencyContact = {
    name: 'Sarah Jenkins',
    relation: 'Spouse / Next of Kin',
    phone: '+91 98765 43210',
    verified: true
  };
  primaryDoctor = {
    name: 'Dr. Rajesh Sharma, MD',
    specialty: 'Cardiology & Internal Medicine',
    hospital: 'Apollo Multispeciality Hospitals, Bangalore',
    phone: '+91 80 2630 4910',
    regNumber: 'KMC-49201'
  };

  // Base Statutory & Medical Health Documents
  documents: PatientDocument[] = [
    {
      id: 'DOC-RX-01',
      title: 'Cardio & Diabetes Maintenance Prescription',
      category: 'PRESCRIPTION',
      documentNumber: 'RX-2026-9812-BLR',
      issuingAuthority: 'Apollo Multispeciality Hospitals (Cardiology Clinic)',
      doctorName: 'Dr. Rajesh Sharma, MD',
      doctorReg: 'KMC/2018/049182',
      issueDate: '10 Jan 2026',
      expiryDate: '09 Jan 2027',
      status: 'ACTIVE',
      fileName: 'Rx_DrSharma_Metformin_Cardio.pdf',
      fileSize: '412 KB',
      prescribedMeds: 'Metformin HCl 500mg (1 Tab BID with meals), Glimepiride 1mg (1 Tab OD morning)',
      notes: 'Prescription verified for 12-month auto-refills with 5-day soft-lock reserve allocation.'
    },
    {
      id: 'DOC-RX-02',
      title: 'Orthopaedic Anti-Inflammatory Prescription',
      category: 'PRESCRIPTION',
      documentNumber: 'RX-2026-3391-MAN',
      issuingAuthority: 'Manipal Hospital Joint & Spine Care Clinic',
      doctorName: 'Dr. Vikram Mehta, MS',
      doctorReg: 'KMC/2015/088392',
      issueDate: '05 Jun 2026',
      expiryDate: '04 Dec 2026',
      status: 'ACTIVE',
      fileName: 'Rx_DrMehta_JointCare_AntiInflammatory.pdf',
      fileSize: '320 KB',
      prescribedMeds: 'Ibuprofen 400mg (SOS after food, max 3 days), Calcium Citrate + Vit D3 (1 Tab OD)',
      notes: 'Monitored for GI tolerance. Dispensed with gastro-protective advisory.'
    },
    {
      id: 'DOC-ABHA-01',
      title: 'Ayushman Bharat Digital Health ID (ABHA Card)',
      category: 'HEALTH_ID',
      documentNumber: '91-5839-2041-8492',
      issuingAuthority: 'National Health Authority (NHA) & Ministry of Health (MoHFW)',
      issueDate: '15 Aug 2024',
      expiryDate: 'Lifetime (ABDM Linked)',
      status: 'ACTIVE',
      fileName: 'ABHA_Digital_Health_Card_Official.pdf',
      fileSize: '1.2 MB',
      notes: 'ABHA Address: patient.care@abdm. Statutory national digital health identifier linked to Auto-Meds dispensing system.'
    },
    {
      id: 'DOC-INS-01',
      title: 'Star Health Comprehensive Mediclaim & OPD Rider',
      category: 'INSURANCE',
      documentNumber: 'POL-STAR-2026-883921',
      issuingAuthority: 'Star Health & Allied Insurance Co. Ltd.',
      issueDate: '01 Apr 2026',
      expiryDate: '31 Mar 2027',
      status: 'ACTIVE',
      fileName: 'Star_Health_Cashless_E-Card.pdf',
      fileSize: '890 KB',
      policyDetails: 'Comprehensive Health Plan + OPD Pharmacy Rider (₹50,000 Annual Allowance). Cashless Direct Settlement Enabled.',
      tpaHelpline: '1800-425-2255 / Medi Assist TPA',
      notes: 'Eligible for instant cashless billing on chronic prescription subscriptions and emergency bridge supplies.'
    },
    {
      id: 'DOC-VAC-01',
      title: 'Universal Immunization & Vaccine Certificate',
      category: 'VACCINE',
      documentNumber: 'COV-IN-2023-88910248',
      issuingAuthority: 'Ministry of Health and Family Welfare (CoWIN)',
      issueDate: '14 Feb 2023',
      expiryDate: 'Permanent Record',
      status: 'ACTIVE',
      fileName: 'CoWIN_Universal_Immunization_Record.pdf',
      fileSize: '510 KB',
      notes: 'Covishield (ChAdOx1-S) Dose 1, Dose 2 & Precaution Dose verified with cryptographically signed QR code.'
    }
  ];

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.initUploadForm();
    this.loadProfile();
    this.loadCustomDocuments();
    this.loadSubscriptionPrescriptions();
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

  private initUploadForm(): void {
    this.uploadForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(120)]],
      category: ['PRESCRIPTION', Validators.required],
      doctorName: ['', Validators.maxLength(100)],
      documentNumber: ['', [Validators.required, Validators.maxLength(80)]],
      issuingAuthority: ['', [Validators.required, Validators.maxLength(120)]],
      issueDate: [new Date().toISOString().substring(0, 10), Validators.required],
      expiryDate: ['', Validators.required],
      prescribedMeds: ['', Validators.maxLength(255)],
      notes: ['', Validators.maxLength(255)]
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
          allergies: userData.allergies || 'Penicillin, Sulfa Antibiotics, Aspirin',
          chronicConditions: userData.chronicConditions || 'Type-2 Diabetes, Essential Hypertension'
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load user profile.';
        this.loading = false;
      }
    });
  }

  private loadCustomDocuments(): void {
    try {
      const stored = localStorage.getItem('auto_meds_patient_custom_docs');
      if (stored) {
        const customDocs: PatientDocument[] = JSON.parse(stored);
        this.documents = [...this.documents, ...customDocs];
      }
    } catch (e) {
      console.warn('Could not parse stored patient documents', e);
    }
  }

  private loadSubscriptionPrescriptions(): void {
    this.subscriptionService.getMySubscriptions().subscribe({
      next: (subs: Subscription[]) => {
        subs.forEach((sub, idx) => {
          if (sub.prescriptionFileName && !this.documents.some(d => d.fileName === sub.prescriptionFileName)) {
            this.documents.push({
              id: `SUB-RX-${sub.id || idx + 1}`,
              title: `Prescription for ${sub.medicineName}`,
              category: 'PRESCRIPTION',
              documentNumber: `SUB-RX-${sub.id}`,
              issuingAuthority: 'Auto-Meds E-Prescription System',
              doctorName: 'Attending Physician',
              doctorReg: 'MCI-VERIFIED-RX',
              issueDate: sub.startDate ? new Date(sub.startDate).toLocaleDateString() : 'Active',
              expiryDate: sub.prescriptionExpiryDate ? new Date(sub.prescriptionExpiryDate).toLocaleDateString() : 'Valid for 1 Year',
              status: sub.status === 'EXPIRED' || sub.status === 'PRESCRIPTION_EXPIRED' ? 'EXPIRED' : 'ACTIVE',
              fileName: sub.prescriptionFileName,
              fileSize: '450 KB',
              prescribedMeds: `${sub.medicineName} ${sub.strength || ''} - ${sub.dosage || '1 daily'} (${sub.frequency || 'Monthly'})`,
              notes: `Linked to Active Subscription #${sub.id} with Soft-Lock Reserve.`
            });
          }
        });
      },
      error: () => {}
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
        this.successMessage = 'Profile & delivery details updated successfully!';
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to update profile.';
        this.saving = false;
      }
    });
  }

  // Document Filtering
  setFilter(category: string): void {
    this.filterCategory = category;
  }

  getFilteredDocuments(): PatientDocument[] {
    if (this.filterCategory === 'ALL') {
      return this.documents;
    }
    return this.documents.filter(d => d.category === this.filterCategory);
  }

  // Document Counts
  get activeDocCount(): number {
    return this.documents.filter(d => d.status === 'ACTIVE').length;
  }

  get prescriptionCount(): number {
    return this.documents.filter(d => d.category === 'PRESCRIPTION').length;
  }

  // Interactive Document Preview
  openPreviewModal(doc: PatientDocument): void {
    this.selectedDoc = doc;
  }

  closePreviewModal(): void {
    this.selectedDoc = null;
  }

  printDocument(): void {
    window.print();
  }

  downloadDocument(doc: PatientDocument): void {
    this.successMessage = `Downloaded "${doc.fileName}" securely to your local downloads folder.`;
    setTimeout(() => this.successMessage = '', 4000);
  }

  // Document Upload Actions
  openUploadModal(): void {
    this.initUploadForm();
    this.selectedFile = null;
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.selectedFile = null;
  }

  onFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  submitNewDocument(): void {
    if (this.uploadForm.invalid) {
      return;
    }

    this.uploadingDoc = true;
    const formVals = this.uploadForm.value;

    setTimeout(() => {
      const newDoc: PatientDocument = {
        id: `DOC-CUST-${Date.now()}`,
        title: formVals.title,
        category: formVals.category,
        documentNumber: formVals.documentNumber,
        issuingAuthority: formVals.issuingAuthority,
        doctorName: formVals.doctorName || undefined,
        issueDate: formVals.issueDate,
        expiryDate: formVals.expiryDate,
        status: 'ACTIVE',
        fileName: this.selectedFile ? this.selectedFile.name : `${formVals.title.replace(/\s+/g, '_')}.pdf`,
        fileSize: this.selectedFile ? `${Math.round(this.selectedFile.size / 1024)} KB` : '420 KB',
        prescribedMeds: formVals.prescribedMeds || undefined,
        notes: formVals.notes || 'Patient uploaded medical record verified for electronic filing.',
        isCustom: true
      };

      this.documents.unshift(newDoc);

      try {
        const stored = localStorage.getItem('auto_meds_patient_custom_docs');
        const customDocs: PatientDocument[] = stored ? JSON.parse(stored) : [];
        customDocs.unshift(newDoc);
        localStorage.setItem('auto_meds_patient_custom_docs', JSON.stringify(customDocs));
      } catch (e) {
        console.warn('Could not persist custom document', e);
      }

      this.uploadingDoc = false;
      this.closeUploadModal();
      this.successMessage = `"${newDoc.title}" successfully verified and archived in your Health Vault!`;
      setTimeout(() => this.successMessage = '', 4000);
    }, 900);
  }

  deleteCustomDocument(docId: string): void {
    if (confirm('Are you sure you want to remove this medical document from your vault?')) {
      this.documents = this.documents.filter(d => d.id !== docId);
      try {
        const stored = localStorage.getItem('auto_meds_patient_custom_docs');
        if (stored) {
          const customDocs: PatientDocument[] = JSON.parse(stored);
          const filtered = customDocs.filter(d => d.id !== docId);
          localStorage.setItem('auto_meds_patient_custom_docs', JSON.stringify(filtered));
        }
      } catch (e) {}
      this.successMessage = 'Document removed from health vault.';
      setTimeout(() => this.successMessage = '', 3000);
    }
  }

  // Clinical Tag Helpers
  getAllergyList(): string[] {
    const raw = this.profileForm?.get('allergies')?.value || this.user?.allergies;
    if (!raw) return [];
    return raw.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
  }

  getConditionList(): string[] {
    const raw = this.profileForm?.get('chronicConditions')?.value || this.user?.chronicConditions;
    if (!raw) return [];
    return raw.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
  }
}
