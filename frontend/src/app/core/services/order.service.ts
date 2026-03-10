import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

export interface CheckoutAddressRequest {
  shippingAddress: string;
  billingAddress: string;
  contactName: string;
  phoneNumber: string;
}

export interface CheckoutSessionResponse {
  sessionId: number;
  userId: number;
  totalAmount: number;
  status: string;
}

export interface PaymentProcessRequest {
  checkoutSessionId: number;
  paymentMethod: 'COD' | 'CREDIT_CARD' | 'DEBIT_CARD';
}

export interface PaymentProcessResponse {
  transactionId: string;
  status: string;
  orderId: number;
  message: string;
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

export interface OrderResponse {
  orderId: number;
  buyerId: number;
  buyerName: string;
  phoneNumber: string;
  shippingAddress: string;
  billingAddress: string;
  paymentMethod: string;
  paymentStatus: string;
  status: string;
  totalAmount: number;
  orderDate: string;
  items: OrderItemResponse[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly checkoutApi = '/api/checkout';
  private readonly paymentApi = '/api/payment';
  private readonly orderApi = '/api/orders';

  constructor(private http: HttpClient) {}

  initiateCheckout(userId: number, totalAmount: number): Observable<CheckoutSessionResponse> {
    return this.http.post<any>(`${this.checkoutApi}/initiate`, { userId, totalAmount }).pipe(
      map((response) => ({
        sessionId: response.sessionId,
        userId: response.userId,
        totalAmount: response.totalAmount,
        status: response.status
      }))
    );
  }

  addCheckoutAddress(sessionId: number, userId: number, address: CheckoutAddressRequest): Observable<CheckoutSessionResponse> {
    return this.http.put<any>(`${this.checkoutApi}/${sessionId}/address?userId=${userId}`, address).pipe(
      map((response) => ({
        sessionId: response.sessionId,
        userId: response.userId,
        totalAmount: response.totalAmount,
        status: response.status
      }))
    );
  }

  processPayment(payment: PaymentProcessRequest): Observable<PaymentProcessResponse> {
    return this.http.post<PaymentProcessResponse>(`${this.paymentApi}/process`, payment);
  }

  getOrdersByBuyer(buyerId: number): Observable<OrderResponse[]> {
    void buyerId;
    return this.http.get<any[]>(`${this.orderApi}/my`).pipe(
      map((orders) => orders.map((order) => this.normalizeOrder(order)))
    );
  }

  getOrdersBySeller(sellerId: number): Observable<OrderResponse[]> {
    void sellerId;
    return this.http.get<any[]>(`${this.orderApi}/seller`).pipe(
      map((orders) => orders.map((order) => this.normalizeOrder(order)))
    );
  }

  getOrderById(orderId: number): Observable<OrderResponse> {
    return this.http.get<any>(`${this.orderApi}/${orderId}`).pipe(
      map((order) => this.normalizeOrder(order))
    );
  }

  updateOrderStatus(orderId: number, status: string): Observable<OrderResponse> {
    return this.http.put<any>(`${this.orderApi}/${orderId}/status`, { status }).pipe(
      map((order) => this.normalizeOrder(order))
    );
  }

  cancelOrder(orderId: number): Observable<string> {
    return this.http.put(`${this.orderApi}/${orderId}/cancel`, {}, { responseType: 'text' });
  }

  private normalizeOrder(order: any): OrderResponse {
    const statusValue = typeof order.status === 'string'
      ? order.status
      : order.status?.name ?? '';

    return {
      orderId: order.orderId ?? order.id,
      buyerId: order.userId ?? 0,
      buyerName: order.contactName ?? `User #${order.userId}`,
      phoneNumber: order.phoneNumber ?? '',
      status: statusValue,
      totalAmount: order.totalAmount,
      shippingAddress: order.shippingAddress,
      paymentMethod: order.paymentMethod,
      paymentStatus: order.paymentStatus ?? '',
      billingAddress: order.billingAddress ?? '',
      orderDate: order.orderDate ?? order.createdAt,
      items: (order.items || []).map((item: any) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        priceAtPurchase: item.priceAtPurchase,
        subtotal: item.subtotal
      }))
    };
  }
}

