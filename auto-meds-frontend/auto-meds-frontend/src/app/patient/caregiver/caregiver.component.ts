import { Component, OnInit } from '@angular/core';
import {
  CaregiverService,
  CaregiverLinkResponse,
  DelegatedPatientResponse,
  CaregiverInviteRequest
} from '../../core/services/caregiver.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-caregiver',
  templateUrl: './caregiver.component.html',
  styleUrls: ['./caregiver.component.css']
})
export class CaregiverComponent implements OnInit {

  // ── Tab State ─────────────────────────────────────────────────────────────
  activeTab: 'my-caregivers' | 'my-patients' = 'my-caregivers';

  // ── Caregiver List (patient view) ─────────────────────────────────────────
  caregivers: CaregiverLinkResponse[] = [];
  loadingCaregivers = false;

  // ── Delegated Patients (caregiver view) ───────────────────────────────────
  delegatedPatients: DelegatedPatientResponse[] = [];
  loadingPatients = false;

  // ── Invite Form ───────────────────────────────────────────────────────────
  showInviteForm = false;
  inviteForm: CaregiverInviteRequest = {
    caregiverEmail: '',
    relationshipLabel: '',
    permissions: 'NOTIFICATIONS,PAY_ON_BEHALF',
    notifyPhone: ''
  };
  inviting = false;
  inviteSuccess = '';
  inviteError = '';

  // ── Feedback ──────────────────────────────────────────────────────────────
  actionMessage = '';
  actionError = '';
  processingId: number | null = null;

  constructor(
    private caregiverService: CaregiverService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCaregivers();
    this.loadDelegatedPatients();
  }

  // ── Data Loading ──────────────────────────────────────────────────────────

  loadCaregivers(): void {
    this.loadingCaregivers = true;
    this.caregiverService.getMyCaregivers().subscribe({
      next: (list: CaregiverLinkResponse[]) => {
        this.caregivers = list;
        this.loadingCaregivers = false;
      },
      error: () => { this.loadingCaregivers = false; }
    });
  }

  loadDelegatedPatients(): void {
    this.loadingPatients = true;
    this.caregiverService.getMyPatients().subscribe({
      next: (list: DelegatedPatientResponse[]) => {
        this.delegatedPatients = list;
        this.loadingPatients = false;
      },
      error: () => { this.loadingPatients = false; }
    });
  }

  // ── Invite ────────────────────────────────────────────────────────────────

  toggleInviteForm(): void {
    this.showInviteForm = !this.showInviteForm;
    this.inviteSuccess = '';
    this.inviteError = '';
  }

  submitInvite(): void {
    if (!this.inviteForm.caregiverEmail || !this.inviteForm.relationshipLabel) {
      this.inviteError = 'Email and relationship label are required.';
      return;
    }

    this.inviting = true;
    this.inviteError = '';
    this.inviteSuccess = '';

    this.caregiverService.inviteCaregiver(this.inviteForm).subscribe({
      next: (response: CaregiverLinkResponse) => {
        this.inviteSuccess = `✅ Invitation sent to ${response.caregiverEmail}! They will receive an email shortly.`;
        this.inviting = false;
        this.showInviteForm = false;
        this.inviteForm = { caregiverEmail: '', relationshipLabel: '', permissions: 'NOTIFICATIONS,PAY_ON_BEHALF', notifyPhone: '' };
        this.loadCaregivers();
      },
      error: (err: { error?: { detail?: string } }) => {
        this.inviteError = err?.error?.detail || 'Failed to send invitation. Please try again.';
        this.inviting = false;
      }
    });
  }

  // ── Revoke ────────────────────────────────────────────────────────────────

  revokeCaregiver(accessId: number, caregiverName: string): void {
    if (!confirm(`Remove ${caregiverName} as your caregiver? They will lose all access immediately.`)) {
      return;
    }
    this.processingId = accessId;
    this.caregiverService.revokeCaregiver(accessId).subscribe({
      next: () => {
        this.actionMessage = `✅ ${caregiverName}'s access has been revoked.`;
        this.processingId = null;
        this.loadCaregivers();
      },
      error: (err: { error?: { detail?: string } }) => {
        this.actionError = err?.error?.detail || 'Failed to revoke access.';
        this.processingId = null;
      }
    });
  }

  // ── Accept (from pending invitation as a caregiver) ───────────────────────

  acceptInvitation(accessId: number): void {
    this.processingId = accessId;
    this.caregiverService.respondToInvitation({ accessId, action: 'ACCEPT' }).subscribe({
      next: (r: CaregiverLinkResponse) => {
        this.actionMessage = `✅ You are now a caregiver for ${r.caregiverName || 'the patient'}!`;
        this.processingId = null;
        this.loadCaregivers();
        this.loadDelegatedPatients();
      },
      error: (err: { error?: { detail?: string } }) => {
        this.actionError = err?.error?.detail || 'Failed to accept invitation.';
        this.processingId = null;
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':   return 'status-active';
      case 'PENDING':  return 'status-pending';
      case 'REVOKED':  return 'status-revoked';
      default:         return '';
    }
  }

  getPermissionBadges(permissions: string): string[] {
    if (!permissions) return [];
    return permissions.split(',').map(p => p.trim());
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  dismissMessages(): void {
    this.actionMessage = '';
    this.actionError = '';
    this.inviteSuccess = '';
    this.inviteError = '';
  }
}
