# 🐛 Bugs Fixed & Challenges Faced

## 1. Duplicate CORS Headers (Critical)

### Problem
Browser rejected API responses with error:
```
Access to XMLHttpRequest has been blocked by CORS policy: The 'Access-Control-Allow-Origin' header contains multiple values 'http://localhost:4200, *', but only one is allowed.
```

### Root Cause
Both the API Gateway AND individual services (`@CrossOrigin(origins = "*")` on controllers, CORS beans in SecurityConfig) were adding CORS headers. The browser received two `Access-Control-Allow-Origin` values.

### Fix
1. Removed `@CrossOrigin(origins = "*")` from all backend controllers
2. Removed CORS config from individual service SecurityConfigs
3. Created a centralized `WebFilter` in the API Gateway at `HIGHEST_PRECEDENCE`
4. This filter strips any downstream CORS headers and adds the correct single value
5. Added `DedupeResponseHeader` in Gateway's `application.yml` as a safety net

---

## 2. Login Failed — 401 Unauthorized

### Problem
Frontend login page returned 401 despite correct credentials.

### Root Cause
Spring Security's default configuration was blocking the `/api/auth/login` endpoint because it required authentication for all requests.

### Fix
Updated `SecurityConfig.java` in auth-service:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated())
```

---

## 3. Cart Price Showing "₹" Symbol Without Number

### Problem
In the cart page, product prices displayed as just "₹" (the rupee symbol) without any number. E.g., showing "₹" instead of "₹45,999.00".

### Root Cause
The HTML template used `item.price` but the backend cart item response used `productPrice` as the field name. `item.price` was `undefined`, so Angular's `number` pipe rendered nothing.

### Fix
Updated `cart.component.html` to use fallback:
```html
₹{{ (item.price || item.productPrice) | number:'1.2-2' }}
```

---

## 4. Order ID Showing "NA" After Placing Order

### Problem
After successfully placing an order, the Order Confirmation page showed "Order ID: NA" instead of the actual order number.

### Root Cause
The Checkout Service's Feign client returned a `Map<String, Object>` from the Order Service. The `PaymentServiceImpl` extracted the orderId using a key that didn't match the actual response structure. The orderId was never populated in the `PaymentResponse` sent back to the frontend.

### Fix
1. Updated `PaymentServiceImpl.java` to correctly extract `orderId` from the Map response
2. Updated `payment-page.component.ts` to use fallback extraction:
```typescript
const extractedOrderId = response.orderId || response.id || response.data?.orderId || response.transactionId || 'UNKNOWN';
```

---

## 5. Docker Compose Services Hanging

### Problem
Running `docker-compose up` caused services to hang and become unresponsive. Services kept restarting.

### Root Cause
Services were starting before MySQL was ready, causing JDBC connection failures. Health checks were misconfigured or too aggressive.

### Fix
1. Added proper health checks to all containers
2. Used `depends_on` with `condition: service_healthy`
3. For development, switched to running services natively with `start-all.sh` (faster iteration)

---

## 6. "No Static Resource" Error on Order Service

### Problem
GET requests to `/api/orders/my` returned `404 No Static Resource`.

### Root Cause
The Spring Boot application was scanning for static resources in the `resources/static` folder instead of routing to the controller.

### Fix
Ensured that `@RequestMapping("/api/orders")` was correctly placed on the controller class and the endpoint methods had proper HTTP method annotations.

---

## 7. Seller Can't See Orders

### Problem
Seller dashboard showed "No orders" even though buyers had placed orders.

### Root Cause
The `getOrdersBySeller` method queried orders by `sellerId`, but the order items' `sellerId` field wasn't being populated during order creation.

### Fix
Updated `OrderServiceImpl.createOrder()` to fetch `sellerId` from the Product Service:
```java
ProductDto product = productServiceClient.getProductById(itemRequest.getProductId());
orderItem.setSellerId(product.getSellerId());
```

---

## Lessons Learned

1. **CORS must be handled at ONE place** — either the gateway OR the services, never both
2. **Backend field names must match frontend expectations** — use DTO normalization
3. **Feign client return types matter** — `Map<String, Object>` is brittle; prefer typed DTOs
4. **Health checks are essential** for Docker startup ordering
5. **Local development is faster** than Docker during active development
6. **Circuit breakers prevent cascade failures** — always add fallbacks on inter-service calls
