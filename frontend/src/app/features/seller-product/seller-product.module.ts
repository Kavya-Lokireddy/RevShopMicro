import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';

import { SellerProductRoutingModule } from './seller-product-routing.module';
import { SellerDashboardComponent } from './pages/seller-dashboard/seller-dashboard.component';
import { ProductFormComponent } from './components/product-form/product-form.component';
import { ProductListComponent } from './components/product-list/product-list.component';

const routes: Routes = [
  {
    path: '',
    component: SellerDashboardComponent
  }
];

@NgModule({
  declarations: [
    SellerDashboardComponent,
    ProductFormComponent,
    ProductListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SellerProductRoutingModule
  ],
exports:[
  SellerDashboardComponent
  ]
})
export class SellerProductModule { }
