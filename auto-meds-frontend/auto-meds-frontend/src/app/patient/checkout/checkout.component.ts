import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { Cart } from '../../core/models/cart.model';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  checkoutForm!: FormGroup;
  cart: Cart | null = null;
  loading = true;
  submitting = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkoutForm = this.formBuilder.group({
      deliveryAddress: ['', Validators.required],
      paymentMethod: ['CASH_ON_DELIVERY', Validators.required]
    });

    this.loadCart();
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (data) => {
        this.cart = data;
        this.loading = false;
        if (!data || data.items.length === 0) {
          this.router.navigate(['/cart']);
        }
        
        // Auto pre-fill default address from user profile
        const user = this.authService.currentUserValue;
        if (user) {
          this.checkoutForm.patchValue({
            deliveryAddress: '123 Health Ave, Suite 4B, Greenville, 560001'
          });
        }
      },
      error: () => this.loading = false
    });
  }

  onPlaceOrder(): void {
    if (this.checkoutForm.invalid) {
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const { deliveryAddress, paymentMethod } = this.checkoutForm.value;

    this.orderService.checkout(deliveryAddress, paymentMethod).subscribe({
      next: (order) => {
        this.submitting = false;
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to place order. Please check stock availability.';
        this.submitting = false;
      }
    });
  }
}
