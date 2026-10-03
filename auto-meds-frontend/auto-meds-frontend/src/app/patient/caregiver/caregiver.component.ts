import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CaregiverService,
  CaregiverLinkResponse,
  DelegatedPatientResponse,
  PendingInvitationResponse,
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

  // ── Pending Invitations (caregiver inbox) ─────────────────────────────────
  pendingInvitations: PendingInvitationResponse[] = [];
  loadingPending = false;

  // ── Patient Orders (for proxy payment) ─────────────────────────────────────
  patientOrders: { [patientId: number]: any[] } = {};
  loadingOrders: { [patientId: number]: boolean } = {};
  expandedPatientId: number | null = null;
  payingOrderId: number | null = null;

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

  // ── Feedback & Actions ────────────────────────────────────────────────────
  actionMessage = '';
  actionError = '';
  processingId: number | null = null;
  copiedLinkId: number | null = null;

  constructor(
    private caregiverService: CaregiverService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCaregivers();
    this.loadDelegatedPatients();
    this.loadPendingInvitations();

    // Check if user arrived via an /accept/:id invitation link
    const acceptId = this.route.snapshot.paramMap.get('id');
    if (acceptId) {
      this.acceptPendingInvitation(Number(acceptId), true);
    }
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

  loadPendingInvitations(): void {
    this.loadingPending = true;
    this.caregiverService.getPendingInvitations().subscribe({
      next: (list: PendingInvitationResponse[]) => {
        this.pendingInvitations = list;
        this.loadingPending = false;
        // If caregiver has pending invites but no caregivers of their own, switch tab to patients
        if (this.pendingInvitations.length > 0 && this.caregivers.length === 0) {
          this.activeTab = 'my-patients';
        }
      },
      error: () => { this.loadingPending = false; }
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
        this.inviteSuccess = `✅ Invitation sent to ${response.caregiverEmail}! They can now accept it in their AutoMeds account.`;
        this.inviting = false;
        this.showInviteForm = false;
        this.inviteForm = { caregiverEmail: '', relationshipLabel: '', permissions: 'NOTIFICATIONS,PAY_ON_BEHALF', notifyPhone: '' };
        this.loadCaregivers();
      },
      error: (err: Error | { error?: { detail?: string } }) => {
        const msg = (err as Error).message || (err as any)?.error?.detail;
        this.inviteError = msg || 'Failed to send invitation. Please try again.';
        this.inviting = false;
      }
    });
  }

  // ── Revoke / Cancel ───────────────────────────────────────────────────────

  revokeCaregiver(accessId: number, caregiverName: string): void {
    if (!confirm(`Cancel/Revoke caregiver access for ${caregiverName}?`)) {
      return;
    }
    this.processingId = accessId;
    this.caregiverService.revokeCaregiver(accessId).subscribe({
      next: () => {
        this.actionMessage = `✅ Caregiver link for ${caregiverName} has been revoked.`;
        this.processingId = null;
        this.loadCaregivers();
      },
      error: (err: Error | { error?: { detail?: string } }) => {
        this.actionError = (err as Error).message || (err as any)?.error?.detail || 'Failed to revoke access.';
        this.processingId = null;
      }
    });
  }

  // ── Pending Invitation Actions (Caregiver Inbound) ────────────────────────

  acceptPendingInvitation(accessId: number, fromUrl = false): void {
    this.processingId = accessId;
    this.caregiverService.respondToInvitation({ accessId, action: 'ACCEPT' }).subscribe({
      next: () => {
        this.actionMessage = '🎉 Congratulations! You have accepted the caregiver invitation. You can now manage medications for this patient.';
        this.processingId = null;
        this.loadPendingInvitations();
        this.loadDelegatedPatients();
        this.activeTab = 'my-patients';

        if (fromUrl) {
          this.router.navigate(['/caregiver'], { replaceUrl: true });
        }
      },
      error: (err: Error | { error?: { detail?: string } }) => {
        this.actionError = (err as Error).message || (err as any)?.error?.detail || 'Failed to accept invitation.';
        this.processingId = null;
        if (fromUrl) {
          this.router.navigate(['/caregiver'], { replaceUrl: true });
        }
      }
    });
  }

  declinePendingInvitation(accessId: number): void {
    if (!confirm('Decline this caregiver invitation?')) {
      return;
    }
    this.processingId = accessId;
    this.caregiverService.respondToInvitation({ accessId, action: 'DECLINE' }).subscribe({
      next: () => {
        this.actionMessage = 'Caregiver invitation declined.';
        this.processingId = null;
        this.loadPendingInvitations();
      },
      error: (err: Error | { error?: { detail?: string } }) => {
        this.actionError = (err as Error).message || (err as any)?.error?.detail || 'Failed to decline invitation.';
        this.processingId = null;
      }
    });
  }

  // ── Copy Direct Accept Link ───────────────────────────────────────────────

  copyInviteLink(accessId: number): void {
    const url = `${window.location.origin}/caregiver/accept/${accessId}`;
    if (navigator?.clipboard?.writeText) {
      navigator.clipboard.writeText(url).then(() => {
        this.copiedLinkId = accessId;
        this.actionMessage = `📋 Direct invite link copied to clipboard! The caregiver can open this link in their browser to accept instantly.`;
        setTimeout(() => {
          if (this.copiedLinkId === accessId) {
            this.copiedLinkId = null;
          }
        }, 3500);
      });
    } else {
      prompt('Copy this invite link for your caregiver:', url);
    }
  }

  // ── Delegated Patient Orders & Proxy Payment ──────────────────────────────

  togglePatientOrders(patientId: number): void {
    if (this.expandedPatientId === patientId) {
      this.expandedPatientId = null;
      return;
    }
    this.expandedPatientId = patientId;
    this.loadPatientOrders(patientId);
  }

  loadPatientOrders(patientId: number): void {
    this.loadingOrders[patientId] = true;
    this.caregiverService.getPatientPendingOrders(patientId).subscribe({
      next: (orders: any[]) => {
        this.patientOrders[patientId] = orders;
        this.loadingOrders[patientId] = false;
      },
      error: () => {
        this.loadingOrders[patientId] = false;
      }
    });
  }

  payOrderOnBehalf(patientId: number, orderId: number): void {
    this.payingOrderId = orderId;
    this.caregiverService.payOnBehalf({
      patientId,
      orderId,
      paymentMethod: 'UPI',
      paymentReference: 'UPI-PROXY-' + Date.now()
    }).subscribe({
      next: (res) => {
        this.actionMessage = `✅ ${res.message || 'Payment completed successfully!'}`;
        this.payingOrderId = null;
        this.loadPatientOrders(patientId);
      },
      error: (err: Error | { error?: { detail?: string } }) => {
        this.actionError = (err as Error).message || (err as any)?.error?.detail || 'Payment failed.';
        this.payingOrderId = null;
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

  getInitial(name?: string, fallback = '?'): string {
    return name && name.length > 0 ? name.charAt(0).toUpperCase() : fallback;
  }

  hasOrders(patientId: number): boolean {
    const list = this.patientOrders[patientId];
    return Array.isArray(list) && list.length > 0;
  }

  getOrderList(patientId: number): any[] {
    return this.patientOrders[patientId] || [];
  }
}
