import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, forkJoin, map, of } from 'rxjs';

export interface ReviewRequest {
    productId: number;
    orderId: number;
    rating: number;
    comment: string;
}

export interface Review {
    id: number;
    buyer: {
        id: number;
        name: string;
    };
    product: {
        id: number;
        name: string;
    };
    rating: number;
    comment: string;
    createdAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class ReviewService {

    private API = '/api/reviews';

    constructor(private http: HttpClient) { }

    addReview(review: ReviewRequest): Observable<Review> {
        return this.http.post<any>(this.API, review).pipe(
            map((response) => this.normalizeReview(response))
        );
    }

    getReviewsByProduct(productId: number): Observable<Review[]> {
        return this.http.get<any>(`${this.API}/product/${productId}`).pipe(
            map((response) => (response?.reviews || []).map((review: any) => this.normalizeReview(review)))
        );
    }

    getReviewsBySeller(sellerId: number): Observable<Review[]> {
        void sellerId;
        return of([]);
    }

    getReviewsByBuyer(buyerId: number): Observable<Review[]> {
        void buyerId;
        return this.http.get<any[]>(`${this.API}/my`).pipe(
            map((reviews) => reviews.map((review) => this.normalizeReview(review)))
        );
    }

    getAverageRating(productId: number): Observable<{ averageRating: number }> {
        return this.http.get<{ averageRating: number }>(`${this.API}/product/${productId}/average`);
    }

    deleteReview(reviewId: number): Observable<string> {
        return this.http.delete(`${this.API}/${reviewId}`, { responseType: 'text' });
    }

    getReviewsForProductIds(productIds: number[]): Observable<Review[]> {
        const uniqueProductIds = Array.from(new Set(productIds.filter((id) => Number.isFinite(id))));
        if (uniqueProductIds.length === 0) {
            return of([]);
        }

        return forkJoin(uniqueProductIds.map((id) => this.getReviewsByProduct(id))).pipe(
            map((reviewGroups) => reviewGroups.flat())
        );
    }

    private normalizeReview(review: any): Review {
        return {
            id: review.id,
            buyer: {
                id: review.buyer?.id ?? review.userId,
                name: review.buyer?.name ?? `User #${review.userId}`
            },
            product: {
                id: review.product?.id ?? review.productId,
                name: review.product?.name ?? `Product #${review.productId}`
            },
            rating: review.rating,
            comment: review.comment,
            createdAt: review.createdAt
        };
    }
}

