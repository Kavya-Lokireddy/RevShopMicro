# 🏗️ Backend Microservices Architecture

## 1. Architecture Diagram (Service Communication)

```
                         ┌──────────────┐
                         │   Angular    │
                         │  Frontend    │
                         │ (Port 4200)  │
                         └──────┬───────┘
                                │ HTTP (REST)
                                ▼
                    ┌───────────────────────┐
                    │     API Gateway       │
                    │    (Port 8080)        │
                    │  Spring Cloud Gateway │
                    │  CORS + Routing       │
                    └──────────┬────────────┘
                               │
              ┌────────────────┼─────────────────────┐
              │                │                     │
              ▼                ▼                     ▼
    ┌─────────────┐  ┌──────────────┐   ┌──────────────────┐
    │ Auth Service │  │Product Service│  │   Cart Service    │
    │  (8081)      │  │  (8082)       │  │    (8083)         │
    └─────────────┘  └──────────────┘   └──────────────────┘
                               │                     │
                               │                     │
              ┌────────────────┼─────────────────────┤
              │                │                     │
              ▼                ▼                     ▼
    ┌─────────────┐  ┌──────────────┐   ┌──────────────────┐
    │Order Service │  │Checkout Svc  │   │ Config Server    │
    │  (8084)      │  │  (8085)      │   │    (8888)        │
    └─────────────┘  └──────────────┘   └──────────────────┘
              │                │
              └────────┬───────┘
                       ▼
              ┌──────────────┐
              │  Eureka      │
              │ Discovery    │
              │ (Port 8761)  │
              └──────────────┘
                       │
                       ▼
              ┌──────────────┐
              │   MySQL 8.0  │
              │ (Port 3306)  │
              │  revshop_p3  │
              └──────────────┘
```

---

## 2. Service-by-Service Deep Dive

### 2.1 Discovery Server (Eureka) – Port 8761
**Purpose**: Netflix Eureka server that acts as a **service registry**. Every microservice registers itself here on startup. When a service (e.g. checkout-service) needs to communicate with another (e.g. order-service), it asks Eureka for the current IP/port instead of hardcoding it.

**Why it matters (Interview answer):**
> "We use Eureka for dynamic service discovery. Instead of hardcoding service URLs, each service registers itself at startup and looks up other services by name. This enables horizontal scaling — we can run multiple instances of any service and Eureka will load-balance across them."

**Key Config** (`application.yml`):
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

---

### 2.2 API Gateway – Port 8080
**Purpose**: Single entry point for ALL frontend requests. Routes requests to the appropriate microservice like a traffic cop.

**Key Responsibilities:**
1. **Routing**: `/api/auth/**` → Auth Service, `/api/products/**` → Product Service, etc.
2. **CORS Handling**: Custom `WebFilter` at `HIGHEST_PRECEDENCE` that adds CORS headers and strips duplicates from downstream services.
3. **Header Deduplication**: `DedupeResponseHeader` filter prevents duplicate `Access-Control-Allow-Origin` headers.

**CORS Solution (Important Interview Topic):**
```java
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
public WebFilter corsFilter() {
    return (exchange, chain) -> {
        // For OPTIONS preflight → respond immediately with CORS headers
        // For actual requests → strip downstream CORS headers, add our own
        // This prevents the "multiple Access-Control-Allow-Origin" browser error
    };
}
```

**Why Gateway-level CORS?**
> "CORS must be handled at a single point. If both the gateway AND downstream services add CORS headers, browsers reject the response because of duplicate `Access-Control-Allow-Origin` headers. We centralize CORS at the gateway and remove `@CrossOrigin` from individual services."

**Routing Config** (`application.yml`):
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true                    # Auto-discover routes from Eureka
          lower-case-service-id: true      # Use lowercase service names
```

---

### 2.3 Auth Service – Port 8081
**Purpose**: Handles user **registration**, **login**, **password reset**, and **JWT token generation/validation**.

**Entities:**
- `User`: id, name, email, password (BCrypt hashed), role (BUYER/SELLER), active, resetToken, createdAt
- `Role`: Enum → `BUYER`, `SELLER`

**Key Endpoints:**
| Method | Endpoint                | Description                     |
|--------|-------------------------|---------------------------------|
| POST   | `/api/auth/register`    | Register new user               |
| POST   | `/api/auth/login`       | Authenticate and return JWT     |
| POST   | `/api/auth/forgot-password` | Initiate password reset     |
| POST   | `/api/auth/reset-password`  | Reset password with token   |
| GET    | `/api/auth/validate`    | Validate JWT token (internal)   |
| GET    | `/api/auth/user/{id}`   | Get user by ID (internal)       |

**JWT Token Structure:**
```json
{
  "sub": "gotam123@gmail.com",    // email as subject
  "role": "BUYER",                 // user role
  "userId": 5,                     // user ID
  "iat": 1773098334,               // issued at timestamp
  "exp": 1773184734                // expires in 24 hours
}
```

**Security Config:**
- BCrypt password encoder
- Stateless session management (no server-side sessions)
- All `/api/auth/**` endpoints are publicly accessible
- HMAC-SHA256 JWT signing with shared secret key

---

### 2.4 Product Service – Port 8082
**Purpose**: Manages the **product catalog** — CRUD operations for products and categories.

**Entities:**
- `Product`: id, name, description, price, mrp, discountPercentage (auto-calculated), quantity, rating, imageUrl, active, stockThreshold, sellerId, categoryId, createdAt, updatedAt
- `Category`: id, name, description

**Auto-calculated Discount:**
```java
@PrePersist
public void prePersist() {
    if (this.discountPercentage == null && this.mrp != null && this.price != null) {
        this.discountPercentage = ((this.mrp - this.price) / this.mrp) * 100;
    }
}
```

**Key Endpoints:**
| Method | Endpoint                          | Description                         |
|--------|-----------------------------------|-------------------------------------|
| GET    | `/api/products`                   | Get all products (paginated)        |
| GET    | `/api/products/{id}`              | Get single product details          |
| GET    | `/api/products/search?keyword=`   | Full-text search by keyword         |
| GET    | `/api/products/category/{catId}`  | Filter by category                  |
| POST   | `/api/products/batch`             | Get multiple products by IDs        |
| POST   | `/api/seller/products`            | Create product (seller only)        |
| PUT    | `/api/seller/products/{id}`       | Update product (seller only)        |
| DELETE | `/api/seller/products/{id}`       | Delete product (seller only)        |
| GET    | `/api/categories`                 | List all categories                 |

---

### 2.5 Cart Service – Port 8083
**Purpose**: Manages per-user shopping carts. Each user has one active cart.

**Entities:**
- `Cart`: id, userId, items (OneToMany), createdAt
- `CartItem`: id, cart (ManyToOne), productId, productName, productPrice, sellerId, quantity, createdAt

**Key Endpoints:**
| Method | Endpoint                    | Header Required | Description                 |
|--------|-----------------------------|-----------------|-----------------------------|
| GET    | `/api/cart`                 | X-User-Id       | Get user's cart             |
| POST   | `/api/cart/items`           | X-User-Id       | Add item to cart            |
| PUT    | `/api/cart/items/{itemId}`  | X-User-Id       | Update item quantity        |
| DELETE | `/api/cart/items/{itemId}`  | X-User-Id       | Remove item from cart       |
| DELETE | `/api/cart`                 | X-User-Id       | Clear entire cart           |

**Design Decision**: Cart uses `X-User-Id` header (forwarded by the Gateway from JWT) rather than parsing JWT itself.

---

### 2.6 Checkout Service – Port 8085
**Purpose**: Orchestrates the multi-step checkout and payment flow. Creates checkout sessions, handles address entry, processes payment, and delegates order creation to the Order Service.

**Entities:**
- `CheckoutSession`: id, userId, cartSnapshot (JSON TEXT), shippingAddress, billingAddress, contactName, phoneNumber, paymentMethod, paymentStatus, totalAmount, status, createdAt, expiresAt (30 min TTL)
- `CheckoutStatus`: Enum → `INITIATED`, `ADDRESS_ADDED`, `COMPLETED`, `EXPIRED`, `CANCELLED`
- `PaymentTransaction`: id, checkoutSessionId, transactionId (UUID), amount, paymentMethod, status, orderId, errorMessage, createdAt
- `PaymentMethod`: Enum → `COD`, `CREDIT_CARD`, `DEBIT_CARD`
- `PaymentStatus`: Enum → `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

**Key Endpoints:**
| Method | Endpoint                                | Description                       |
|--------|-----------------------------------------|-----------------------------------|
| POST   | `/api/checkout/initiate`                | Create checkout session from cart  |
| PUT    | `/api/checkout/{sessionId}/address`     | Add shipping/billing address      |
| POST   | `/api/payment/process`                  | Process payment (COD or Card)     |
| GET    | `/api/payment/{transactionId}`          | Get transaction status            |

**Inter-Service Communication (Feign Clients):**
```java
@FeignClient(name = "cart-service")
public interface CartServiceClient {
    @GetMapping("/api/cart")
    CartDto getCart(@RequestHeader("X-User-Id") Long userId);
    
    @DeleteMapping("/api/cart")
    void clearCart(@RequestHeader("X-User-Id") Long userId);
}

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    @PostMapping("/api/orders")
    Map<String, Object> createOrder(@RequestBody CreateOrderRequest request);
}
```

**Circuit Breaker (Resilience4j):**
```java
@CircuitBreaker(name = "orderService", fallbackMethod = "processPaymentFallback")
public PaymentResponse processPayment(PaymentRequest request) { ... }
```
> "If the order-service goes down, the circuit breaker trips after repeated failures, immediately returning a user-friendly error instead of waiting for timeouts."

---

### 2.7 Order Service – Port 8084
**Purpose**: Source of truth for all purchases. Handles order creation, status tracking, cancellation, buyer notifications, and seller order views.

**Entities:**
- `Order`: id, userId, totalAmount, shippingAddress, billingAddress, contactName, phoneNumber, paymentMethod, paymentStatus, orderDate, status, createdAt, orderItems (OneToMany)
- `OrderItem`: id, order (ManyToOne), productId, productName, sellerId, quantity, priceAtPurchase, subtotal
- `OrderStatus`: Enum → `PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` → `CANCELLED`
- `Notification`: id, userId, message, type, referenceId, isRead, createdAt
- `NotificationType`: Enum → `ORDER_PLACED`, `ORDER_CONFIRMED`, `ORDER_SHIPPED`, `ORDER_DELIVERED`, `ORDER_CANCELLED`
- `Review`: id, userId, productId, orderId, rating, comment, createdAt

**Key Endpoints:**
| Method | Endpoint                      | Description                          |
|--------|-------------------------------|--------------------------------------|
| POST   | `/api/orders`                 | Create order (called by checkout)    |
| GET    | `/api/orders/{id}`            | Get order by ID                      |
| GET    | `/api/orders/my`              | Get buyer's orders (uses JWT)        |
| GET    | `/api/orders/seller`          | Get seller's orders (uses JWT)       |
| PUT    | `/api/orders/{id}/status`     | Update order status (seller)         |
| PUT    | `/api/orders/{id}/cancel`     | Cancel order (buyer)                 |
| POST   | `/api/reviews`                | Submit a review                      |
| GET    | `/api/reviews/product/{id}`   | Get reviews for a product            |
| GET    | `/api/reviews/my`             | Get buyer's reviews                  |
| GET    | `/api/notifications`          | Get user's notifications             |
| GET    | `/api/notifications/unread`   | Get unread notifications             |
| PUT    | `/api/notifications/{id}/read`| Mark notification as read            |
| GET    | `/api/favorites`              | Get user's favorites                 |
| POST   | `/api/favorites/{productId}`  | Add product to favorites             |
| DELETE | `/api/favorites/{productId}`  | Remove from favorites                |

**JWT Utility (`JwtUtil.java`):**
The Order Service uses a shared JWT secret to extract userId from the `Authorization` header. It also supports `X-User-Id` header forwarding from the Gateway.

```java
public static Long getUserIdFromRequest(HttpServletRequest request) {
    // First try X-User-Id header (forwarded by gateway)
    // Then fall back to parsing JWT from Authorization header
}
```

---

## 3. Database Schema (MySQL)

All services share one database: `revshop_p3` on MySQL 8.0.

### Tables Created by JPA Auto-DDL:
| Table               | Service          | Key Columns                                          |
|---------------------|------------------|------------------------------------------------------|
| `users`             | auth-service     | id, name, email, password, role, active, resetToken   |
| `products`          | product-service  | id, name, price, mrp, quantity, sellerId, categoryId |
| `categories`        | product-service  | id, name, description                                |
| `carts`             | cart-service     | id, userId, createdAt                                |
| `cart_items`        | cart-service     | id, cart_id, productId, productName, quantity         |
| `orders`            | order-service    | id, userId, totalAmount, status, paymentMethod       |
| `order_items`       | order-service    | id, order_id, productId, quantity, priceAtPurchase   |
| `notifications`     | order-service    | id, userId, message, type, isRead                    |
| `reviews`           | order-service    | id, userId, productId, orderId, rating, comment      |
| `favorites`         | order-service    | id, userId, productId, addedAt                       |
| `checkout_sessions` | checkout-service | id, userId, cartSnapshot, status, expiresAt          |
| `payment_transactions` | checkout-service | id, transactionId, amount, paymentMethod, orderId |

---

## 4. Inter-Service Communication Map

```
checkout-service  ──(Feign)──→  cart-service      (fetch cart, clear cart)
checkout-service  ──(Feign)──→  order-service     (create order after payment)
order-service     ──(Feign)──→  product-service   (validate product prices at checkout)
api-gateway       ──(Eureka)──→ ALL services      (route requests by service name)
```
