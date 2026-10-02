import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { ComplianceAndPaymentService } from '../../core/services/compliance-and-payment.service';
import { Cart } from '../../core/models/cart.model';
import { Order } from '../../core/models/order.model';
import { DispensingSlip, DrugInteractionAlert } from '../../core/models/compliance-and-payment.model';
import { QrCodeGenerator } from '../../core/utils/qr-code.util';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  checkoutForm!: FormGroup;
  cardForm!: FormGroup;

  cart: Cart | null = null;
  loading = true;
  submitting = false;
  errorMessage = '';

  // Clinical Safety Interceptor
  clinicalAlerts: DrugInteractionAlert[] = [];
  checkingSafety = false;
  safetyAcknowledged = false;
  userAllergies = '';
  userConditions = '';

  // Payment Selection
  selectedPaymentTab: 'UPI' | 'CARD' | 'CASH_ON_DELIVERY' = 'UPI';
  upiId = 'patient@okhdfcbank';
  upiQrDataUrl = '';

  // 3D Secure Card OTP Modal
  showOtpModal = false;
  cardOtp = '123456';
  processingPayment = false;
  pendingOrderId: number | null = null;

  // Order Success & Dispensing Slip
  placedOrder: Order | null = null;
  dispensingSlip: DispensingSlip | null = null;
  showDispensingModal = false;

  constructor(
    private formBuilder: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private complianceService: ComplianceAndPaymentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkoutForm = this.formBuilder.group({
      deliveryAddress: ['', Validators.required],
      paymentMethod: ['UPI', Validators.required]
    });

    this.cardForm = this.formBuilder.group({
      cardNumber: ['4532 8901 2345 6789', [Validators.required, Validators.minLength(16)]],
      cardHolder: ['ADITYA KUMAR', Validators.required],
      expiryDate: ['12/28', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/?([0-9]{2})$/)]],
      cvv: ['789', [Validators.required, Validators.minLength(3), Validators.maxLength(4)]]
    });

    this.loadUserDataAndCart();
  }

  loadUserDataAndCart(): void {
    this.loading = true;
    this.authService.getProfile().subscribe({
      next: (profile) => {
        if (profile) {
          this.userAllergies = profile.allergies || '';
          this.userConditions = profile.chronicConditions || '';
          if (profile.address) {
            const fullAddress = `${profile.address}, ${profile.city || ''}, ${profile.state || ''} ${profile.pincode || ''}`.trim();
            this.checkoutForm.patchValue({ deliveryAddress: fullAddress });
          }
        }
        this.loadCart();
      },
      error: () => this.loadCart()
    });
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (data) => {
        this.cart = data;
        this.loading = false;
        if (!data || data.items.length === 0) {
          this.router.navigate(['/cart']);
          return;
        }

        // Generate dynamic UPI QR payload
        this.generateUpiQr(data.totalAmount);

        // Perform Clinical DDI & Allergy Interceptor Check
        const medicineIds = data.items.map(item => item.medicineId);
        this.checkClinicalSafety(medicineIds);
      },
      error: () => this.loading = false
    });
  }

  generateUpiQr(amount: number): void {
    const upiUri = `upi://pay?pa=automeds.billing@icici&pn=AutoMeds%20Pharmacy&am=${amount}&cu=INR&tn=AutoMedsRef${Date.now()}`;
    this.upiQrDataUrl = QrCodeGenerator.generateDataUrl(upiUri, 180);
  }

  checkClinicalSafety(medicineIds: number[]): void {
    if (!medicineIds || medicineIds.length === 0) return;
    this.checkingSafety = true;
    this.complianceService.checkClinicalSafety(medicineIds, this.userAllergies, this.userConditions).subscribe({
      next: (alerts) => {
        this.clinicalAlerts = alerts;
        this.checkingSafety = false;
        // If no high/critical alerts, auto-acknowledge
        if (!alerts.some(a => a.severity === 'CRITICAL' || a.severity === 'HIGH')) {
          this.safetyAcknowledged = true;
        }
      },
      error: () => {
        this.checkingSafety = false;
      }
    });
  }

  selectPaymentMethod(method: 'UPI' | 'CARD' | 'CASH_ON_DELIVERY'): void {
    this.selectedPaymentTab = method;
    this.checkoutForm.patchValue({ paymentMethod: method });
  }

  setUpiSuggestion(suffix: string): void {
    const prefix = this.upiId.split('@')[0] || 'patient';
    this.upiId = prefix + suffix;
  }

  hasCriticalAlerts(): boolean {
    return this.clinicalAlerts.some(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');
  }

  onInitiateOrder(): void {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    if (this.hasCriticalAlerts() && !this.safetyAcknowledged) {
      this.errorMessage = 'Please read and acknowledge the clinical safety and allergy alert before proceeding.';
      return;
    }

    if (this.selectedPaymentTab === 'CARD' && this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const { deliveryAddress, paymentMethod } = this.checkoutForm.value;

    // 1. Create order on backend
    this.orderService.checkout(deliveryAddress, paymentMethod).subscribe({
      next: (order) => {
        if (paymentMethod === 'UPI') {
          // Process UPI immediate verification
          this.executePayment(order.id, 'UPI', { upiId: this.upiId });
        } else if (paymentMethod === 'CARD') {
          // Trigger 3D Secure Bank OTP modal
          this.pendingOrderId = order.id;
          this.submitting = false;
          this.showOtpModal = true;
        } else {
          // Cash on Delivery
          this.submitting = false;
          this.handleOrderComplete(order);
        }
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to place order. Please verify stock availability.';
        this.submitting = false;
      }
    });
  }

  confirmCardOtp(): void {
    if (!this.pendingOrderId) return;
    this.processingPayment = true;

    const cardVal = this.cardForm.value;
    const paymentReq = {
      orderId: this.pendingOrderId,
      paymentMethod: 'CARD' as const,
      cardNumber: cardVal.cardNumber.replace(/\s+/g, ''),
      cardHolder: cardVal.cardHolder,
      expiryDate: cardVal.expiryDate,
      cvv: cardVal.cvv,
      otp: this.cardOtp
    };

    this.complianceService.processPayment(paymentReq).subscribe({
      next: (resp) => {
        this.processingPayment = false;
        this.showOtpModal = false;
        // Fetch updated order
        this.orderService.getOrderById(this.pendingOrderId!).subscribe({
          next: (updatedOrder) => this.handleOrderComplete(updatedOrder),
          error: () => this.router.navigate(['/orders'])
        });
      },
      error: (err) => {
        this.processingPayment = false;
        this.errorMessage = err.message || '3D Secure authorization failed. Please check OTP.';
      }
    });
  }

  cancelOtp(): void {
    this.showOtpModal = false;
    if (this.pendingOrderId) {
      this.router.navigate(['/orders']);
    }
  }

  private executePayment(orderId: number, method: 'UPI' | 'CARD', extra: any): void {
    this.complianceService.processPayment({
      orderId,
      paymentMethod: method,
      upiId: extra.upiId
    }).subscribe({
      next: (resp) => {
        this.submitting = false;
        this.orderService.getOrderById(orderId).subscribe({
          next: (updatedOrder) => this.handleOrderComplete(updatedOrder),
          error: () => this.router.navigate(['/orders'])
        });
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.message || 'Payment processing failed.';
      }
    });
  }

  private handleOrderComplete(order: Order): void {
    this.placedOrder = order;
    // Pre-fetch dispensing slip
    this.complianceService.getDispensingSlip(order.id).subscribe({
      next: (slip) => this.dispensingSlip = slip,
      error: () => {}
    });
  }

  openDispensingModal(): void {
    this.showDispensingModal = true;
  }

  closeDispensingModal(): void {
    this.showDispensingModal = false;
  }
}
