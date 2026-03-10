import { Component, OnInit,Input,Output,EventEmitter } from '@angular/core';
import { ProductService } from '../../../../core/services/product.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent{

  @Input() products: Product[] = [];
  @Output() editProduct = new EventEmitter<Product>();
  @Output() productDeleted = new EventEmitter<void>();
//   selectedProduct: Product | null = null;

  constructor(private productService: ProductService) {}


  edit(product: Product) {
    this.editProduct.emit(product);
  }

    delete(id?: number) {
      if (!id) return;
      if (confirm('Are you sure you want to delete this product?')) {
        this.productService.deleteProduct(id).subscribe({
          next: () => {
            this.products = this.products.filter(p => p.id !== id);
            alert('Deleted!');
            this.productDeleted.emit();
          },
          error: (err) => {
            const message = err?.error?.message || err?.error || 'Failed to delete product';
            alert(message);
            console.error('Product delete failed', err);
          }
        });
      }
    }

}
