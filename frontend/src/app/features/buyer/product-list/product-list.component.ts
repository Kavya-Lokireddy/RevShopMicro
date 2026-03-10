import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { FavoriteService, Favorite } from '../../../core/services/favorite.service';
import { OrderService, OrderResponse } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {

  allProducts: any[] = [];
  products: any[] = [];
  page = 0;
  pageSize = 6;
  userId!: number;
  favoritesMap: { [key: number]: boolean } = {};

  // Dashboard Widget Data
  recentOrders: OrderResponse[] = [];
  recentFavorites: Favorite[] = [];

  constructor(
    private service: ProductService,
    private cartService: CartService,
    private favoriteService: FavoriteService,
    private orderService: OrderService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.userId = user.id;
        this.loadProducts();
        this.loadDashboardData();
        this.cartService.getCart(this.userId).subscribe(cart => {
          if (cart && cart.items) {
            cart.items.forEach(item => {
              this.cartQuantities[item.productId] = item.quantity;
            });
          }
        });
      }
    });
  }

  loadDashboardData() {
    this.orderService.getOrdersByBuyer(this.userId).subscribe(orders => {
      this.recentOrders = orders.slice(0, 3); // Top 3 recent
    });
  }

  loadProducts() {
    this.service.getAllProducts().subscribe(res => {
      // Filter out inactive products so buyers don't see them
      this.allProducts = res.filter(p => p.active !== false);
      this.updatePaginatedProducts();
      this.loadFavorites();
    });
  }

  updatePaginatedProducts() {
    const start = this.page * this.pageSize;
    this.products = this.allProducts.slice(start, start + this.pageSize);
  }

  loadFavorites() {
    this.favoriteService.getFavoritesByBuyer(this.userId).subscribe(favorites => {
      this.favoritesMap = {};
      favorites.forEach(f => {
        this.favoritesMap[f.productId] = true;
      });
      this.recentFavorites = favorites.slice(0, 3); // 3 recent favorites
    });
  }

  // Track cart quantities per product
  cartQuantities: { [productId: number]: number } = {};

  addToCart(product: any) {
    this.cartService.addToCart(this.userId, product.id, 1).subscribe({
      next: () => {
        this.cartQuantities[product.id] = (this.cartQuantities[product.id] || 0) + 1;
        this.cartService.getCart(this.userId).subscribe();
      },
      error: (err) => {
        const msg = err?.error?.message || err?.error || 'Could not add to cart';
        alert('⚠️ ' + msg);
      }
    });
  }

  decrementFromCart(product: any) {
    const current = this.cartQuantities[product.id] || 0;
    this.cartService.cart$.subscribe(cart => {
      const item = cart?.items.find(i => i.productId === product.id);
      if (item) {
        if (current <= 1) {
          this.cartService.removeFromCart(this.userId, item.cartItemId).subscribe(() => {
            delete this.cartQuantities[product.id];
            this.cartService.getCart(this.userId).subscribe();
          });
        } else {
          this.cartService.updateCartItemQuantity(this.userId, item.cartItemId, current - 1).subscribe(() => {
            this.cartQuantities[product.id] = current - 1;
            this.cartService.getCart(this.userId).subscribe();
          });
        }
      }
    }).unsubscribe();
  }

  toggleFavorite(product: any) {
    const isFav = this.favoritesMap[product.id];
    if (isFav) {
      this.favoriteService.removeFavorite(this.userId, product.id).subscribe(() => {
        this.favoritesMap[product.id] = false;
      });
    } else {
      this.favoriteService.addFavorite(this.userId, product.id).subscribe(() => {
        this.favoritesMap[product.id] = true;
      });
    }
  }

  next() {
    if ((this.page + 1) * this.pageSize < this.allProducts.length) {
      this.page++;
      this.updatePaginatedProducts();
    }
  }

  prev() {
    if (this.page > 0) {
      this.page--;
      this.updatePaginatedProducts();
    }
  }
}
