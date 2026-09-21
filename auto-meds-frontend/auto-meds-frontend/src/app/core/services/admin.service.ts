import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Medicine } from '../models/medicine.model';
import { Subscription } from '../models/subscription.model';
import { Order } from '../models/order.model';
import { User } from '../models/user.model';

export interface AdminDashboardMetrics {
  totalPatients: number;
  totalMedicines: number;
  activeSubscriptions: number;
  pendingRequests: number;
  upcomingRefills: number;
  pendingOrders: number;
  lowStockMedicines: number;
  outOfStockMedicines: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getDashboardMetrics(): Observable<AdminDashboardMetrics> {
    return this.http.get<AdminDashboardMetrics>(`${this.apiUrl}/dashboard`);
  }

  getPendingSubscriptionRequests(): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.apiUrl}/subscription-requests`);
  }

  approveSubscription(id: number, assignments: any[]): Observable<Subscription[]> {
    return this.http.put<Subscription[]>(`${this.apiUrl}/subscriptions/${id}/approve`, assignments);
  }

  rejectSubscription(id: number, reason?: string): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.apiUrl}/subscriptions/${id}/reject`, { reason });
  }

  requestClarification(id: number, message: string): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.apiUrl}/subscriptions/${id}/clarification`, { message });
  }

  createMedicine(medicine: Partial<Medicine>): Observable<Medicine> {
    return this.http.post<Medicine>(`${this.apiUrl}/medicines`, medicine);
  }

  updateMedicine(id: number, medicine: Partial<Medicine>): Observable<Medicine> {
    return this.http.put<Medicine>(`${this.apiUrl}/medicines/${id}`, medicine);
  }

  deactivateMedicine(id: number): Observable<Medicine> {
    return this.http.delete<Medicine>(`${this.apiUrl}/medicines/${id}`);
  }

  updateStock(medicineId: number, stockQuantity: number): Observable<Medicine> {
    return this.http.put<Medicine>(`${this.apiUrl}/inventory/${medicineId}/stock`, { stockQuantity });
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders`);
  }

  updateOrderStatus(id: number, orderStatus: string): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/orders/${id}/status`, { orderStatus });
  }

  getAllPatients(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  triggerRefillScheduler(): Observable<any> {
    return this.http.post(`${this.apiUrl}/scheduler/trigger-refill`, {});
  }

  downloadPrescription(id: number): Observable<Blob> {
    return this.http.get(`http://localhost:8080/api/prescriptions/${id}`, { responseType: 'blob' });
  }
}
