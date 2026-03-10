import { Component, OnInit } from '@angular/core';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { Product } from '../../models/product.model';
import { ProductService } from '../../../../core/services/product.service';
import { AuthService } from '../../../../core/services/auth.service';
import { OrderResponse, OrderService } from '../../../../core/services/order.service';
import { Review, ReviewService } from '../../../../core/services/review.service';

@Component({
  selector: 'app-seller-dashboard',
  templateUrl: './seller-dashboard.component.html',
  styleUrls: ['./seller-dashboard.component.css']
})
export class SellerDashboardComponent implements OnInit {

  products: Product[] = [];
  selectedProduct: Product | null = null;

  // Widget metrics
  totalSales: number = 0;
  totalOrders: number = 0;
  lowStockItems: Product[] = [];

  // Orders Management
  activeTab: 'PRODUCTS' | 'ORDERS' | 'REVIEWS' = 'PRODUCTS';
  sellerOrders: OrderResponse[] = [];
  sellerReviews: Review[] = [];
  totalReviews = 0;

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private orderService: OrderService,
    private reviewService: ReviewService
  ) { }

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    if (!this.authService.currentUser) {
      alert("Please login first!");
      return;
    }
    const sellerId = this.authService.currentUser.id;

    forkJoin({
      allProducts: this.productService.getSellerProducts(sellerId),
      orders: this.orderService.getOrdersBySeller(sellerId).pipe(catchError(() => of([])))
    }).pipe(
      switchMap(({ allProducts, orders }) =>
        this.reviewService.getReviewsForProductIds(allProducts.map((product) => product.id || 0)).pipe(
          catchError(() => of([])),
          map((reviews) => ({ allProducts, orders, reviews }))
        )
      )
    ).subscribe(({ allProducts, orders, reviews }) => {
      // 1. Process Products
      // this.products = allProducts.filter(p => Number(p.sellerId) === Number(sellerId));
      this.products = allProducts.filter(p => p.active !== false);
      this.lowStockItems = this.products.filter(p => Number(p.quantity) <= Number(p.stockThreshold));

      // 2. Process Orders
      this.totalOrders = orders.length;
      this.sellerOrders = orders;

      this.totalSales = 0;
      orders.forEach(o => {
        o.items.forEach(item => {
          const belongsToSeller = this.products.some(p => p.id === item.productId);
          if (belongsToSeller) {
            this.totalSales += Number(item.subtotal);
          }
        });
      });

      // 3. Process Reviews
      this.sellerReviews = reviews;
      this.totalReviews = reviews.length;
    });
  }

  // loadProducts() {
  //   if (!this.authService.currentUser) return;
  //   const sellerId = this.authService.currentUser.id;
  //   this.productService.getAllProducts().subscribe((allProducts) => {
  //     this.products = allProducts.filter(p => Number(p.sellerId) === Number(sellerId));
  //     this.lowStockItems = this.products.filter(p => Number(p.quantity) <= Number(p.stockThreshold));
  //   });
  // }
  loadProducts() {
  if (!this.authService.currentUser) return;

  const sellerId = this.authService.currentUser.id;

  this.productService.getSellerProducts(sellerId).subscribe((products) => {
    this.products = products.filter(p => p.active !== false);
    this.lowStockItems = this.products.filter(
      p => Number(p.quantity) <= Number(p.stockThreshold)
    );
  });
}

  loadSalesMetrics() {
    this.loadDashboardData();
  }

  edit(product: Product) {
    this.selectedProduct = { ...product };
  }

  clear() {
    this.selectedProduct = null;
  }

  onSaved() {
    console.log("save event received");
    this.selectedProduct = null;
    this.loadProducts();
  }

  updateOrderStatus(orderId: number, status: string) {
    this.orderService.updateOrderStatus(orderId, status).subscribe(() => {
      this.loadSalesMetrics(); // Reload orders efficiently
    });
  }

  // Helper method to set tabs
  setTab(tab: 'PRODUCTS' | 'ORDERS' | 'REVIEWS') {
    this.activeTab = tab;
  }

}
