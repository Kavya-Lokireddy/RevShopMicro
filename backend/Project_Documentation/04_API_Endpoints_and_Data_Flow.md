# 🔄 API Endpoints & Data Flow

## 1. Complete API Reference

### Authentication Service (Port 8081)
| Method | Endpoint                   | Body                                   | Response                              | Auth  |
|--------|----------------------------|----------------------------------------|---------------------------------------|-------|
| POST   | `/api/auth/register`       | `{name, email, password, role}`        | `"User registered successfully"`      | No    |
| POST   | `/api/auth/login`          | `{email, password}`                    | `{token, userId, name, email, role}`  | No    |
| POST   | `/api/auth/forgot-password`| `{email}`                              | `"Reset link sent"`                   | No    |
| POST   | `/api/auth/reset-password` | `{token, newPassword}`                 | `"Password reset successfully"`       | No    |
| GET    | `/api/auth/validate`       | -                                      | `{userId, email, role}`               | JWT   |
| GET    | `/api/auth/user/{userId}`  | -                                      | `User object`                         | JWT   |

### Product Service (Port 8082)
| Method | Endpoint                          | Body / Params                    | Response                     | Auth     |
|--------|-----------------------------------|----------------------------------|------------------------------|----------|
| GET    | `/api/products?page=0&size=10`    | -                                | `{products[], total, page}`  | JWT      |
| GET    | `/api/products/{id}`              | -                                | `ProductResponse`            | JWT      |
| GET    | `/api/products/search?keyword=`   | `keyword` query param            | `ProductResponse[]`          | JWT      |
| GET    | `/api/products/category/{catId}`  | `page`, `size` query params      | `{products[], total, page}`  | JWT      |
| POST   | `/api/products/batch`             | `[id1, id2, id3]`               | `ProductResponse[]`          | JWT      |
| POST   | `/api/seller/products`            | `{name, description, price,...}` | `ProductResponse`            | JWT(Seller)|
| PUT    | `/api/seller/products/{id}`       | `{name, description, price,...}` | `ProductResponse`            | JWT(Seller)|
| DELETE | `/api/seller/products/{id}`       | -                                | `204 No Content`             | JWT(Seller)|
| GET    | `/api/seller/products`            | -                                | `ProductResponse[]`          | JWT(Seller)|
| GET    | `/api/categories`                 | -                                | `Category[]`                 | JWT      |

### Cart Service (Port 8083)
| Method | Endpoint                    | Headers / Body                       | Response         | Auth |
|--------|-----------------------------|--------------------------------------|------------------|------|
| GET    | `/api/cart`                 | `X-User-Id: {userId}`                | `CartResponse`   | JWT  |
| POST   | `/api/cart/items`           | `X-User-Id` + `{productId, quantity}`| `CartResponse`   | JWT  |
| PUT    | `/api/cart/items/{itemId}`  | `X-User-Id` + `{quantity}`           | `CartResponse`   | JWT  |
| DELETE | `/api/cart/items/{itemId}`  | `X-User-Id`                          | `CartResponse`   | JWT  |
| DELETE | `/api/cart`                 | `X-User-Id`                          | `204 No Content` | JWT  |

### Checkout Service (Port 8085)
| Method | Endpoint                                | Body                                    | Response                   | Auth |
|--------|-----------------------------------------|-----------------------------------------|----------------------------|------|
| POST   | `/api/checkout/initiate`                | `{userId, totalAmount}`                 | `CheckoutSessionResponse`  | JWT  |
| PUT    | `/api/checkout/{sessionId}/address`     | `{shippingAddress, contactName, phone}` | `CheckoutSessionResponse`  | JWT  |
| POST   | `/api/payment/process`                  | `{checkoutSessionId, paymentMethod}`    | `{transactionId, orderId}` | JWT  |
| GET    | `/api/payment/{transactionId}`          | -                                       | `PaymentTransaction`       | JWT  |

### Order Service (Port 8084)
| Method | Endpoint                       | Body / Params                | Response              | Auth |
|--------|--------------------------------|------------------------------|-----------------------|------|
| POST   | `/api/orders`                  | `CreateOrderRequest`         | `OrderResponse`       | JWT  |
| GET    | `/api/orders/{id}`             | -                            | `OrderResponse`       | JWT  |
| GET    | `/api/orders/my`               | -                            | `OrderResponse[]`     | JWT  |
| GET    | `/api/orders/seller`           | -                            | `OrderResponse[]`     | JWT  |
| PUT    | `/api/orders/{id}/status`      | `{status}`                   | `OrderResponse`       | JWT  |
| PUT    | `/api/orders/{id}/cancel`      | -                            | `"Order cancelled"`   | JWT  |
| POST   | `/api/reviews`                 | `{orderId, productId, rating, comment}` | `Review`   | JWT  |
| GET    | `/api/reviews/product/{id}`    | -                            | `{reviews[], avgRating, total}` | JWT |
| GET    | `/api/reviews/my`              | -                            | `Review[]`            | JWT  |
| GET    | `/api/notifications`           | -                            | `Notification[]`      | JWT  |
| GET    | `/api/notifications/unread`    | -                            | `Notification[]`      | JWT  |
| PUT    | `/api/notifications/{id}/read` | -                            | `200 OK`              | JWT  |
| GET    | `/api/favorites`               | -                            | `Favorite[]`          | JWT  |
| POST   | `/api/favorites/{productId}`   | -                            | `Favorite`            | JWT  |
| DELETE | `/api/favorites/{productId}`   | -                            | `204 No Content`      | JWT  |
| GET    | `/api/favorites/{id}/check`    | -                            | `{isFavorite: bool}`  | JWT  |

---

## 2. End-to-End Data Flows

### Flow 1: User Registration & Login
```
Frontend                    API Gateway              Auth Service              MySQL
   │                            │                        │                      │
   │──POST /api/auth/register──→│────/api/auth/register─→│                      │
   │                            │                        │──BCrypt hash pwd────→│
   │                            │                        │──INSERT INTO users──→│
   │                            │                        │←──User saved────────│
   │←──"User registered"───────│←───────────────────────│                      │
   │                            │                        │                      │
   │──POST /api/auth/login─────→│────/api/auth/login────→│                      │
   │                            │                        │──SELECT by email────→│
   │                            │                        │←──User row────────────│
   │                            │                        │──BCrypt.matches()     │
   │                            │                        │──Generate JWT token   │
   │←──{token, userId, role}───│←───────────────────────│                      │
   │                            │                        │                      │
   │──Store token in localStorage                        │                      │
   │──Navigate to dashboard                              │                      │
```

### Flow 2: Add to Cart → Checkout → Payment → Order
```
Frontend           Gateway         Cart Svc        Checkout Svc      Order Svc        Product Svc
   │                  │               │                │                │                │
   │──POST /cart/items→│──────────────→│                │                │                │
   │                  │               │──validate──────│────────────────│───GET product──→│
   │                  │               │──save item─────│                │                │
   │←──CartResponse───│←──────────────│                │                │                │
   │                  │               │                │                │                │
   │──POST /checkout/initiate────────→│───────────────→│                │                │
   │                  │               │                │──GET /cart────→│                │
   │                  │               │                │←──CartDto─────│                │
   │                  │               │                │──save session  │                │
   │←──{sessionId}────│←──────────────│────────────────│                │                │
   │                  │               │                │                │                │
   │──PUT /checkout/{id}/address─────→│───────────────→│                │                │
   │←──{session updated}──────────────│────────────────│                │                │
   │                  │               │                │                │                │
   │──POST /payment/process──────────→│───────────────→│                │                │
   │                  │               │                │──validate      │                │
   │                  │               │                │──process pay   │                │
   │                  │               │                │──POST /orders──│───────────────→│
   │                  │               │                │                │──GET products──→│
   │                  │               │                │                │←──validate price│
   │                  │               │                │                │──save order     │
   │                  │               │                │                │──notify buyer   │
   │                  │               │                │                │──notify seller  │
   │                  │               │                │←──{orderId}────│                │
   │                  │               │                │──clear cart───→│                │
   │                  │               │                │──save txn      │                │
   │←──{orderId, transactionId}───────│────────────────│                │                │
   │                  │               │                │                │                │
   │──Navigate to /order-confirmation │                │                │                │
```

### Flow 3: Seller Views Orders & Updates Status
```
Frontend (Seller)         Gateway              Order Service              MySQL
   │                        │                       │                       │
   │──GET /orders/seller───→│───────────────────────│                       │
   │                        │                       │──Parse JWT (sellerId)─│
   │                        │                       │──SELECT orders WHERE  │
   │                        │                       │  sellerId matches─────│
   │←──OrderResponse[]─────│←──────────────────────│                       │
   │                        │                       │                       │
   │──PUT /orders/5/status──│───────────────────────│                       │
   │  {status: "SHIPPED"}   │                       │──UPDATE order status──│
   │                        │                       │──CREATE notification──│
   │←──OrderResponse───────│←──────────────────────│                       │
```
