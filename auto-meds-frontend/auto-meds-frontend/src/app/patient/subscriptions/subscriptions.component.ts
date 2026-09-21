import { Component, OnInit } from '@angular/core';
import { SubscriptionService } from '../../core/services/subscription.service';
import { Subscription } from '../../core/models/subscription.model';

@Component({
  selector: 'app-subscriptions',
  templateUrl: './subscriptions.component.html',
  styleUrls: ['./subscriptions.component.css']
})
export class SubscriptionsComponent implements OnInit {
  subscriptions: Subscription[] = [];
  loading = true;

  constructor(private subscriptionService: SubscriptionService) {}

  ngOnInit(): void {
    this.loadSubscriptions();
  }

  loadSubscriptions(): void {
    this.loading = true;
    this.subscriptionService.getMySubscriptions().subscribe({
      next: (data) => {
        this.subscriptions = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  cancel(id: number): void {
    if (confirm('Are you sure you want to cancel this subscription?')) {
      this.subscriptionService.cancelSubscription(id).subscribe({
        next: () => this.loadSubscriptions()
      });
    }
  }

  pause(id: number): void {
    this.subscriptionService.pauseSubscription(id).subscribe({
      next: () => this.loadSubscriptions()
    });
  }

  resume(id: number): void {
    this.subscriptionService.resumeSubscription(id).subscribe({
      next: () => this.loadSubscriptions()
    });
  }
}
