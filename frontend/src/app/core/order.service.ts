import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CheckoutRequest {
  userId: number;
  totalAmount: number;
}

export interface PaymentRequest {
  checkoutSessionId: number;
  paymentMethod: 'COD' | 'CREDIT_CARD' | 'DEBIT_CARD';
}

export interface OrderResponse {
  orderId: number;
  message: string;
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED' | string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly checkoutApi = '/api/checkout';
  private readonly paymentApi = '/api/payment';

  constructor(private http: HttpClient) { }

  createOrder(order: CheckoutRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.checkoutApi}/initiate`, order);
  }

  makePayment(payment: PaymentRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.paymentApi}/process`, payment);
  }
}

