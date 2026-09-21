import { Component, OnInit } from '@angular/core';
import { SubscriptionService } from '../../core/services/subscription.service';
import { OrderService } from '../../core/services/order.service';
import { NotificationService } from '../../core/services/notification.service';
import { Subscription } from '../../core/models/subscription.model';
import { Order } from '../../core/models/order.model';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  subscriptions: Subscription[] = [];
  activeSubscriptions: Subscription[] = [];
  upcomingRefills: Subscription[] = [];
  recentOrders: Order[] = [];
  notifications: Notification[] = [];
  loading = true;

  constructor(
    private subscriptionService: SubscriptionService,
    private orderService: OrderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    this.subscriptionService.getMySubscriptions().subscribe({
      next: (data) => {
        this.subscriptions = data;
        this.activeSubscriptions = data.filter(s => s.status === 'ACTIVE');
        this.upcomingRefills = data.filter(s => s.status === 'ACTIVE' && s.nextRefillDate);
        this.loading = false;
      },
      error: () => this.loading = false
    });

    this.orderService.getMyOrders().subscribe({
      next: (orders) => {
        this.recentOrders = orders.slice(0, 3);
      }
    });

    this.notificationService.getMyNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs.slice(0, 5);
      }
    });
  }

  cancelSubscription(id: number): void {
    if (confirm('Are you sure you want to cancel this medication subscription?')) {
      this.subscriptionService.cancelSubscription(id).subscribe({
        next: () => this.loadDashboardData()
      });
    }
  }

  pauseSubscription(id: number): void {
    this.subscriptionService.pauseSubscription(id).subscribe({
      next: () => this.loadDashboardData()
    });
  }

  resumeSubscription(id: number): void {
    this.subscriptionService.resumeSubscription(id).subscribe({
      next: () => this.loadDashboardData()
    });
  }
}
