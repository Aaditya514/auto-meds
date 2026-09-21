import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { Order } from '../../core/models/order.model';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css']
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  message = '';

  statusOptions = [
    'PENDING', 'APPROVED', 'PACKED', 'DISPATCHED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REJECTED'
  ];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.adminService.getAllOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  updateStatus(order: Order, newStatus: string): void {
    if (order.orderStatus === newStatus) return;

    this.adminService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updated) => {
        order.orderStatus = updated.orderStatus;
        this.message = `Order #${order.id} status updated to ${newStatus}.`;
        setTimeout(() => this.message = '', 4000);
      }
    });
  }
}
