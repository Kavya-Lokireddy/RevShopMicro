import { Component, Input, OnChanges, OnInit, Output, EventEmitter } from '@angular/core';
import { Category, ProductService } from '../../../../core/services/product.service';
import { Product } from '../../models/product.model';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit, OnChanges {

  @Input() product:Product|null=null;         // product to edit, if any
  @Output() onSaved = new EventEmitter<void>(); // notify list after save

  form: Product = this.emptyForm();
  categories: Category[] = [];
  categoriesLoadFailed = false;

  constructor(private productService: ProductService,private authService:AuthService) {}

  ngOnInit() {
    this.loadCategories();
  }

  ngOnChanges() {
    // Copy input product to local form or reset if adding
    this.form = this.product ? { ...this.product } : this.emptyForm();
    this.form.categoryId = Number(this.form.categoryId ?? this.product?.categoryId ?? 1);
  }

  private loadCategories() {
    this.productService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories || [];
        this.categoriesLoadFailed = false;
      },
      error: () => {
        this.loadCategoriesFromProductsFallback();
      }
    });
  }

  private loadCategoriesFromProductsFallback() {
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        const categoryMap = new Map<number, string>();

        products.forEach((product) => {
          const categoryId = Number(product.categoryId);
          if (!Number.isFinite(categoryId) || categoryId <= 0) {
            return;
          }
          const categoryName = String(product.categoryName || product.category?.name || `Category ${categoryId}`);
          if (!categoryMap.has(categoryId)) {
            categoryMap.set(categoryId, categoryName);
          }
        });

        this.categories = Array.from(categoryMap.entries())
          .map(([id, name]) => ({ id, name }))
          .sort((a, b) => a.id - b.id);

        this.categoriesLoadFailed = this.categories.length === 0;
      },
      error: () => {
        this.categories = [];
        this.categoriesLoadFailed = true;
      }
    });
  }

 save() {
     if (!this.authService.currentUser) {
       alert("Please login first!");
       return;
     }

     // Ensure sellerId is always current user
     this.form.sellerId = this.authService.currentUser.id;

     if (this.form.id ){
       // Update existing product
       this.productService.updateProduct(this.form.id, this.form)
         .subscribe({
           next: () => this.onSaveSuccess('Updated!'),
           error: (err) => this.onSaveError(err)
         });
     } else {
       // Add new product
       this.productService.addProduct(this.form)
       .subscribe({
         next: () => this.onSaveSuccess('Added!'),
         error: (err) => this.onSaveError(err)
       });
     }
   }

  private onSaveSuccess(message: string) {
     alert(message);
     this.onSaved.emit();
     this.form = this.emptyForm();
   }

  private onSaveError(err: any) {
    const message = err?.error?.message || err?.error || 'Failed to save product';
    alert(message);
    console.error('Product save failed', err);
  }


  private emptyForm(): Product {
    return {
        id: 0, // ensure 0 or undefined for new product
      name: '',
      description: '',
      price: 0,
      mrp: 0,
      category: '',
      quantity: 0,
      categoryId: 1,
      sellerId: 0,
      discountPercentage: 0,
      stockThreshold: 5
    };
  }
}
