import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-order-confirmation',
  templateUrl: './order-confirmation.component.html',
  styleUrls: ['./order-confirmation.component.css']
})
export class OrderConfirmationComponent implements OnInit {

  orderId: string = '';
  paymentMethod: string = '';

  paymentMethodLabel: string = '';
  paymentIcon: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.orderId = this.route.snapshot.queryParamMap.get('orderId') || 'N/A';
    this.paymentMethod = this.route.snapshot.queryParamMap.get('paymentMethod') || 'COD';

    switch (this.paymentMethod.toUpperCase()) {
      case 'COD':
        this.paymentMethodLabel = 'Cash on Delivery';
        this.paymentIcon = '💵';
        break;
      case 'CREDIT_CARD':
        this.paymentMethodLabel = 'Credit Card';
        this.paymentIcon = '💳';
        break;
      case 'DEBIT_CARD':
        this.paymentMethodLabel = 'Debit Card';
        this.paymentIcon = '🏧';
        break;
      default:
        this.paymentMethodLabel = this.paymentMethod;
        this.paymentIcon = '✅';
    }
  }

  goToOrders(): void {
    this.router.navigate(['/orders']);
  }

  continueShopping(): void {
    this.router.navigate(['/buyer/dashboard']);
  }
}
