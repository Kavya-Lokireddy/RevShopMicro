import { NgModule, Injectable } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import {
  HttpClientModule,
  HTTP_INTERCEPTORS,
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, catchError, throwError } from 'rxjs';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthPageComponent } from './features/auth/auth-page/auth-page.component';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password.component';

// Kavya's module
import { SellerProductModule } from './features/seller-product/seller-product.module';

// Gotam's components
import { OrderListComponent } from './features/orders/order-list/order-list.component';
import { ProductReviewsComponent } from './features/reviews/product-reviews/product-reviews.component';
import { FavoritesComponent } from './features/favorites/favorites/favorites.component';

// Anusha's components
import { CheckoutPageComponent } from './features/checkout/checkout-page/checkout-page.component';
import { PaymentPageComponent } from './features/payment/payment-page/payment-page.component';
import { OrderConfirmationComponent } from './features/order-confirmation/order-confirmation.component';

// Jatin's components
import { ProductListComponent } from './features/buyer/product-list/product-list.component';
import { ProductSearchComponent } from './features/buyer/product-search/product-search.component';
import { ProductDetailsComponent } from './features/buyer/product-details/product-details.component';

// Pavan's components
import { CartComponent } from './features/cart/cart-page/cart.component';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    const token = localStorage.getItem("token");
    const isAuthEndpoint = req.url.includes('/api/auth/');

    if (token && !isAuthEndpoint) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !isAuthEndpoint) {
          localStorage.removeItem('token');
        }
        return throwError(() => error);
      })
    );
  }
}

@NgModule({
  declarations: [
    AppComponent,
    AuthPageComponent,
    ForgotPasswordComponent,
    OrderListComponent,
    ProductReviewsComponent,
    FavoritesComponent,
    CheckoutPageComponent,
    PaymentPageComponent,
    OrderConfirmationComponent,
    ProductListComponent,
    ProductSearchComponent,
    ProductDetailsComponent,
    CartComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    SellerProductModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
