# 🎨 Frontend Architecture (Angular)

## 1. Angular Project Structure

```
frontend/src/app/
├── app.module.ts                    # Root module: declares all components, imports modules
├── app-routing.module.ts            # All route definitions with guards
├── app.component.ts                 # Root component: navbar, notifications, routing
├── app.component.html               # Main layout with navigation bar
├── app.component.css                # Global component styles
│
├── core/                            # Singleton services and infrastructure
│   ├── guards/
│   │   └── auth.guard.ts            # Route guard: checks JWT + role
│   ├── interceptors/
│   │   └── auth.interceptor.ts      # Attaches Bearer token to every HTTP request
│   └── services/
│       ├── auth.service.ts          # Login, register, JWT management
│       ├── product.service.ts       # Product CRUD + search
│       ├── cart.service.ts          # Cart operations with product enrichment
│       ├── checkout.service.ts      # Checkout session + payment processing
│       ├── order.service.ts         # Orders CRUD + status management
│       ├── review.service.ts        # Product reviews
│       ├── favorite.service.ts      # Wishlist management
│       └── notification.service.ts  # In-app notifications
│
├── features/                        # Feature modules (lazy-loaded & eager)
│   ├── auth/                        # Login, Register, Forgot Password
│   ├── buyer/                       # Buyer Dashboard, Product List, Product Details, Search
│   ├── cart/                        # Shopping Cart page
│   ├── checkout/                    # Checkout form (address, order summary)
│   ├── payment/                     # Payment method selection + processing
│   ├── order-confirmation/          # Post-payment success screen
│   ├── orders/                      # Order History list
│   ├── favorites/                   # Wishlist page
│   ├── reviews/                     # Review components
│   └── seller-product/              # Seller Dashboard (lazy-loaded module)
│       ├── seller-product.module.ts
│       ├── pages/seller-dashboard/
│       ├── components/product-form/
│       └── components/product-list/
│
└── model/                           # Shared interfaces/models
```

---

## 2. Routing Configuration

All routes are defined in `app-routing.module.ts`:

| Path                    | Component                | Guard    | Role   | Description               |
|-------------------------|--------------------------|----------|--------|---------------------------|
| `/`                     | Redirects to `/login`    | -        | -      | Default redirect          |
| `/login`                | AuthPageComponent        | -        | -      | Login + Registration      |
| `/forgot-password`      | ForgotPasswordComponent  | -        | -      | Password reset            |
| `/buyer/dashboard`      | ProductListComponent     | AuthGuard| BUYER  | Product catalog           |
| `/buyer/search`         | ProductSearchComponent   | AuthGuard| BUYER  | Keyword search            |
| `/buyer/product/:id`    | ProductDetailsComponent  | AuthGuard| BUYER  | Product detail page       |
| `/cart`                 | CartComponent            | AuthGuard| BUYER  | Shopping cart              |
| `/checkout`             | CheckoutPageComponent    | AuthGuard| BUYER  | Checkout form             |
| `/payment`              | PaymentPageComponent     | AuthGuard| BUYER  | Payment processing        |
| `/order-confirmation`   | OrderConfirmationComponent| AuthGuard| BUYER | Order success page        |
| `/orders`               | OrderListComponent       | AuthGuard| BUYER  | Order history             |
| `/favorites`            | FavoritesComponent       | AuthGuard| BUYER  | Wishlist                  |
| `/seller/dashboard`     | SellerDashboardComponent | AuthGuard| SELLER | Seller dashboard (lazy)   |

**Lazy Loading:**
The Seller module is lazy-loaded to reduce initial bundle size for buyers:
```typescript
{
  path: 'seller/dashboard',
  loadChildren: () =>
    import('./features/seller-product/seller-product.module')
      .then(m => m.SellerProductModule)
}
```

---

## 3. Authentication Flow

### 3.1 Auth Guard (`auth.guard.ts`)
Protects routes by checking:
1. Is the user logged in? (has valid JWT token)
2. Does the user have the correct role? (BUYER vs SELLER)

```typescript
canActivate(route, state): boolean {
  const currentUser = this.authService.currentUser;
  if (currentUser) {
    const expectedRole = route.data['role'];
    if (expectedRole && currentUser.role !== expectedRole) {
      // Redirect to their own dashboard
      return false;
    }
    return true;
  }
  // Not logged in → redirect to /login
  this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
}
```

### 3.2 Auth Interceptor (`auth.interceptor.ts`)
Automatically attaches the JWT token to every outgoing HTTP request:
```typescript
intercept(req, next) {
  const token = localStorage.getItem("token");
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next.handle(req);
}
```

### 3.3 Auth Service (`auth.service.ts`)
- Uses a `BehaviorSubject<User | null>` to share logged-in user state across all components
- On page reload, decodes the JWT from localStorage to restore the user session
- Login response stores: `token`, `userId`, `name`, `email`, `role`

**Login Flow:**
```
User enters email/password
  → POST /api/auth/login
  → Backend validates credentials with BCrypt
  → Returns JWT + user info
  → Frontend stores JWT in localStorage
  → BehaviorSubject emits user → all subscribed components update
  → Router redirects to /buyer/dashboard or /seller/dashboard
```

---

## 4. Key Services (Frontend)

### 4.1 Product Service
- **Buyer methods**: `getAllProducts()`, `getProductDetails()`, `searchProducts()`, `getProductsByCategory()`
- **Seller methods**: `addProduct()`, `updateProduct()`, `deleteProduct()`, `getSellerProducts()`
- **Data normalization**: Backend responses are normalized into a consistent `Product` interface using `normalizeProduct()`

### 4.2 Cart Service
- Manages cart CRUD via `/api/cart` endpoints
- **Cart Enrichment**: After fetching cart from backend, calls `POST /api/products/batch` to enrich cart items with product descriptions, MRP, discount, and stock info
- Uses `BehaviorSubject<CartResponse>` to share cart state across navbar badge and cart page

### 4.3 Checkout Service
- **3-step flow**: `initiateCheckout()` → `addAddress()` → `processPayment()`
- Supports COD, Credit Card, Debit Card payment methods
- For cards, sends dummy card details (simulated payment)

### 4.4 Order Service
- **Data normalization**: Backend returns `id` for orders; frontend normalizes it to `orderId` for consistent template usage
- Supports buyer-side order listing (`/my`), seller-side order listing (`/seller`), status updates, and cancellation

### 4.5 Review Service
- Submit reviews after order delivery
- Aggregates reviews for seller dashboard using `forkJoin` to fetch reviews for all seller products in parallel

### 4.6 Notification Service
- Fetches notifications from backend
- Supports marking individual notifications as read
- Displayed in a dropdown in the navbar

---

## 5. Component Architecture (Key Components)

### 5.1 App Component (Root)
- Contains the **navigation bar** with conditional links for Buyer vs Seller
- Shows **notification dropdown** with unread count badge
- Shows **cart count badge** in the navbar
- Subscribes to `authService.currentUser$` to reactively show/hide elements

### 5.2 Seller Dashboard
- **3 tabs**: My Catalog | Manage Orders | Customer Reviews
- **Widgets**: Total Sales, Total Orders, Low Stock Alerts
- **Product Form**: Inline form for adding/editing products
- **Order Table**: Shows orders with status dropdown to update status
- **Reviews**: Aggregated customer reviews from all seller products

### 5.3 Buyer Product List
- Paginated grid of products
- Category filter sidebar
- "Add to Cart" and "Add to Favorites" buttons on each card
- Links to product detail page

### 5.4 Cart Component
- Shows cart items with quantity controls (+/- buttons and manual input)
- Displays price, MRP, discount percentage, and stock availability
- Order Summary sidebar with checkout button

### 5.5 Checkout → Payment → Confirmation Flow
1. **Checkout**: Prefills buyer name/phone from localStorage, shows order summary, collects shipping address
2. **Payment**: Selects payment method (COD/Card), processes payment
3. **Confirmation**: Shows success with Order ID, payment method, and status

---

## 6. State Management Pattern

```
                    ┌──────────────────────────┐
                    │    BehaviorSubject<T>     │
                    │  (Services as Stores)     │
                    └────────────┬─────────────┘
                                 │
            ┌────────────────────┼────────────────────┐
            │                    │                    │
            ▼                    ▼                    ▼
     ┌────────────┐     ┌────────────┐      ┌────────────┐
     │ Component A │     │ Component B │      │ Component C │
     │ (subscribes)│     │ (subscribes)│      │ (subscribes)│
     └────────────┘     └────────────┘      └────────────┘
```

- `AuthService.currentUser$` → User state across navbar, guards, components
- `CartService.cart$` → Cart data shared between cart page and navbar badge
- No external state management library (NgRx) — BehaviorSubjects suffice for this project scope

---

## 7. Environment Configuration

```typescript
// environment.ts (Development)
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
```

All services use `environment.apiBaseUrl` as the base URL, ensuring a single point of configuration change.
