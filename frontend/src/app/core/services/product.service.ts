import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../../features/seller-product/models/product.model';
import { Observable, map } from 'rxjs';
import { AuthService } from './auth.service';

export interface Category {
  id: number;
  name: string;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private buyerUrl = '/api/products';
  private sellerUrl = '/api/seller/products';
  private categoryUrl = '/api/categories';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  // ===== Kavya's Seller Methods =====

  // addProduct(product: Product): Observable<Product> {
  //   product.categoryId = 1;
  //   return this.http.post<any>(this.baseUrl, product)
  //     .pipe(map((saved) => this.normalizeProduct(saved)));
  // }

  addProduct(product: Product): Observable<Product> {

  const headers = {
    'X-User-Id': String(this.authService.currentUser?.id ?? 0)
  };

  const payload = {
    name: product.name,
    description: product.description,
    price: product.price,
    mrp: product.mrp,
    quantity: product.quantity,
    categoryId: Number(product.categoryId)
  };

  return this.http.post<any>(this.sellerUrl, payload, { headers })
    .pipe(map((saved) => this.normalizeProduct(saved)));
}

  updateProduct(id: number, product: Product): Observable<Product> {
    const headers = {
      'X-User-Id': String(this.authService.currentUser?.id ?? 0)
    };
    const payload = {
      name: product.name,
      description: product.description,
      price: product.price,
      mrp: product.mrp,
      quantity: product.quantity,
      categoryId: Number(product.categoryId),
      active: product.active ?? true
    };
    return this.http.put<any>(`${this.sellerUrl}/${id}`, payload, { headers })
      .pipe(map((saved) => this.normalizeProduct(saved)));
  }

  deleteProduct(id: number) {
    const headers = {
      'X-User-Id': String(this.authService.currentUser?.id ?? 0)
    };
    return this.http.delete(`${this.sellerUrl}/${id}`, { headers });
  }

  getAllProducts(): Observable<Product[]> {
    return this.http.get<any>(this.buyerUrl)
      .pipe(map((response) => (response?.products || []).map((p: any) => this.normalizeProduct(p))));
  }

  setThreshold(id: number, threshold: number) {
    const headers = {
      'X-User-Id': String(this.authService.currentUser?.id ?? 0)
    };
    return this.http.put(
      `${this.sellerUrl}/${id}/threshold`,
      { threshold },
      { headers }
    );
  }

  private normalizeProduct(product: any): Product {
    return {
      id: product.id,
      name: product.name,
      description: product.description,
      price: product.price,
      mrp: product.mrp,
      discountPercentage: product.discountPercentage,
      category: product.category ?? (product.categoryName ? { name: product.categoryName } : ''),
      categoryName: product.categoryName ?? product.category?.name ?? '',
      categoryId: product.categoryId ?? product.category?.id ?? 1,
      quantity: product.quantity,
      sellerId: product.sellerId ?? product.seller?.id ?? 0,
      active: product.active,
      stockThreshold: product.stockThreshold
    };
  }

  // ===== Jatin's Buyer Methods =====

  getProductsByCategory(categoryId: number, page: number = 0, size: number = 5) {
    return this.http.get<any>(
      `${this.buyerUrl}/category/${categoryId}?page=${page}&size=${size}`
    ).pipe(
      map((response) => ({
        ...response,
        products: (response?.products || []).map((p: any) => this.normalizeProduct(p))
      }))
    );
  }

  searchProducts(keyword: string) {
    return this.http.get<any[]>(`${this.buyerUrl}/search?keyword=${keyword}`);
  }

  getProductDetails(id: number) {
    return this.http.get<any>(`${this.buyerUrl}/${id}`);
  }

  getSellerProducts(sellerId: number) {
    const headers = {
      'X-User-Id': String(sellerId)
    };
    return this.http.get<any[]>(this.sellerUrl, { headers })
      .pipe(map((products) => products.map((p) => this.normalizeProduct(p))));
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.categoryUrl);
  }
}

