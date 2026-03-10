import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { CartService } from './core/services/cart.service';
import { AuthService, User } from './core/services/auth.service';
import { NotificationService, Notification } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'frontend';

  searchOpen = false;
  searchQuery = '';

  notifications: Notification[] = [];
  unreadCount = 0;
  showNotifications = false;

  currentUser: User | null = null;

  // Hardcoded for now, same as in CartComponent for the seeder
  private userId!: number;

  isAuthRoute = false;

  constructor(
    public cartService: CartService,
    private router: Router,
    public authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isAuthRoute = event.urlAfterRedirects.includes('/login') || event.urlAfterRedirects.includes('/forgot-password');
      }
    });
  }

  ngOnInit(): void {

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        if (user.role === 'BUYER') {
          // Buyer-only widgets.
          this.cartService.getCart(user.id).subscribe({
            error: () => {
              // Ignore unauthorized cart fetch during app bootstrap.
            }
          });
          this.loadNotifications();
        } else {
          this.notifications = [];
          this.unreadCount = 0;
        }
      } else {
        this.notifications = [];
        this.unreadCount = 0;
      }
    });

  }

  loadNotifications() {
    this.notificationService.getUserNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => !n.read).length;
      },
      error: () => {
        // Ignore unauthorized notification fetch during app bootstrap.
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }

  markAsRead(n: Notification) {
    if (!n.read) {
      this.notificationService.markAsRead(n.id).subscribe(() => {
        n.read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      });
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleSearch(): void {
    if (this.searchOpen && this.searchQuery.trim()) {
      this.doSearch();
    } else {
      this.searchOpen = !this.searchOpen;
    }
  }

  doSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/buyer/search'], {
        queryParams: { q: this.searchQuery.trim() }
      });
      this.searchOpen = false;
      this.searchQuery = '';
    }
  }
}
