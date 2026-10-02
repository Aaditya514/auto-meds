import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subscription } from '../models/subscription.model';
import { PrescriptionOcrResult } from '../models/prescription-ocr.model';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private apiUrl = 'http://localhost:8080/api/subscriptions';
  private rxUrl = 'http://localhost:8080/api/prescriptions';

  constructor(private http: HttpClient) {}

  createSubscription(medicineId: number | null | undefined, dosage: string | null, frequency: string | null, quantity: number | null, prescriptionFile: File, doctorVisitDate?: string): Observable<Subscription> {
    const formData = new FormData();
    if (medicineId) {
      formData.append('medicineId', medicineId.toString());
    }
    if (dosage) {
      formData.append('dosage', dosage);
    }
    if (frequency) {
      formData.append('frequency', frequency);
    }
    if (quantity) {
      formData.append('quantity', quantity.toString());
    }
    formData.append('prescriptionFile', prescriptionFile);
    if (doctorVisitDate) {
      formData.append('doctorVisitDate', doctorVisitDate);
    }

    return this.http.post<Subscription>(this.apiUrl, formData);
  }

  // Phase 4 OCR Scan: Extracts candidate medicines & doctor details on the fly
  scanPrescription(file: File): Observable<PrescriptionOcrResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<PrescriptionOcrResult>(`${this.rxUrl}/scan`, formData);
  }

  // Phase 4 OCR Audit: Retrieves stored OCR data for a prescription
  getPrescriptionOcr(prescriptionId: number): Observable<PrescriptionOcrResult> {
    return this.http.get<PrescriptionOcrResult>(`${this.rxUrl}/${prescriptionId}/ocr`);
  }

  // Phase 4 Chronic Care: Emergency 5-Day Bridge Supply
  requestBridgeSupply(id: number): Observable<Subscription> {
    return this.http.post<Subscription>(`${this.apiUrl}/${id}/bridge-supply`, {});
  }

  // Phase 4 Chronic Care: Vacation Snooze (7 or 14 days)
  snoozeSubscription(id: number, days: number = 7): Observable<Subscription> {
    return this.http.post<Subscription>(`${this.apiUrl}/${id}/snooze?days=${days}`, {});
  }

  // Phase 4 Chronic Care: Refill Synchronization ("Pillbox Day")
  syncRefills(targetDay: number = 1): Observable<Subscription[]> {
    return this.http.post<Subscription[]>(`${this.apiUrl}/sync-refills?targetDay=${targetDay}`, {});
  }

  getMySubscriptions(): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.apiUrl}/my`);
  }

  getSubscriptionById(id: number): Observable<Subscription> {
    return this.http.get<Subscription>(`${this.apiUrl}/${id}`);
  }

  cancelSubscription(id: number): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.apiUrl}/${id}/cancel`, {});
  }

  pauseSubscription(id: number): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.apiUrl}/${id}/pause`, {});
  }

  resumeSubscription(id: number): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.apiUrl}/${id}/resume`, {});
  }

  renewSubscription(id: number, prescriptionFile?: File): Observable<Subscription> {
    const formData = new FormData();
    if (prescriptionFile) {
      formData.append('prescriptionFile', prescriptionFile);
    }
    return this.http.put<Subscription>(`${this.apiUrl}/${id}/renew`, formData);
  }
}
