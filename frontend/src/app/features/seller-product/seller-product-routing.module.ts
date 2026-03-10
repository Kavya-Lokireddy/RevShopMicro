import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SellerDashboardComponent } from './pages/seller-dashboard/seller-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: SellerDashboardComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SellerProductRoutingModule {}
