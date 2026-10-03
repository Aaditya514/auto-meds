import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';
import { API_BASE } from '../constants/api.config';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${API_BASE}/orders`;

  constructor(private http: HttpClient) {}

  checkout(deliveryAddress: string, paymentMethod: string = 'CASH_ON_DELIVERY'): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/checkout`, { deliveryAddress, paymentMethod });
  }

  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/my`);
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  downloadPrescription(prescriptionId: number): Observable<Blob> {
    return this.http.get(`${API_BASE}/prescriptions/${prescriptionId}`, { responseType: 'blob' });
  }
}
