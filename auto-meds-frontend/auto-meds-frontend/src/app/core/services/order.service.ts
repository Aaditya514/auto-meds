import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/orders';

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
    return this.http.get(`http://localhost:8080/api/prescriptions/${prescriptionId}`, { responseType: 'blob' });
  }
}
