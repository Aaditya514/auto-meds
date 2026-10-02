import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AuditLog,
  DispensingSlip,
  DrugInteractionAlert,
  PaymentRequest,
  PaymentResponse
} from '../models/compliance-and-payment.model';

@Injectable({
  providedIn: 'root'
})
export class ComplianceAndPaymentService {
  private paymentApi = 'http://localhost:8080/api/payments';
  private clinicalSafetyApi = 'http://localhost:8080/api/clinical-safety';
  private auditLogsApi = 'http://localhost:8080/api/audit-logs';

  constructor(private http: HttpClient) {}

  /**
   * Check for Drug-Drug Interactions (DDI) and Patient Allergy/Condition conflicts
   */
  checkClinicalSafety(
    medicineIds: number[],
    patientAllergies?: string,
    patientConditions?: string
  ): Observable<DrugInteractionAlert[]> {
    return this.http.post<DrugInteractionAlert[]>(`${this.clinicalSafetyApi}/check`, {
      medicineIds,
      patientAllergies: patientAllergies || '',
      patientConditions: patientConditions || ''
    });
  }

  /**
   * Process payment (UPI / Card 3DS / COD)
   */
  processPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.paymentApi}/process`, request);
  }

  /**
   * Fetch official Four-Eyes verified clinical dispensing slip with QR code payload
   */
  getDispensingSlip(orderId: number): Observable<DispensingSlip> {
    return this.http.get<DispensingSlip>(`${this.paymentApi}/dispensing-slip/${orderId}`);
  }

  /**
   * Fetch immutable healthcare audit log trail for compliance (HIPAA / CDSCO)
   */
  getAuditLogs(action?: string, entityName?: string): Observable<AuditLog[]> {
    let params = new HttpParams();
    if (action) {
      params = params.set('action', action);
    }
    if (entityName) {
      params = params.set('entityName', entityName);
    }
    return this.http.get<AuditLog[]>(this.auditLogsApi, { params });
  }
}
