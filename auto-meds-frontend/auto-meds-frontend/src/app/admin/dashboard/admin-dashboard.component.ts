import { Component, OnInit } from '@angular/core';
import {
  AdminService,
  AdminDashboardMetrics,
  ProcurementAlert,
  DeficitSubscription
} from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  metrics: AdminDashboardMetrics | null = null;
  procurementAlerts: ProcurementAlert[] = [];
  deficitSubscriptions: DeficitSubscription[] = [];

  loading = true;
  triggering = false;
  triggerMessage = '';
  triggerSuccess = true;

  quickRestockingId: number | null = null;
  restockToast = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.adminService.getDashboardMetrics().subscribe({
      next: (data) => {
        this.metrics = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    this.adminService.getProcurementAlerts().subscribe({
      next: (alerts) => {
        this.procurementAlerts = (alerts || []).slice(0, 4);
      },
      error: () => {}
    });

    this.adminService.getDeficitSubscriptions().subscribe({
      next: (deficits) => {
        this.deficitSubscriptions = (deficits || []).slice(0, 4);
      },
      error: () => {}
    });
  }

  triggerScheduler(): void {
    this.triggering = true;
    this.triggerMessage = '';
    this.adminService.triggerRefillScheduler().subscribe({
      next: (res) => {
        this.triggering = false;
        this.triggerSuccess = true;
        this.triggerMessage = 'Refill scheduler executed: soft-lock reservations and reminder cadence verified.';
        this.loadAll();
        setTimeout(() => (this.triggerMessage = ''), 6000);
      },
      error: (err) => {
        this.triggering = false;
        this.triggerSuccess = false;
        this.triggerMessage = 'Scheduler trigger failed: ' + (err.error?.message || err.message);
        setTimeout(() => (this.triggerMessage = ''), 6000);
      }
    });
  }

  quickRestock(medicineId: number, packSize: number): void {
    const qty = packSize > 0 ? packSize : 50;
    this.quickRestockingId = medicineId;
    this.adminService.restockMedicine({ medicineId, quantity: qty }).subscribe({
      next: (res) => {
        this.quickRestockingId = null;
        this.restockToast = `Restocked ${res.medicineName} (+${qty} units). ${res.deficitsResolvedCount} deficit subscriptions auto-resolved!`;
        this.loadAll();
        setTimeout(() => (this.restockToast = ''), 6000);
      },
      error: (err) => {
        this.quickRestockingId = null;
        this.restockToast = 'Restock failed: ' + (err.error?.message || err.message);
        setTimeout(() => (this.restockToast = ''), 6000);
      }
    });
  }

  get hasCriticalAlerts(): boolean {
    if (!this.metrics) return false;
    return (
      (this.metrics.deficitSubscriptions ?? 0) > 0 ||
      (this.metrics.outOfStockMedicines ?? 0) > 0 ||
      (this.metrics.pendingRequests ?? 0) > 0
    );
  }

  get fulfillmentRate(): number {
    if (!this.metrics) return 100;
    const total = (this.metrics.activeSubscriptions ?? 0) + (this.metrics.deficitSubscriptions ?? 0);
    if (total === 0) return 100;
    return Math.round(((this.metrics.activeSubscriptions ?? 0) / total) * 100);
  }
}
