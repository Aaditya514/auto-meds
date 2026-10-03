import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CaregiverInviteRequest {
  caregiverEmail: string;
  relationshipLabel: string;
  permissions: string;
  notifyPhone?: string;
}

export interface RespondRequest {
  accessId: number;
  action: 'ACCEPT' | 'DECLINE';
}

export interface ProxyPaymentRequest {
  patientId: number;
  orderId: number;
  paymentMethod: string;
  paymentReference?: string;
}

export interface CaregiverLinkResponse {
  accessId: number;
  caregiverId: number;
  caregiverName: string;
  caregiverEmail: string;
  relationshipLabel: string;
  permissions: string;
  status: 'PENDING' | 'ACTIVE' | 'REVOKED';
  invitedAt: string;
  acceptedAt?: string;
}

export interface DelegatedPatientResponse {
  accessId: number;
  patientId: number;
  patientName: string;
  patientEmail: string;
  relationshipLabel: string;
  permissions: string;
  acceptedAt: string;
}

@Injectable({ providedIn: 'root' })
export class CaregiverService {

  private readonly apiBase = 'http://localhost:8080/api/caregiver';

  constructor(private http: HttpClient) {}

  /** Patient: Invite a registered user to be a caregiver */
  inviteCaregiver(request: CaregiverInviteRequest): Observable<CaregiverLinkResponse> {
    return this.http.post<CaregiverLinkResponse>(`${this.apiBase}/invite`, request);
  }

  /** Patient: Get all caregivers for the logged-in patient */
  getMyCaregivers(): Observable<CaregiverLinkResponse[]> {
    return this.http.get<CaregiverLinkResponse[]>(`${this.apiBase}/my-caregivers`);
  }

  /** Patient: Revoke a specific caregiver's access */
  revokeCaregiver(accessId: number): Observable<any> {
    return this.http.delete(`${this.apiBase}/${accessId}/revoke`);
  }

  /** Caregiver: Accept or decline a pending invitation */
  respondToInvitation(request: RespondRequest): Observable<CaregiverLinkResponse> {
    return this.http.post<CaregiverLinkResponse>(`${this.apiBase}/respond`, request);
  }

  /** Caregiver: Get all patients this caregiver is delegated to manage */
  getMyPatients(): Observable<DelegatedPatientResponse[]> {
    return this.http.get<DelegatedPatientResponse[]>(`${this.apiBase}/my-patients`);
  }

  /** Caregiver: Pay for a patient's pending order on their behalf */
  payOnBehalf(request: ProxyPaymentRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiBase}/pay-on-behalf`, request);
  }

  /** One-click accept from email link */
  acceptViaEmailLink(accessId: number): Observable<CaregiverLinkResponse> {
    return this.http.get<CaregiverLinkResponse>(`${this.apiBase}/accept/${accessId}`);
  }
}
