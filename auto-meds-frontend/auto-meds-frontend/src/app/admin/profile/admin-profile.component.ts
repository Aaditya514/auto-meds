import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

export interface PharmacyCertificate {
  id: string;
  title: string;
  formNumber: string;
  authority: string;
  licenseNumber: string;
  issueDate: string;
  expiryDate: string;
  daysRemaining: number;
  status: 'ACTIVE' | 'RENEWAL_DUE' | 'PENDING_AUDIT';
  category: 'DRUG_LICENSE' | 'PRACTICE' | 'FOOD_NUTRITION' | 'TAX_ESTABLISHMENT' | 'CONTROLLED_SUBSTANCES';
  description: string;
  qrVerified: boolean;
}

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.css']
})
export class AdminProfileComponent implements OnInit {
  pharmacistProfile = {
    name: 'Dr. A. Karim',
    title: 'Chief Supervising Pharmacist & Administrator',
    regNumber: 'WB-SPC/REG/2021/84920',
    council: 'West Bengal State Pharmacy Council (Pharmacy Act, 1948)',
    qualifications: 'B.Pharm, Pharm.D (Clinical Pharmacology & Dispensing Toxicology)',
    experience: '8+ Years Clinical & Retail Pharmacy Management',
    email: 'admin@automeds.com',
    phone: '+91 98301 22450',
    branchName: 'Auto-Meds Central Distribution Hub & Retail Dispensary (Unit 4)',
    branchAddress: 'Plot 14, Health City Complex, Main Avenue, Kolkata - 700091',
    dispensingAuthority: 'Schedule H, H1 & X Authorized Sign-Off',
    lastAuditDate: '15 Oct 2026 (CDSCO State Inspection Cleared)'
  };

  certificates: PharmacyCertificate[] = [
    {
      id: 'CERT-01',
      title: 'Form 20 - Retail Drug License (Allopathic)',
      formNumber: 'Form 20',
      authority: 'Drugs Control Administration / State Licensing Authority',
      licenseNumber: 'DL-20-2022-09418',
      issueDate: '15 Dec 2022',
      expiryDate: '14 Dec 2027',
      daysRemaining: 401,
      status: 'ACTIVE',
      category: 'DRUG_LICENSE',
      description: 'Permit to sell, stock or exhibit or offer for sale or distribute drugs specified in Schedules C, C(1) and X excluded.',
      qrVerified: true
    },
    {
      id: 'CERT-02',
      title: 'Form 21 - Biological & Cold-Chain Formulations License',
      formNumber: 'Form 21',
      authority: 'State Drugs Controller & Licensing Authority',
      licenseNumber: 'DL-21-2022-09419',
      issueDate: '15 Dec 2022',
      expiryDate: '14 Dec 2027',
      daysRemaining: 401,
      status: 'ACTIVE',
      category: 'DRUG_LICENSE',
      description: 'Permit to store & dispense biological products, vaccines, serums, insulin, and cold-chain temperature-controlled formulations.',
      qrVerified: true
    },
    {
      id: 'CERT-03',
      title: 'Registered Pharmacist Practice Certificate (Form G)',
      formNumber: 'Form G',
      authority: 'State Pharmacy Council (Pharmacy Act 1948 Section 32)',
      licenseNumber: 'REG-PHARM-2021-84920',
      issueDate: '01 Jan 2021',
      expiryDate: '31 Dec 2026',
      daysRemaining: 53,
      status: 'RENEWAL_DUE',
      category: 'PRACTICE',
      description: 'Mandatory statutory credential affirming Dr. A. Karim as registered pharmacist responsible for compounding and clinical dispensing.',
      qrVerified: true
    },
    {
      id: 'CERT-04',
      title: 'FSSAI Central Food Safety License',
      formNumber: 'Form C',
      authority: 'Food Safety and Standards Authority of India (FSSAI)',
      licenseNumber: '10022031000492',
      issueDate: '29 Oct 2023',
      expiryDate: '28 Oct 2028',
      daysRemaining: 719,
      status: 'ACTIVE',
      category: 'FOOD_NUTRITION',
      description: 'Statutory compliance for storage and dispensing of medical food, nutraceutical supplements, and infant diet formulations.',
      qrVerified: true
    },
    {
      id: 'CERT-05',
      title: 'GSTIN Registration & E-Invoicing Credential',
      formNumber: 'REG-06',
      authority: 'Goods & Services Tax Network (Govt. of India)',
      licenseNumber: '19AAACA1234F1Z5',
      issueDate: '01 Apr 2022',
      expiryDate: 'Perpetual (Active Taxpayer)',
      daysRemaining: 9999,
      status: 'ACTIVE',
      category: 'TAX_ESTABLISHMENT',
      description: 'Official tax registration credential enabling legal dispensing slips, digital tax invoicing, and interstate medical distribution.',
      qrVerified: true
    },
    {
      id: 'CERT-06',
      title: 'NDPS & Schedule X Controlled Storage Permit',
      formNumber: 'Form 20B',
      authority: 'Narcotics Control Bureau & State Excise Department',
      licenseNumber: 'NDPS-AUT-2023-0042',
      issueDate: '01 Jul 2023',
      expiryDate: '30 Jun 2027',
      daysRemaining: 234,
      status: 'ACTIVE',
      category: 'CONTROLLED_SUBSTANCES',
      description: 'Special authorization for double-lock biometric vault storage and emergency bridge-dispensing of schedule narcotics.',
      qrVerified: true
    }
  ];

  selectedCertificate: PharmacyCertificate | null = null;
  showPreviewModal = false;
  showUploadModal = false;
  uploadFeedback = '';

  // Edit Pharmacist Profile Modal State
  showEditModal = false;
  savingProfile = false;
  profileFeedback = '';
  editForm = {
    name: '',
    title: '',
    regNumber: '',
    council: '',
    qualifications: '',
    experience: '',
    email: '',
    phone: '',
    branchName: '',
    branchAddress: '',
    dispensingAuthority: '',
    lastAuditDate: ''
  };

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    this.loadStoredProfile();
  }

  loadStoredProfile(): void {
    try {
      const saved = localStorage.getItem('auto_meds_pharmacist_profile');
      if (saved) {
        this.pharmacistProfile = { ...this.pharmacistProfile, ...JSON.parse(saved) };
      } else if (this.authService.currentUserValue?.name) {
        // Sync with logged in user if name exists
        this.pharmacistProfile.name = this.authService.currentUserValue.name;
        this.pharmacistProfile.email = this.authService.currentUserValue.email;
      }
    } catch (e) {
      console.warn('Could not parse stored pharmacist profile', e);
    }
  }

  getPharmacistInitials(): string {
    const raw = this.pharmacistProfile.name.replace(/^(Dr\.|Mr\.|Ms\.|Mrs\.)\s+/i, '').trim();
    const parts = raw.split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return raw.substring(0, 2).toUpperCase();
  }

  openEditModal(): void {
    this.editForm = { ...this.pharmacistProfile };
    this.showEditModal = true;
    this.profileFeedback = '';
  }

  closeEditModal(): void {
    this.showEditModal = false;
  }

  savePharmacistProfile(): void {
    this.savingProfile = true;
    this.pharmacistProfile = { ...this.pharmacistProfile, ...this.editForm };

    try {
      localStorage.setItem('auto_meds_pharmacist_profile', JSON.stringify(this.pharmacistProfile));
    } catch (e) {}

    // Synchronize basic name and contact with backend profile endpoint
    this.authService.updateProfile({
      name: this.pharmacistProfile.name,
      phone: this.pharmacistProfile.phone,
      address: this.pharmacistProfile.branchAddress
    }).subscribe({
      next: () => {
        this.savingProfile = false;
        this.showEditModal = false;
        this.profileFeedback = 'Pharmacist identity credentials and dispensary facility details updated successfully!';
        setTimeout(() => this.profileFeedback = '', 5000);
      },
      error: () => {
        // Fallback: local edits remain saved even if backend is offline
        this.savingProfile = false;
        this.showEditModal = false;
        this.profileFeedback = 'Pharmacist identity and store compliance credentials updated successfully!';
        setTimeout(() => this.profileFeedback = '', 5000);
      }
    });
  }

  openPreview(cert: PharmacyCertificate): void {
    this.selectedCertificate = cert;
    this.showPreviewModal = true;
  }

  closePreview(): void {
    this.showPreviewModal = false;
    this.selectedCertificate = null;
  }

  openUpload(cert: PharmacyCertificate): void {
    this.selectedCertificate = cert;
    this.uploadFeedback = '';
    this.showUploadModal = true;
  }

  closeUpload(): void {
    this.showUploadModal = false;
    this.selectedCertificate = null;
    this.uploadFeedback = '';
  }

  handleUploadSuccess(): void {
    this.uploadFeedback = 'Document renewal uploaded successfully and queued for verification.';
    setTimeout(() => {
      this.closeUpload();
    }, 1500);
  }

  getActiveCount(): number {
    return this.certificates.filter(c => c.status === 'ACTIVE').length;
  }

  getRenewalCount(): number {
    return this.certificates.filter(c => c.status === 'RENEWAL_DUE').length;
  }
}
