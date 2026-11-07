import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { Subject, Observable, Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { API_BASE } from '../constants/api.config';

export interface InventoryUpdateEvent {
  medicineId: number;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  inStock: boolean;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RealtimeService implements OnDestroy {
  private eventSource: EventSource | null = null;
  private inventorySubject = new Subject<InventoryUpdateEvent>();
  private reconnectTimeout: any = null;
  private authSubscription: Subscription | null = null;

  public inventoryUpdates$: Observable<InventoryUpdateEvent> = this.inventorySubject.asObservable();

  constructor(
    private authService: AuthService,
    private ngZone: NgZone
  ) {
    // Listen to login/logout state changes to refresh SSE stream
    this.authSubscription = this.authService.currentUser$.subscribe(() => {
      this.reconnect();
    });
  }

  public connect(): void {
    if (typeof window === 'undefined' || typeof EventSource === 'undefined') {
      return;
    }

    this.disconnect();

    const token = this.authService.token;
    let url = '';

    if (token) {
      if (this.authService.isAdmin() || this.authService.isPharmacist()) {
        url = `${API_BASE}/notifications/staff-stream?token=${encodeURIComponent(token)}`;
      } else {
        url = `${API_BASE}/notifications/stream?token=${encodeURIComponent(token)}`;
      }
    } else {
      url = `${API_BASE}/notifications/public-stream`;
    }

    try {
      this.eventSource = new EventSource(url);

      this.eventSource.addEventListener('CONNECTED', (e: MessageEvent) => {
        // SSE connected
      });

      this.eventSource.addEventListener('INVENTORY_UPDATED', (e: MessageEvent) => {
        try {
          const data: InventoryUpdateEvent = JSON.parse(e.data);
          this.ngZone.run(() => {
            this.inventorySubject.next(data);
          });
        } catch (err) {
          console.error('[RealtimeService] Failed to parse INVENTORY_UPDATED data', err);
        }
      });

      this.eventSource.addEventListener('PING', () => {
        // keepalive heartbeat
      });

      this.eventSource.onerror = () => {
        // Browser EventSource automatically reconnects, but if state is closed, retry with backoff
        if (this.eventSource?.readyState === EventSource.CLOSED) {
          this.scheduleReconnect();
        }
      };
    } catch (err) {
      console.warn('[RealtimeService] Could not establish EventSource connection', err);
      this.scheduleReconnect();
    }
  }

  public disconnect(): void {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

  public reconnect(): void {
    this.disconnect();
    this.connect();
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimeout) {
      return;
    }
    this.reconnectTimeout = setTimeout(() => {
      this.reconnectTimeout = null;
      this.connect();
    }, 5000);
  }

  ngOnDestroy(): void {
    this.disconnect();
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }
}
