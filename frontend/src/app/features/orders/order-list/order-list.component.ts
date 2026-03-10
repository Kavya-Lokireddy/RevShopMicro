import { Component, OnInit } from '@angular/core';
import { OrderService, OrderResponse } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { ReviewService } from '../../../core/services/review.service';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-order-list',
    templateUrl: './order-list.component.html',
    styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {

    orders: OrderResponse[] = [];
    loading = true;
    errorMessage = '';
    buyerId!: number;
    reviewedProductIds = new Set<number>();

    activeReviewKey: string | null = null;
    reviewDraft = {
        rating: 5,
        comment: ''
    };
    reviewError = '';
    reviewSuccess = '';

    constructor(
        private orderService: OrderService,
        private authService: AuthService,
        private reviewService: ReviewService
    ) { }

    ngOnInit(): void {
        this.authService.currentUser$.subscribe(user => {
            if (user) {
                this.buyerId = user.id;
                this.loadOrders();
            }
        });
    }

    loadOrders(): void {
        this.loading = true;
        forkJoin({
            orders: this.orderService.getOrdersByBuyer(this.buyerId),
            reviews: this.reviewService.getReviewsByBuyer(this.buyerId)
        }).subscribe({
            next: ({ orders, reviews }) => {
                this.orders = orders;
                this.reviewedProductIds = new Set(
                    reviews
                        .map(review => review.product?.id)
                        .filter((productId): productId is number => !!productId)
                );
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = 'Failed to load orders';
                this.loading = false;
                console.error(err);
            }
        });
    }

    cancelOrder(orderId: number): void {
        if (confirm('Are you sure you want to cancel this order?')) {
            this.orderService.cancelOrder(orderId).subscribe({
                next: () => {
                    this.loadOrders();
                },
                error: (err) => {
                    this.errorMessage = err.error || 'Failed to cancel order';
                }
            });
        }
    }

    getReviewKey(orderId: number, productId: number): string {
        return `${orderId}-${productId}`;
    }

    isReviewFormOpen(orderId: number, productId: number): boolean {
        return this.activeReviewKey === this.getReviewKey(orderId, productId);
    }

    openReviewForm(orderId: number, productId: number): void {
        if (this.reviewedProductIds.has(productId)) {
            return;
        }
        this.activeReviewKey = this.getReviewKey(orderId, productId);
        this.reviewDraft = { rating: 5, comment: '' };
        this.reviewError = '';
        this.reviewSuccess = '';
    }

    closeReviewForm(): void {
        this.activeReviewKey = null;
        this.reviewError = '';
    }

    setRating(star: number): void {
        this.reviewDraft.rating = star;
    }

    canReview(order: OrderResponse, productId: number): boolean {
        return order.status !== 'CANCELLED' && !this.reviewedProductIds.has(productId);
    }

    submitReview(orderId: number, item: { productId: number; productName: string }): void {
        this.reviewError = '';
        this.reviewSuccess = '';

        this.reviewService.addReview({
            productId: item.productId,
            orderId,
            rating: this.reviewDraft.rating,
            comment: this.reviewDraft.comment.trim()
        }).subscribe({
            next: () => {
                this.reviewedProductIds.add(item.productId);
                this.reviewSuccess = `Thanks! Your review for "${item.productName}" was submitted.`;
                this.activeReviewKey = null;
            },
            error: (err) => {
                this.reviewError = err.error?.message || err.error || 'Failed to submit review';
                if (this.reviewError.toLowerCase().includes('already reviewed')) {
                    this.reviewedProductIds.add(item.productId);
                    this.activeReviewKey = null;
                } else if (this.reviewError.toLowerCase().includes('purchased')) {
                    this.activeReviewKey = null;
                }
                console.error(`Failed to submit review for order ${orderId}`, err);
            }
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'PENDING': return 'status-pending';
            case 'CONFIRMED': return 'status-confirmed';
            case 'SHIPPED': return 'status-shipped';
            case 'DELIVERED': return 'status-delivered';
            case 'CANCELLED': return 'status-cancelled';
            default: return '';
        }
    }
}
