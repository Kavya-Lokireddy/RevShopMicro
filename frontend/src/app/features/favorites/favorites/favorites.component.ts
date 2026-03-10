import { Component, OnInit } from '@angular/core';
import { FavoriteService, Favorite } from '../../../core/services/favorite.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-favorites',
    templateUrl: './favorites.component.html',
    styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnInit {

    favorites: Favorite[] = [];
    loading = true;
    errorMessage = '';
    buyerId!: number;

    constructor(
        private favoriteService: FavoriteService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.authService.currentUser$.subscribe(user => {
            if (user) {
                this.buyerId = user.id;
                this.loadFavorites();
            }
        });
    }

    loadFavorites(): void {
        this.loading = true;
        this.favoriteService.getFavoritesByBuyer(this.buyerId).subscribe({
            next: (data) => {
                this.favorites = data;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = 'Failed to load favorites';
                this.loading = false;
            }
        });
    }

    removeFavorite(productId: number): void {
        this.favoriteService.removeFavorite(this.buyerId, productId).subscribe({
            next: () => {
                this.favorites = this.favorites.filter(f => f.productId !== productId);
            },
            error: () => {
                this.errorMessage = 'Failed to remove from favorites';
            }
        });
    }
}
