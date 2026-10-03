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
  message = '';
  errorMessage = '';
  actionLoading = false;

  // Snooze Modal State
  snoozeModalOpen = false;
  selectedSubForSnooze: Subscription | null = null;
  selectedSnoozeDays: number = 7;

  // Refill Synchronization ("Pillbox Day") Modal State
  syncModalOpen = false;
  selectedPillboxDay: number = 1;

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
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load your subscriptions.';
      }
    });
  }

  cancel(id: number): void {
    if (confirm('Are you sure you want to cancel this subscription?')) {
      this.subscriptionService.cancelSubscription(id).subscribe({
        next: () => {
          this.message = 'Subscription cancelled successfully.';
          this.loadSubscriptions();
        },
        error: (err) => this.errorMessage = err.message || 'Failed to cancel subscription.'
      });
    }
  }

  pause(id: number): void {
    this.subscriptionService.pauseSubscription(id).subscribe({
      next: () => {
        this.message = 'Subscription paused.';
        this.loadSubscriptions();
      },
      error: (err) => this.errorMessage = err.message || 'Failed to pause subscription.'
    });
  }

  resume(id: number): void {
    this.subscriptionService.resumeSubscription(id).subscribe({
      next: () => {
        this.message = 'Subscription resumed.';
        this.loadSubscriptions();
      },
      error: (err) => this.errorMessage = err.message || 'Failed to resume subscription.'
    });
  }

  // Phase 4: Emergency 5-Day Bridge Supply
  requestBridgeSupply(sub: Subscription): void {
    if (this.actionLoading) return; // Design Motion: Guard against double-tap

    if (!confirm(`Request an immediate 5-Day Emergency Bridge Supply for ${sub.medicineName}? An expedited order will be dispatched while your prescription renewal is processed.`)) {
      return;
    }
    this.actionLoading = true;
    this.message = '';
    this.errorMessage = '';
    this.subscriptionService.requestBridgeSupply(sub.id).subscribe({
      next: (updated) => {
        this.actionLoading = false;
        this.message = `🚨 Emergency 5-Day Bridge Supply activated for ${sub.medicineName}! An expedited bridge delivery order has been generated.`;
        this.loadSubscriptions();
      },
      error: (err) => {
        this.actionLoading = false;
        this.errorMessage = err.error?.message || err.message || 'Could not request bridge supply.';
      }
    });
  }

  // Phase 4: Vacation Snooze
  openSnoozeModal(sub: Subscription): void {
    this.selectedSubForSnooze = sub;
    this.selectedSnoozeDays = 7;
    this.snoozeModalOpen = true;
  }

  closeSnoozeModal(): void {
    this.snoozeModalOpen = false;
    this.selectedSubForSnooze = null;
  }

  submitSnooze(): void {
    if (!this.selectedSubForSnooze || this.actionLoading) return;
    this.actionLoading = true;
    const sub = this.selectedSubForSnooze;
    this.subscriptionService.snoozeSubscription(sub.id, this.selectedSnoozeDays).subscribe({
      next: (res) => {
        this.actionLoading = false;
        this.message = `Refill snoozed by ${this.selectedSnoozeDays} days for ${sub.medicineName}. Next refill has been pushed forward.`;
        this.closeSnoozeModal();
        this.loadSubscriptions();
      },
      error: (err) => {
        this.actionLoading = false;
        this.errorMessage = err.error?.message || err.message || 'Failed to snooze refill.';
      }
    });
  }

  // Phase 4: Refill Synchronization ("Pillbox Day")
  openSyncModal(): void {
    this.syncModalOpen = true;
    this.selectedPillboxDay = 1;
  }

  closeSyncModal(): void {
    this.syncModalOpen = false;
  }

  submitSyncRefills(): void {
    if (this.actionLoading) return;
    this.actionLoading = true;
    this.subscriptionService.syncRefills(this.selectedPillboxDay).subscribe({
      next: (subs) => {
        this.actionLoading = false;
        this.message = `Pillbox Day Synced! ${subs.length} active chronic prescription refill(s) have been aligned to Day ${this.selectedPillboxDay} of every month.`;
        this.closeSyncModal();
        this.loadSubscriptions();
      },
      error: (err) => {
        this.actionLoading = false;
        this.errorMessage = err.error?.message || err.message || 'Failed to synchronize refills.';
      }
    });
  }
}
