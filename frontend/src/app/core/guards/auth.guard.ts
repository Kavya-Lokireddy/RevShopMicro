import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {

    constructor(private authService: AuthService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        const currentUser = this.authService.currentUser;

        if (currentUser) {
            // Check if route is restricted by role
            const expectedRole = route.data['role'];

            if (expectedRole && currentUser.role !== expectedRole) {
                // role not authorized, redirect to their respective home
                if (currentUser.role === 'SELLER') {
                    this.router.navigate(['/seller/dashboard']);
                } else {
                    this.router.navigate(['/buyer/dashboard']);
                }
                return false;
            }
            return true; // Authorized
        }

        // not logged in so redirect to login page
        this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
    }
}
