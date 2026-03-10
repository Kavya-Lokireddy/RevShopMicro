import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService, PaymentProcessRequest } from '../../../core/services/order.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-payment-page',
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.css']
})
export class PaymentPageComponent {

  orderId = '';
  checkoutSessionId: number | null = null;
  buyerId = 3;

  paymentMethod = 'COD';
  isProcessing = false;
  message = '';
  isError = false;

  constructor(
    private orderService: OrderService,
    private cartService: CartService,
    private router: Router
  ) {
    const nav = this.router.getCurrentNavigation();
    this.checkoutSessionId = nav?.extras.state?.['checkoutSessionId'] ?? null;
    this.buyerId = nav?.extras.state?.['buyerId'] ?? 3;
  }

  ngOnInit() {
    if (!this.checkoutSessionId) {
      this.router.navigate(['/checkout']);
    }
  }

  pay(): void {
    this.message = '';
    this.isError = false;

    if (!this.checkoutSessionId) {
      this.isError = true;
      this.message = 'Missing checkout session. Please place order again.';
      return;
    }

    this.isProcessing = true;

    const paymentPayload: PaymentProcessRequest = {
      checkoutSessionId: this.checkoutSessionId,
      paymentMethod: this.paymentMethod as PaymentProcessRequest['paymentMethod']
    };

    this.orderService.processPayment(paymentPayload).subscribe({
      next: (response) => {
        this.isProcessing = false;
        this.orderId = String(response.orderId);
        this.message = `Payment successful. Order #${response.orderId}`;
        this.isError = false;

        // Optionally clear user's cart after checkout.
        this.cartService.clearCart(this.buyerId).subscribe();

        this.router.navigate(['/order-confirmation'], {
          queryParams: {
            orderId: response.orderId,
            paymentMethod: this.paymentMethod
          }
        });
      },
      error: () => {
        this.isProcessing = false;
        this.isError = true;
        this.message = 'Payment request failed. Please try again.';
      }
    });
  }
}
