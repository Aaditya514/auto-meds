import { Component, OnInit } from '@angular/core';
import { AdminService, AdminDashboardMetrics } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  metrics: AdminDashboardMetrics | null = null;
  loading = true;
  triggerMessage = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.loading = true;
    this.adminService.getDashboardMetrics().subscribe({
      next: (data) => {
        this.metrics = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  triggerScheduler(): void {
    this.triggerMessage = '';
    this.adminService.triggerRefillScheduler().subscribe({
      next: (res) => {
        this.triggerMessage = 'Auto-refill scheduler process completed successfully!';
        this.loadMetrics();
        setTimeout(() => this.triggerMessage = '', 5000);
      },
      error: (err) => {
        this.triggerMessage = 'Scheduler trigger failed: ' + err.message;
        setTimeout(() => this.triggerMessage = '', 5000);
      }
    });
  }
}
