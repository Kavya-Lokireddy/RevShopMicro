import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Favorite {
    id: number;
    productId: number;
    productName: string;
    productPrice: number;
    productImage: string;
    addedAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class FavoriteService {

    private API = '/api/favorites';

    constructor(private http: HttpClient) { }

    private buildUserHeader(userId: number) {
        return {
            headers: new HttpHeaders({
                'X-User-Id': String(userId)
            })
        };
    }

    addFavorite(buyerId: number, productId: number): Observable<Favorite> {
        const options = this.buildUserHeader(buyerId);
        return this.http.post<Favorite>(`${this.API}/${productId}`, {}, options);
    }

    removeFavorite(buyerId: number, productId: number): Observable<void> {
        const options = this.buildUserHeader(buyerId);
        return this.http.delete<void>(`${this.API}/${productId}`, options);
    }

    getFavoritesByBuyer(buyerId: number): Observable<Favorite[]> {
        const options = this.buildUserHeader(buyerId);
        return this.http.get<Favorite[]>(this.API, options);
    }

    isFavorite(buyerId: number, productId: number): Observable<{ isFavorite: boolean }> {
        const options = this.buildUserHeader(buyerId);
        return this.http.get<{ isFavorite: boolean }>(`${this.API}/${productId}/check`, options);
    }
}

