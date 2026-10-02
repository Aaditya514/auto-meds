import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { ComplianceAndPaymentService } from '../../core/services/compliance-and-payment.service';
import { Order } from '../../core/models/order.model';
import { DispensingSlip } from '../../core/models/compliance-and-payment.model';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css']
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  message = '';

  // Dispensing Slip Modal
  selectedDispensingSlip: DispensingSlip | null = null;
  showDispensingModal = false;
  loadingSlip = false;

  statusOptions = [
    'PENDING', 'APPROVED', 'PACKED', 'DISPATCHED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REJECTED'
  ];

  constructor(
    private adminService: AdminService,
    private complianceService: ComplianceAndPaymentService
  ) {}

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
        if (updated.dispensingSlipCode) {
          order.dispensingSlipCode = updated.dispensingSlipCode;
        }
        this.message = `Order #${order.id} status updated to ${newStatus}.`;
        setTimeout(() => this.message = '', 4000);
      }
    });
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
    this.adminService.downloadPrescription(prescriptionId).subscribe({
      next: (blob: Blob) => {
        const fileUrl = URL.createObjectURL(blob);
        window.open(fileUrl, '_blank');
      },
      error: () => {
        alert('Could not open prescription file.');
      }
    });
  }
}
