import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: AuthResponse | null = null;
  cartItemCount: number = 0;
  unreadNotificationCount: number = 0;

  constructor(
    public authService: AuthService,
    private cartService: CartService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        if (user.role === 'PATIENT') {
          this.loadCartCount();
        }
        this.loadNotifications();
      } else {
        this.cartItemCount = 0;
        this.unreadNotificationCount = 0;
      }
    });

    this.cartService.cart$.subscribe(cart => {
      if (cart) {
        this.cartItemCount = cart.totalItems;
      } else {
        this.cartItemCount = 0;
      }
    });
  }

  loadCartCount(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cartItemCount = cart.totalItems;
      },
      error: () => {}
    });
  }

  loadNotifications(): void {
    this.notificationService.getMyNotifications().subscribe({
      next: (notifs) => {
        this.unreadNotificationCount = notifs.filter(n => !n.isRead).length;
      },
      error: () => {}
    });
  }

  getUserInitials(): string {
    const name = this.currentUser?.name;
    if (!name) return 'U';
    const cleanName = name.replace(/^(Dr\.|Mr\.|Ms\.|Mrs\.)\s+/i, '').trim();
    const parts = cleanName.split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return cleanName.substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
