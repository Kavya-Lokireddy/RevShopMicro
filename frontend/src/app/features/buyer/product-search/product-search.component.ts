import { Component } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';

@Component({
 selector:'app-product-search',
 templateUrl:'./product-search.component.html'
})
export class ProductSearchComponent {

  keyword: any = '';
  results:any[]=[];

  constructor(private service:ProductService){}

  search(){
    this.service.searchProducts(this.keyword)
      .subscribe(res=>this.results=res);
  }
}
