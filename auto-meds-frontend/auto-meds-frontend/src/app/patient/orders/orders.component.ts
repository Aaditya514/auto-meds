import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../core/services/order.service';
import { ComplianceAndPaymentService } from '../../core/services/compliance-and-payment.service';
import { Order } from '../../core/models/order.model';
import { DispensingSlip } from '../../core/models/compliance-and-payment.model';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  selectedOrder: Order | null = null;

  // Dispensing Slip Modal
  selectedDispensingSlip: DispensingSlip | null = null;
  showDispensingModal = false;
  loadingSlip = false;

  constructor(
    private orderService: OrderService,
    private complianceService: ComplianceAndPaymentService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getMyOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  viewOrderDetails(order: Order): void {
    this.selectedOrder = order;
  }

  closeModal(): void {
    this.selectedOrder = null;
  }

  viewDispensingSlip(orderId: number): void {
    this.loadingSlip = true;
    this.complianceService.getDispensingSlip(orderId).subscribe({
      next: (slip) => {
        this.selectedDispensingSlip = slip;
        this.showDispensingModal = true;
        this.loadingSlip = false;
      },
      error: (err) => {
        alert(err.message || 'Dispensing slip is only generated after pharmacist approval.');
        this.loadingSlip = false;
      }
    });
  }

  closeDispensingModal(): void {
    this.showDispensingModal = false;
    this.selectedDispensingSlip = null;
  }

  viewPrescription(prescriptionId?: number): void {
    if (!prescriptionId) {
      alert('No prescription attached to this order.');
      return;
    }
    this.orderService.downloadPrescription(prescriptionId).subscribe({
      next: (blob: Blob) => {
        const fileUrl = URL.createObjectURL(blob);
        window.open(fileUrl, '_blank');
      },
      error: () => {
        alert('Could not load prescription document.');
      }
    });
  }
}
