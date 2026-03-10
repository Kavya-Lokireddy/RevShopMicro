import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { CartService, CartItemResponse } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { switchMap } from 'rxjs';

interface CheckoutFormState {
  name: string;
  phoneNumber: string;
  shippingAddress: string;
  totalAmount: number;
}

@Component({
  selector: 'app-checkout-page',
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.css']
})
export class CheckoutPageComponent implements OnInit {

  order: CheckoutFormState = {
    name: '',
    phoneNumber: '',
    shippingAddress: '',
    totalAmount: 0
  };
  productId = '';
  itemCount = 1;
  cartItems: CartItemResponse[] = [];
  isSubmitting = false;
  errorMessage = '';
  private cartUserId = 3;
  private buyerUserId = '3';

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private cartService: CartService,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.buyerUserId = user.id.toString();
        this.cartUserId = user.id;
      }
      this.prefillBuyerDetails();
      this.setAmountFromCartService();

      this.route.queryParamMap.subscribe(() => {
        this.setDefaultAmount();
      });
    });
  }

  private prefillBuyerDetails(): void {
    const userObjectKeys = ['loggedInBuyer', 'currentUser', 'buyer', 'user', 'authUser'];

    for (const key of userObjectKeys) {
      const raw = localStorage.getItem(key);
      if (!raw) {
        continue;
      }

      try {
        const parsed = JSON.parse(raw);
        const name = String(parsed?.name ?? parsed?.username ?? parsed?.fullName ?? '').trim();
        const phone = String(parsed?.phoneNumber ?? parsed?.phone ?? parsed?.mobile ?? '').trim();
        const userId = String(parsed?.id ?? parsed?.userId ?? parsed?.buyerId ?? '').trim();

        if (name && !this.order.name) {
          this.order.name = name;
        }
        if (phone && !this.order.phoneNumber) {
          this.order.phoneNumber = phone;
        }
        if (userId && this.buyerUserId === '3') {
          this.buyerUserId = userId;
        }
      } catch {
        // Ignore malformed user JSON and continue fallback checks.
      }
    }

    const directNameKeys = ['buyerName', 'userName', 'name'];
    const directPhoneKeys = ['buyerPhoneNumber', 'phoneNumber', 'userPhone', 'phone'];
    const directUserIdKeys = ['buyerId', 'userId', 'currentUserId'];

    for (const key of directNameKeys) {
      if (this.order.name) {
        break;
      }
      const value = localStorage.getItem(key);
      if (value?.trim()) {
        this.order.name = value.trim();
      }
    }

    for (const key of directPhoneKeys) {
      if (this.order.phoneNumber) {
        break;
      }
      const value = localStorage.getItem(key);
      if (value?.trim()) {
        this.order.phoneNumber = value.trim();
      }
    }

    for (const key of directUserIdKeys) {
      if (this.buyerUserId !== '3') {
        break;
      }
      const value = localStorage.getItem(key);
      if (value?.trim()) {
        this.buyerUserId = value.trim();
      }
    }
  }

  private setDefaultAmount(): void {
    const params = this.route.snapshot.queryParamMap;

    const amountFromQuery = Number(params.get('amount'));
    const priceFromQuery = Number(params.get('productPrice'));
    const qtyFromQuery = Number(params.get('quantity'));

    this.productId = params.get('productId') || '';
    this.itemCount = Number.isFinite(qtyFromQuery) && qtyFromQuery > 0 ? qtyFromQuery : 1;

    if (Number.isFinite(amountFromQuery) && amountFromQuery > 0) {
      this.order.totalAmount = amountFromQuery;
      return;
    }

    if (Number.isFinite(priceFromQuery) && priceFromQuery > 0) {
      this.order.totalAmount = +(priceFromQuery * this.itemCount).toFixed(2);
      return;
    }

    const selectedProductAmount = this.getAmountFromSelectedProduct();
    if (selectedProductAmount > 0) {
      return;
    }

    const cartAmount = this.getAmountFromLocalCart();
    if (cartAmount > 0) {
      this.order.totalAmount = cartAmount;
    }
  }

  private setAmountFromCartService(): void {
    this.cartService.getCart(this.cartUserId).subscribe({
      next: (cart) => {
        const total = Number(cart?.totalPrice ?? 0);
        const count = Number(cart?.totalItems ?? 0);
        if (Number.isFinite(total) && total > 0) {
          this.order.totalAmount = +total.toFixed(2);
        }
        if (Number.isFinite(count) && count > 0) {
          this.itemCount = count;
        }
        if (cart && cart.items) {
          this.cartItems = cart.items;
        }
      },
      error: () => {
        // Ignore cart API errors and rely on query/localStorage fallback.
      }
    });
  }

  private getAmountFromSelectedProduct(): number {
    const raw = localStorage.getItem('selectedProduct');
    if (!raw) {
      return 0;
    }

    try {
      const product = JSON.parse(raw);
      const price = Number(product?.price ?? product?.amount ?? 0);
      const quantity = Number(product?.quantity ?? 1);

      if (!Number.isFinite(price) || price <= 0 || !Number.isFinite(quantity) || quantity <= 0) {
        return 0;
      }

      if (!this.productId && product?.id) {
        this.productId = String(product.id);
      }

      this.itemCount = quantity;
      this.order.totalAmount = +(price * quantity).toFixed(2);
      return this.order.totalAmount;
    } catch {
      return 0;
    }
  }

  private getAmountFromLocalCart(): number {
    const keys = [
      'revshop_cart_total',
      'cartTotal',
      'totalAmount',
      'revshop_cart',
      'cart',
      'cartItems',
      'items'
    ];

    for (const key of keys) {
      const raw = localStorage.getItem(key);
      if (!raw) {
        continue;
      }

      const numberFromRaw = this.parsePositiveNumber(raw);
      if (numberFromRaw > 0) {
        return numberFromRaw;
      }

      try {
        const parsed = JSON.parse(raw);

        const structuredTotal = this.getTotalFromStructuredCart(parsed);
        if (structuredTotal > 0) {
          return structuredTotal;
        }

        if (typeof parsed === 'object' && parsed !== null) {
          const parsedRecord = parsed as Record<string, unknown>;
          const nestedTotal = this.parsePositiveNumber(
            parsedRecord['total'] ??
            parsedRecord['cartTotal'] ??
            parsedRecord['totalAmount'] ??
            parsedRecord['grandTotal']
          );
          if (nestedTotal > 0) {
            return nestedTotal;
          }
        }
      } catch {
        // Ignore malformed cart JSON and keep searching.
      }
    }

    return 0;
  }

  private getTotalFromStructuredCart(parsed: unknown): number {
    const items = this.extractCartItems(parsed);
    if (items.length === 0) {
      return 0;
    }

    let total = 0;
    let itemCount = 0;

    for (const item of items) {
      if (typeof item !== 'object' || item === null) {
        continue;
      }

      const itemRecord = item as Record<string, unknown>;
      const quantity = this.parsePositiveNumber(
        itemRecord['quantity'] ??
        itemRecord['qty'] ??
        itemRecord['count'] ??
        1
      ) || 1;

      const unitPrice = this.parsePositiveNumber(
        itemRecord['price'] ??
        itemRecord['amount'] ??
        itemRecord['productPrice'] ??
        itemRecord['unitPrice'] ??
        itemRecord['sellingPrice']
      );

      const lineTotal = this.parsePositiveNumber(
        itemRecord['lineTotal'] ??
        itemRecord['total'] ??
        itemRecord['totalAmount'] ??
        itemRecord['itemTotal']
      );

      if (unitPrice > 0) {
        total += unitPrice * quantity;
        itemCount += quantity;
        continue;
      }

      if (lineTotal > 0) {
        total += lineTotal;
        itemCount += quantity;
      }
    }

    if (total > 0) {
      this.itemCount = itemCount > 0 ? itemCount : this.itemCount;
      return +total.toFixed(2);
    }

    return 0;
  }

  private extractCartItems(parsed: unknown): unknown[] {
    if (Array.isArray(parsed)) {
      return parsed;
    }

    if (typeof parsed !== 'object' || parsed === null) {
      return [];
    }

    const record = parsed as Record<string, unknown>;
    const candidates = [record['items'], record['cartItems'], record['products'], record['cart']];

    for (const candidate of candidates) {
      if (Array.isArray(candidate)) {
        return candidate;
      }
    }

    return [];
  }

  private parsePositiveNumber(value: unknown): number {
    if (typeof value === 'number') {
      return Number.isFinite(value) && value > 0 ? +value.toFixed(2) : 0;
    }

    if (typeof value === 'string') {
      const cleaned = value.replace(/[^\d.-]/g, '');
      if (!cleaned) {
        return 0;
      }
      const parsed = Number(cleaned);
      return Number.isFinite(parsed) && parsed > 0 ? +parsed.toFixed(2) : 0;
    }

    return 0;
  }

  placeOrder(): void {
    this.errorMessage = '';

    if (!this.order.name.trim() || !this.order.shippingAddress.trim()) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }

    const phoneRegex = /^\+?[0-9]{10,15}$/;
    if (!phoneRegex.test(this.order.phoneNumber.trim())) {
      this.errorMessage = 'Enter a valid phone number (10 to 15 digits).';
      return;
    }

    if (!this.order.totalAmount || this.order.totalAmount <= 0) {
      this.errorMessage = 'Total amount must be greater than 0.';
      return;
    }

    this.isSubmitting = true;
    this.order.name = this.order.name.trim();
    this.order.phoneNumber = this.order.phoneNumber.trim();

    let items = this.cartItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }));

    if (items.length === 0 && this.productId) {
      items = [{ productId: Number(this.productId), quantity: this.itemCount }];
    }

    if (items.length === 0) {
      this.errorMessage = 'No items found in cart to purchase.';
      this.isSubmitting = false;
      return;
    }

    const userId = Number(this.buyerUserId);
    const billingAddress = this.order.shippingAddress.trim();
    const shippingAddress = this.order.shippingAddress.trim();

    this.orderService.initiateCheckout(userId, this.order.totalAmount).pipe(
      switchMap((session) =>
        this.orderService.addCheckoutAddress(session.sessionId, userId, {
          shippingAddress,
          billingAddress,
          contactName: this.order.name,
          phoneNumber: this.order.phoneNumber
        })
      )
    ).subscribe({
      next: (session) => {
        this.isSubmitting = false;
        this.router.navigate(['/payment'], { state: { checkoutSessionId: session.sessionId, buyerId: userId } });
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err?.error?.message || err?.error || 'Failed to initiate checkout';
      }
    });
  }
}
