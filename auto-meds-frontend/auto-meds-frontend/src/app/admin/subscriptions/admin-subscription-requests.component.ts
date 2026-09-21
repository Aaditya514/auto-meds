import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { MedicineService } from '../../core/services/medicine.service';
import { Subscription } from '../../core/models/subscription.model';
import { Medicine } from '../../core/models/medicine.model';

export interface AdminSubscriptionRequestItem extends Subscription {
  selectedMedicineIds: number[];
  searchTerm?: string;
  isPickerExpanded?: boolean;
  medicineDetails?: {
    [key: number]: {
      dosage: string;
      frequency: string;
      quantity: number;
    }
  };
}

@Component({
  selector: 'app-admin-subscription-requests',
  templateUrl: './admin-subscription-requests.component.html',
  styleUrls: ['./admin-subscription-requests.component.css']
})
export class AdminSubscriptionRequestsComponent implements OnInit {
  requests: AdminSubscriptionRequestItem[] = [];
  medicines: Medicine[] = [];
  loading = true;
  message = '';
  errorMessage = '';

  // General filter & view controls
  globalSearchQuery = '';
  viewMode: 'cards' | 'table' = 'cards';

  // Side-by-Side Audit Modal
  auditModalOpen = false;
  activeModalReq: AdminSubscriptionRequestItem | null = null;
  modalSearchTerm = '';

  constructor(
    private adminService: AdminService,
    private medicineService: MedicineService
  ) { }

  ngOnInit(): void {
    this.loadMedicines();
    this.loadRequests();
  }

  loadMedicines(): void {
    this.medicineService.getMedicines().subscribe({
      next: (data) => {
        this.medicines = data;
      }
    });
  }

  loadRequests(): void {
    this.loading = true;
    this.adminService.getPendingSubscriptionRequests().subscribe({
      next: (data) => {
        this.requests = data.map(sub => {
          const selectedMedicineIds = sub.medicineId ? [sub.medicineId] : [];
          const medicineDetails: any = {};
          selectedMedicineIds.forEach(mId => {
            medicineDetails[mId] = {
              dosage: sub.dosage || '1 tablet/day',
              frequency: sub.frequency || 'Once Daily',
              quantity: sub.quantity || 30
            };
          });
          return {
            ...sub,
            selectedMedicineIds,
            medicineDetails,
            searchTerm: '',
            isPickerExpanded: true
          };
        });
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  get filteredRequests(): AdminSubscriptionRequestItem[] {
    if (!this.globalSearchQuery || !this.globalSearchQuery.trim()) {
      return this.requests;
    }
    const q = this.globalSearchQuery.toLowerCase().trim();
    return this.requests.filter(req =>
      (req.patientName && req.patientName.toLowerCase().includes(q)) ||
      (req.patientEmail && req.patientEmail.toLowerCase().includes(q)) ||
      req.id.toString().includes(q) ||
      (req.dosage && req.dosage.toLowerCase().includes(q)) ||
      (req.frequency && req.frequency.toLowerCase().includes(q))
    );
  }

  getFilteredMedicines(req: AdminSubscriptionRequestItem, searchOverride?: string): Medicine[] {
    const term = (searchOverride !== undefined ? searchOverride : req.searchTerm || '').toLowerCase().trim();
    if (!term) {
      return this.medicines;
    }
    return this.medicines.filter(m =>
      m.medicineName.toLowerCase().includes(term) ||
      m.brandName.toLowerCase().includes(term) ||
      (m.composition && m.composition.toLowerCase().includes(term)) ||
      (m.strength && m.strength.toLowerCase().includes(term))
    );
  }

  isMedicineSelected(req: AdminSubscriptionRequestItem, medId: number): boolean {
    return req.selectedMedicineIds ? req.selectedMedicineIds.includes(medId) : false;
  }

  toggleMedicineSelection(req: AdminSubscriptionRequestItem, medId: number): void {
    if (!req.selectedMedicineIds) {
      req.selectedMedicineIds = [];
    }
    if (!req.medicineDetails) {
      req.medicineDetails = {};
    }
    const idx = req.selectedMedicineIds.indexOf(medId);
    if (idx > -1) {
      req.selectedMedicineIds.splice(idx, 1);
      delete req.medicineDetails[medId];
    } else {
      req.selectedMedicineIds.push(medId);
      req.medicineDetails[medId] = {
        dosage: req.dosage || '1 tablet/day',
        frequency: req.frequency || 'Once Daily',
        quantity: req.quantity || 30
      };
    }
  }

  removeMedicine(req: AdminSubscriptionRequestItem, medId: number): void {
    if (!req.selectedMedicineIds) return;
    const idx = req.selectedMedicineIds.indexOf(medId);
    if (idx > -1) {
      req.selectedMedicineIds.splice(idx, 1);
    }
    if (req.medicineDetails) {
      delete req.medicineDetails[medId];
    }
  }

  selectAllFiltered(req: AdminSubscriptionRequestItem, searchOverride?: string): void {
    const filtered = this.getFilteredMedicines(req, searchOverride);
    if (!req.selectedMedicineIds) {
      req.selectedMedicineIds = [];
    }
    if (!req.medicineDetails) {
      req.medicineDetails = {};
    }
    filtered.forEach(m => {
      if (!req.selectedMedicineIds.includes(m.id)) {
        req.selectedMedicineIds.push(m.id);
        req.medicineDetails![m.id] = {
          dosage: req.dosage || '1 tablet/day',
          frequency: req.frequency || 'Once Daily',
          quantity: req.quantity || 30
        };
      }
    });
  }

  clearAllSelected(req: AdminSubscriptionRequestItem): void {
    req.selectedMedicineIds = [];
    req.medicineDetails = {};
  }

  getMedicineById(medId: number): Medicine | undefined {
    return this.medicines.find(m => m.id === medId);
  }

  // Side-by-side Auditor Modal Controls
  openAuditModal(req: AdminSubscriptionRequestItem): void {
    this.activeModalReq = req;
    this.modalSearchTerm = '';
    this.auditModalOpen = true;
  }

  closeAuditModal(): void {
    this.auditModalOpen = false;
    this.activeModalReq = null;
  }

  // Action Dialog (Clarification & Rejection) State
  activeDialogType: 'CLARIFICATION' | 'REJECT' | null = null;
  activeDialogReqId: number | null = null;
  dialogInputText = '';
  dialogSubmitting = false;

  clarificationSuggestions = [
    'Please upload a clearer, un-blurred photo of your prescription document.',
    'Doctor signature, stamp, or medical registration number is missing on the prescription.',
    'The prescription uploaded is expired. Please upload a valid current prescription.',
    'The requested medication dosage does not match the attached prescription document.'
  ];

  rejectionSuggestions = [
    'Invalid or unreadable prescription document uploaded.',
    'Requested medication requires an active, unexpired prescription.',
    'Prescription document is missing doctor registration details.',
    'Medication requested is restricted or unavailable for auto-refill subscription.'
  ];

  openClarificationDialog(id: number): void {
    console.log('openClarificationDialog triggered for request ID:', id);
    this.activeDialogType = 'CLARIFICATION';
    this.activeDialogReqId = id;
    this.dialogInputText = '';
  }

  openRejectDialog(id: number): void {
    console.log('openRejectDialog triggered for request ID:', id);
    this.activeDialogType = 'REJECT';
    this.activeDialogReqId = id;
    this.dialogInputText = '';
  }

  closeActionDialog(): void {
    this.activeDialogType = null;
    this.activeDialogReqId = null;
    this.dialogInputText = '';
    this.dialogSubmitting = false;
  }

  useSuggestion(text: string): void {
    this.dialogInputText = text;
  }

  submitActionDialog(): void {
    if (!this.activeDialogReqId || !this.activeDialogType) return;
    if (!this.dialogInputText || !this.dialogInputText.trim()) {
      alert('Please enter a note/reason before submitting.');
      return;
    }

    this.dialogSubmitting = true;
    const reqId = this.activeDialogReqId;
    const note = this.dialogInputText.trim();

    if (this.activeDialogType === 'CLARIFICATION') {
      this.adminService.requestClarification(reqId, note).subscribe({
        next: () => {
          this.message = `Clarification request sent to patient for subscription request #${reqId}.`;
          this.dialogSubmitting = false;
          this.closeActionDialog();
          if (this.auditModalOpen && this.activeModalReq?.id === reqId) {
            this.closeAuditModal();
          }
          this.loadRequests();
        },
        error: (err) => {
          this.errorMessage = err.message || 'Failed to send clarification request.';
          this.dialogSubmitting = false;
        }
      });
    } else if (this.activeDialogType === 'REJECT') {
      this.adminService.rejectSubscription(reqId, note).subscribe({
        next: () => {
          this.message = `Subscription request #${reqId} has been REJECTED.`;
          this.dialogSubmitting = false;
          this.closeActionDialog();
          if (this.auditModalOpen && this.activeModalReq?.id === reqId) {
            this.closeAuditModal();
          }
          this.loadRequests();
        },
        error: (err) => {
          this.errorMessage = err.message || 'Failed to reject subscription request.';
          this.dialogSubmitting = false;
        }
      });
    }
  }

  approve(req: AdminSubscriptionRequestItem): void {
    this.message = '';
    this.errorMessage = '';

    if (!req.selectedMedicineIds || req.selectedMedicineIds.length === 0) {
      alert('Please select at least 1 medicine from the inventory catalog to assign to this prescription subscription before approving.');
      return;
    }

    const assignments = req.selectedMedicineIds.map(mId => {
      const details = req.medicineDetails ? req.medicineDetails[mId] : null;
      return {
        medicineId: mId,
        dosage: details ? details.dosage : '1 tablet/day',
        frequency: details ? details.frequency : 'Once Daily',
        quantity: details ? details.quantity : 30
      };
    });

    this.adminService.approveSubscription(req.id, assignments).subscribe({
      next: (res) => {
        this.message = `Successfully APPROVED request #${req.id} and assigned ${req.selectedMedicineIds.length} medicine(s).`;
        if (this.auditModalOpen && this.activeModalReq?.id === req.id) {
          this.closeAuditModal();
        }
        this.loadRequests();
      },
      error: (err) => this.errorMessage = err.message || 'Failed to approve subscription.'
    });
  }

  reject(id: number): void {
    this.openRejectDialog(id);
  }

  requestClarification(id: number): void {
    this.openClarificationDialog(id);
  }

  viewPrescription(prescriptionId?: number): void {
    if (!prescriptionId) {
      alert('No prescription file associated with this request.');
      return;
    }
    this.adminService.downloadPrescription(prescriptionId).subscribe({
      next: (blob: Blob) => {
        const fileUrl = URL.createObjectURL(blob);
        window.open(fileUrl, '_blank');
      },
      error: (err) => {
        console.error('Error opening prescription file:', err);
        alert('Could not open prescription file. The file may be missing or inaccessible.');
      }
    });
  }
}

