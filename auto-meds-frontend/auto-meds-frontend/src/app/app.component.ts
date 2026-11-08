import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { CartService } from './core/services/cart.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'auto-meds-frontend';
  mobileSidebarOpen = false;
  cartItemCount = 0;

  constructor(
    public authService: AuthService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.role === 'PATIENT') {
        this.loadCartCount();
      } else {
        this.cartItemCount = 0;
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
        this.cartItemCount = cart ? cart.totalItems : 0;
      },
      error: () => {}
    });
  }

  toggleMobileSidebar(): void {
    this.mobileSidebarOpen = !this.mobileSidebarOpen;
  }

  closeMobileSidebar(): void {
    this.mobileSidebarOpen = false;
  }

  getUserInitials(): string {
    const name = this.authService.currentUserValue?.name;
    if (!name) return 'P';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.closeMobileSidebar();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
