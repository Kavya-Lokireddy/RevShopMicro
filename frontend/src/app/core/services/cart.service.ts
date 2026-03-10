import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface CartItemResponse {
  cartItemId: number;
  productId: number;
  productName: string;
  productDescription: string;
  price: number;
  mrp: number;
  discountPercentage: number;
  quantity: number;
  availableStock: number;
  subtotal: number;
}

export interface CartResponse {
  cartId: number;
  items: CartItemResponse[];
  totalPrice: number;
  totalItems: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private baseUrl = '/api/cart';

  // BehaviorSubject to share cart state across components
  private cartSubject = new BehaviorSubject<CartResponse | null>(null);
  public cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) {}

  private buildUserHeader(userId: number) {
    return {
      headers: new HttpHeaders({
        'X-User-Id': String(userId)
      })
    };
  }

  // ===== 7️⃣ Add Product to Cart =====
  addToCart(userId: number, productId: number, quantity: number): Observable<CartResponse> {
    const options = this.buildUserHeader(userId);
    return this.http.post<CartResponse>(`${this.baseUrl}/items`, {
      productId,
      quantity
    }, options).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  // ===== 8️⃣ Update Cart Quantity =====
  updateCartItemQuantity(userId: number, cartItemId: number, quantity: number): Observable<CartResponse> {
    const options = this.buildUserHeader(userId);
    return this.http.put<CartResponse>(`${this.baseUrl}/items/${cartItemId}`, {
      quantity
    }, options).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  // ===== 9️⃣ Remove Product from Cart =====
  removeFromCart(userId: number, cartItemId: number): Observable<CartResponse> {
    const options = this.buildUserHeader(userId);
    return this.http.delete<CartResponse>(`${this.baseUrl}/items/${cartItemId}`, options).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  // ===== 9️⃣ View Cart =====
  getCart(userId: number): Observable<CartResponse> {
    const options = this.buildUserHeader(userId);
    return this.http.get<CartResponse>(`${this.baseUrl}`, options).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  // ===== Clear Cart =====
  clearCart(userId: number): Observable<void> {
    const options = this.buildUserHeader(userId);
    return this.http.delete<void>(`${this.baseUrl}`, options).pipe(
      tap(() => this.cartSubject.next(null))
    );
  }

  // Get current cart count for badge display
  getCartItemCount(): number {
    const cart = this.cartSubject.getValue();
    return cart ? cart.totalItems : 0;
  }
}

