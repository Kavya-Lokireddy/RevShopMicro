# 🎤 Interview Questions & Answers (RevShop Project)

## General Project Questions

### Q1: Tell me about your project.
> "RevShop is a microservices-based e-commerce platform I built with my team. It has an Angular frontend, 6 Spring Boot microservices, an API Gateway, Eureka service discovery, and MySQL. Buyers can browse products, add to cart, checkout with payment, track orders, and leave reviews. Sellers can manage inventory, view orders, and monitor business metrics. We use JWT for authentication, OpenFeign for inter-service communication, and Resilience4j for fault tolerance."

### Q2: Why did you choose microservices over monolith?
> "We chose microservices because the project naturally breaks down into independent bounded contexts — authentication, products, cart, checkout, orders. Each team member owned a specific service, enabling parallel development. Microservices also mean we can scale individual services independently — for example, the product service may need more instances during a sale event while the auth service stays at one instance."

### Q3: What was your specific contribution?
> "I was responsible for the Order Management module — the Order Service backend (REST APIs, JPA entities, notifications, reviews, favorites) and the frontend Order List component with the order-tracking UI. I also integrated the frontend with backend services, resolved CORS issues at the API Gateway level, and set up the Docker Compose configuration."

### Q4: How does your team collaborate on the codebase?
> "We use a mono-repo with a multi-module Maven parent POM. Each team member owns one or more modules. The `app-routing.module.ts` in the frontend clearly marks routes by owner (comments like `// Gotam: Order Management`). We share common interfaces like the `environment.ts` for API base URL and the JWT secret key across services."

---

## Architecture Questions

### Q5: How does service discovery work in your project?
> "We use Netflix Eureka. Each microservice, on startup, registers its name, IP, and port with the Eureka server. When a service needs to call another (e.g., Checkout Service calling Order Service), it asks Eureka for the current address of 'order-service' by name. This means we never hardcode IPs — if we spin up a second instance for load balancing, Eureka automatically distributes requests."

### Q6: How does the API Gateway work?
> "Our Spring Cloud Gateway acts as a reverse proxy. All frontend requests go to `http://localhost:8080/api/*`. The Gateway uses Eureka's service registry to route requests — it automatically maps service names to routes. For example, a request to `/api/auth/login` is routed to whichever instance of `auth-service` is registered in Eureka. The Gateway also handles CORS centrally."

### Q7: How do your microservices communicate with each other?
> "We use synchronous REST-based inter-service communication via Spring Cloud OpenFeign. For example, the Checkout Service declares a Feign interface for `cart-service` and `order-service`. Feign automatically handles service discovery through Eureka, load balancing, and HTTP client creation. We also use Resilience4j circuit breakers on these calls for fault tolerance."

### Q8: What if one of your microservices goes down?
> "We've implemented the Circuit Breaker pattern using Resilience4j. For example, when the Checkout Service calls the Order Service, if the Order Service is down, after a few failed attempts the circuit 'opens' and immediately returns a fallback error response like 'Order service is currently unavailable. Please try again later.' This prevents cascade failures — the checkout service doesn't waste resources waiting for timeouts."

---

## Security Questions

### Q9: How does authentication work?
> "We use JWT-based stateless authentication. When a user logs in, the Auth Service validates the password using BCrypt and generates a JWT containing the userId, email, and role, signed with HMAC-SHA256. The frontend stores this token in localStorage. Every subsequent API request includes the token as a `Bearer` token in the Authorization header. Backend services decode this token to identify the user without storing any session state."

### Q10: How do you handle authorization?
> "We have role-based authorization at two levels. On the frontend, Angular AuthGuard checks the user's role from the JWT before allowing route access — a BUYER can't access `/seller/dashboard` and vice versa. On the backend, Spring Security's `SecurityFilterChain` restricts endpoints. Seller-specific endpoints like `/api/seller/products` require authentication, and the service extracts the sellerId from the JWT to ensure sellers only manage their own products."

### Q11: Explain the CORS issue you faced and how you resolved it.
> "When our Angular frontend (`localhost:4200`) made requests to the API Gateway (`localhost:8080`), the browser blocked them due to CORS. Initially, we added CORS headers in both the API Gateway AND the individual services. This caused a critical bug: the browser received TWO `Access-Control-Allow-Origin` headers — `http://localhost:4200` from the Gateway and `*` from the downstream service. The browser rejects responses with multiple values for this header. We fixed it by centralizing CORS handling ONLY in the API Gateway with a custom `WebFilter` at highest precedence that strips any CORS headers from downstream services before adding the correct single-value header."

---

## Database & Data Questions

### Q12: How is the database structured?
> "We use MySQL 8.0 with a single `revshop_p3` database. Tables are auto-created by Hibernate's `ddl-auto: update` based on JPA entities. We have tables for users, products, categories, carts, cart_items, orders, order_items, checkout_sessions, payment_transactions, reviews, notifications, and favorites. In production microservices, each service would ideally have its own database (database-per-service pattern), but for our project scope, a shared database was practical."

### Q13: How do you handle data consistency across services?
> "Since we use synchronous REST communication, we have eventual consistency. For example, during checkout: the Checkout Service fetches the cart snapshot, saves it in the checkout session, processes payment, then calls the Order Service to create the order, and finally clears the cart. If any step fails, the circuit breaker catches it and the transaction rolls back within the Checkout Service. The cart snapshot ensures we have a consistent view of what the user is buying."

### Q14: What is the cart snapshot pattern?
> "When a user initiates checkout, we capture a JSON snapshot of the entire cart (products, prices, quantities) and store it in the `checkout_sessions` table. This ensures that even if product prices change between 'checkout' and 'payment', the user pays the price they saw. The snapshot is later deserialized to create order items with the exact prices at the time of purchase."

---

## Frontend Questions

### Q15: How do you manage state in the Angular frontend?
> "We use RxJS BehaviorSubjects as lightweight state stores within Angular services. For example, `AuthService` has a `BehaviorSubject<User | null>` that any component can subscribe to. When the user logs in, it emits the user object, and all subscribed components (navbar, guards, etc.) update reactively. Similarly, `CartService` has a `BehaviorSubject<CartResponse>` that keeps the cart count badge in sync. We didn't need NgRx because the state is relatively simple."

### Q16: What is the Auth Interceptor?
> "It's an Angular `HttpInterceptor` that automatically attaches the JWT token to every outgoing HTTP request. When any service calls `this.http.get('/api/products')`, the interceptor clones the request and adds `Authorization: Bearer <token>` to the headers. This means individual service calls don't need to manually attach tokens."

### Q17: How does the Auth Guard work?
> "The AuthGuard implements `CanActivate` and is applied to protected routes. Before navigating to a route, it checks: (1) Is there a current user? If not, redirect to login with a returnUrl. (2) Does the route require a specific role? If the user's role doesn't match, redirect them to their own dashboard (buyer → buyer dashboard, seller → seller dashboard)."

### Q18: Why do you use lazy loading for the Seller module?
> "The Seller Dashboard module is lazy-loaded using Angular's `loadChildren`. This means when a Buyer logs in, the seller module's JavaScript code isn't downloaded at all — reducing the initial bundle size significantly. The module is only loaded when a user navigates to `/seller/dashboard`, improving first-load performance for the majority of users (buyers)."

---

## Scenario-Based Questions

### Q19: A customer adds a product to cart but the product goes out of stock before checkout. What happens?
> "When the customer proceeds to checkout, the Checkout Service fetches the latest cart from the Cart Service, which includes current stock information. During order creation, the Order Service calls the Product Service to validate current prices and availability. If the product is out of stock, the Order Service throws an `IllegalStateException('Product not found')` and the payment transaction is marked as FAILED, showing the user an error message."

### Q20: What happens if a user's checkout session expires?
> "Checkout sessions have a 30-minute TTL set via `@PrePersist`. When the user tries to pay, we call `session.isExpired()` which compares `LocalDateTime.now()` with `expiresAt`. If expired, we throw `CheckoutExpiredException`, which the `GlobalExceptionHandler` converts to a `410 GONE` response. The user sees a message to start checkout again."

### Q21: How do you prevent a seller from modifying another seller's products?
> "Each seller's API calls go through the Auth Interceptor which attaches their JWT. The Product Service's seller endpoints extract the `sellerId` from the JWT and only return/modify products where `product.sellerId == authenticatedSellerId`. A seller can never see or edit another seller's products because the query filters by their ID."

### Q22: What happens if the user double-clicks the 'Pay Now' button?
> "On the frontend, we disable the button while `isProcessing` is true. Once the first click triggers the payment API call, the button becomes disabled. On the backend, we also check `session.getStatus() == CheckoutStatus.COMPLETED` — if payment was already processed, we throw `IllegalArgumentException('Payment already completed for this session')`, preventing duplicate charges."

### Q23: How do you handle the case where the Order Service is down after payment?
> "This is exactly why we have the Circuit Breaker. If the Order Service is unresponsive, the circuit breaker in the Checkout Service trips after a few failures. The payment transaction is saved with `PaymentStatus.FAILED` and an error message. The user sees 'Order service is currently unavailable. Please try again later.' In a production system, we'd add a message queue (like RabbitMQ or Kafka) to decouple payment from order creation with eventual consistency."

### Q24: Can a buyer review any product?
> "No. The review system validates that the buyer has actually purchased the product. On the frontend, the 'Rate & Review' button only appears for products within `DELIVERED` orders. The backend can additionally verify that the reviewing user's ID matches an order that contains the product."

### Q25: How would you scale this system for high traffic?
> "Several approaches: (1) Run multiple instances of high-traffic services (Product, Cart) behind Eureka's load balancing. (2) Add Redis caching for product catalog and cart data. (3) Use a message queue (RabbitMQ/Kafka) for async operations like notifications and order processing. (4) Implement database-per-service with eventual consistency. (5) Add a CDN for product images. (6) Use Kubernetes for container orchestration with auto-scaling."

---

## Design Pattern Questions

### Q26: What design patterns did you use?
| Pattern               | Where Used                                                |
|-----------------------|-----------------------------------------------------------|
| **API Gateway**       | Spring Cloud Gateway for single entry point               |
| **Service Registry**  | Eureka for service discovery                              |
| **Circuit Breaker**   | Resilience4j on inter-service calls                       |
| **Repository Pattern**| Spring Data JPA repositories for data access              |
| **DTO Pattern**       | Request/Response DTOs to decouple API contracts from entities |
| **Singleton Pattern** | Angular services with `providedIn: 'root'`                |
| **Observer Pattern**  | RxJS BehaviorSubject for reactive state management        |
| **Interceptor Pattern**| Angular HttpInterceptor for adding auth headers           |
| **Guard Pattern**     | Angular AuthGuard for route protection                    |
| **Builder Pattern**   | Order construction with order items                       |
| **Strategy Pattern**  | Different payment processing strategies (COD vs Card)     |
| **Facade Pattern**    | Checkout Service orchestrating cart + payment + orders     |

---

## Technical Deep-Dive Questions

### Q27: Walk me through the complete code flow for placing an order.
> 1. **Frontend** (CheckoutPageComponent): User fills address → calls `checkoutService.initiateCheckout(userId, totalAmount)`
> 2. **Checkout Service** (`POST /api/checkout/initiate`): Calls `cartServiceClient.getCart(userId)` via Feign → validates cart not empty → serializes cart to JSON snapshot → saves `CheckoutSession` with status `INITIATED`
> 3. **Frontend** (CheckoutPageComponent): Receives sessionId → calls `checkoutService.addAddress(sessionId, userId, addressPayload)`
> 4. **Checkout Service** (`PUT /api/checkout/{id}/address`): Updates session with shipping address → status becomes `ADDRESS_ADDED`
> 5. **Frontend** (PaymentPageComponent): User selects payment method → calls `checkoutService.processPayment(sessionId, paymentMethod)`
> 6. **Checkout Service** (`POST /api/payment/process`): Validates session (not expired, not already paid, has address) → creates `PaymentTransaction` → processes payment (COD always succeeds, cards validate card number) → calls `orderServiceClient.createOrder(request)` via Feign
> 7. **Order Service** (`POST /api/orders`): Receives order request → for each item, calls `productServiceClient.getProductById(id)` to get real-time prices → creates `Order` entity with `OrderItem` entities → saves to MySQL → creates notifications for buyer and sellers → returns orderId
> 8. **Checkout Service**: Receives orderId → saves to payment transaction → calls `cartServiceClient.clearCart(userId)` → returns `{transactionId, orderId, status: COMPLETED}`
> 9. **Frontend** (PaymentPageComponent): Receives response → navigates to `/order-confirmation?orderId=X&paymentMethod=COD`
> 10. **Frontend** (OrderConfirmationComponent): Reads query params → displays success with order ID

### Q28: How does the product discount percentage get calculated?
> "The `Product` entity has a `@PrePersist` and `@PreUpdate` lifecycle hook. When a product is saved or updated, if both `mrp` (maximum retail price) and `price` (selling price) are set, it automatically calculates: `discountPercentage = ((mrp - price) / mrp) * 100`. This ensures the discount is always accurate and consistent."

### Q29: Explain the cart enrichment pattern on the frontend.
> "The Cart Service backend only stores basic cart item data (productId, quantity, price, name). When the frontend loads the cart, it calls the `/api/cart` endpoint which returns minimal data. Then the `enrichCart()` method in the frontend CartService calls `POST /api/products/batch` with all product IDs to fetch full product details (description, MRP, discount, stock level, image). It merges this data into the cart items for display. This keeps the cart service lightweight while giving the UI rich product information."
