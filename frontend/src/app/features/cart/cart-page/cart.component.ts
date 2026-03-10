import { Component, OnInit } from '@angular/core';
import { CartService, CartResponse, CartItemResponse } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-cart',
    templateUrl: './cart.component.html',
    styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {

    cart: CartResponse | null = null;
    loading = true;
    error: string | null = null;
    updatingItemId: number | null = null;

    private userId!: number;

    constructor(private cartService: CartService, private authService: AuthService) { }

    ngOnInit(): void {
        this.authService.currentUser$.subscribe(user => {
            if (user) {
                this.userId = user.id;
                this.loadCart();
            }
        });
    }

    loadCart(): void {
        this.loading = true;
        this.error = null;

        this.cartService.getCart(this.userId).subscribe({
            next: (cart) => {
                this.cart = cart;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load cart. Please try again.';
                this.loading = false;
                console.error('Error loading cart:', err);
            }
        });
    }

    // ── 8️⃣ Update Quantity ────────────────────────────────────────────────────

    increaseQuantity(item: CartItemResponse): void {
        if (item.quantity >= item.availableStock) {
            return; // Cannot exceed stock
        }
        this.updateQuantity(item.cartItemId, item.quantity + 1);
    }

    decreaseQuantity(item: CartItemResponse): void {
        if (item.quantity <= 1) {
            return; // Minimum quantity is 1
        }
        this.updateQuantity(item.cartItemId, item.quantity - 1);
    }

    onQuantityInput(item: CartItemResponse, event: Event): void {
        const input = event.target as HTMLInputElement;
        let value = parseInt(input.value, 10);

        if (isNaN(value) || value < 1) {
            value = 1;
        }
        if (value > item.availableStock) {
            value = item.availableStock;
        }

        input.value = value.toString();
        this.updateQuantity(item.cartItemId, value);
    }

    private updateQuantity(cartItemId: number, quantity: number): void {
        this.updatingItemId = cartItemId;

        this.cartService.updateCartItemQuantity(this.userId, cartItemId, quantity).subscribe({
            next: (cart) => {
                this.cart = cart;
                this.updatingItemId = null;
            },
            error: (err) => {
                const message = err.error?.message || err.error || 'Failed to update quantity.';
                this.error = message;
                this.updatingItemId = null;
                console.error('Error updating quantity:', err);
            }
        });
    }

    // ── 9️⃣ Remove Product ─────────────────────────────────────────────────────

    removeItem(cartItemId: number): void {
        this.updatingItemId = cartItemId;

        this.cartService.removeFromCart(this.userId, cartItemId).subscribe({
            next: (cart) => {
                this.cart = cart;
                this.updatingItemId = null;
            },
            error: (err) => {
                this.error = 'Failed to remove item. Please try again.';
                this.updatingItemId = null;
                console.error('Error removing item:', err);
            }
        });
    }

    clearCart(): void {
        this.cartService.clearCart(this.userId).subscribe({
            next: () => {
                this.cart = { cartId: this.cart?.cartId ?? 0, items: [], totalPrice: 0, totalItems: 0 };
            },
            error: (err) => {
                this.error = 'Failed to clear cart. Please try again.';
                console.error('Error clearing cart:', err);
            }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    get isCartEmpty(): boolean {
        return !this.cart || this.cart.items.length === 0;
    }
}
