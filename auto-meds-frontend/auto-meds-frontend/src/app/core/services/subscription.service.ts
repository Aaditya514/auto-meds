import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subscription } from '../models/subscription.model';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private apiUrl = 'http://localhost:8080/api/subscriptions';

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
