import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { Cart } from '../../core/models/cart.model';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;
  loading = true;
  errorMessage = '';

  constructor(private cartService: CartService, private router: Router) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (data) => {
        this.cart = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load cart.';
        this.loading = false;
      }
    });
  }

  updateQuantity(itemId: number, currentQty: number, change: number): void {
    const newQty = currentQty + change;
    if (newQty < 1) return;
    if (newQty > 10) {
      this.errorMessage = 'Maximum allowed limit is 10 units per medicine item per order.';
      setTimeout(() => this.errorMessage = '', 5000);
      return;
    }

    this.errorMessage = '';
    this.cartService.updateQuantity(itemId, newQty).subscribe({
      next: (data) => this.cart = data,
      error: (err) => {
        this.errorMessage = err.message || 'Could not update item quantity.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  removeItem(itemId: number): void {
    this.cartService.removeItem(itemId).subscribe({
      next: (data) => this.cart = data
    });
  }

  clearCart(): void {
    if (confirm('Are you sure you want to clear your cart?')) {
      this.cartService.clearCart().subscribe({
        next: () => this.loadCart()
      });
    }
  }

  proceedToCheckout(): void {
    if (this.cart && this.cart.items.length > 0) {
      this.router.navigate(['/checkout']);
    }
  }
}
