# 🛡️ Security, CORS & Cross-Cutting Concerns

## 1. Authentication & Authorization

### JWT-Based Stateless Authentication
RevShop uses **JSON Web Tokens (JWT)** for stateless authentication. No server-side sessions are maintained.

**How it works:**
1. User logs in with email/password
2. Auth Service validates credentials using **BCrypt** password matching
3. Auth Service generates a JWT signed with **HMAC-SHA256** algorithm
4. JWT contains: `sub` (email), `userId`, `role`, `iat` (issued at), `exp` (expiry - 24 hours)
5. Frontend stores token in `localStorage`
6. Every subsequent API request includes `Authorization: Bearer <token>` header
7. Backend services decode the JWT to extract user identity

**JWT Secret Key (Shared):**
```java
private static final String SECRET_KEY = "revshop-secret-key-for-jwt-token-generation-p3-2024";
```

**Interview Answer:**
> "We use JWT because it's stateless — the server doesn't need to store session data. The token contains all the user info needed (userId, role, email), signed with a shared secret. This means any microservice can independently verify the token without calling the auth service, reducing latency."

### Password Security
- Passwords are hashed using **BCrypt** (one-way hash with salt)
- BCrypt has built-in salt generation, making rainbow table attacks infeasible
- `BCryptPasswordEncoder` from Spring Security

### Authorization
- **Role-based access**: Routes are protected by role (`BUYER` or `SELLER`)
- **Frontend**: Angular `AuthGuard` checks JWT-decoded role before allowing route access
- **Backend**: Product Service has `SecurityConfig` that permits/restricts endpoints
- **Seller endpoints** (`/api/seller/products/**`) are restricted to authenticated sellers

---

## 2. CORS (Cross-Origin Resource Sharing)

### The Problem
Frontend runs on `http://localhost:4200`, backend on `http://localhost:8080`. Browsers block cross-origin requests by default.

### The Solution: Centralized CORS at API Gateway

**Why NOT at individual services?**
If both the Gateway and downstream services add CORS headers, browsers receive **duplicate** `Access-Control-Allow-Origin` headers and reject the response entirely.

**Implementation (`CorsConfig.java` in API Gateway):**
```java
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
public WebFilter corsFilter() {
    return (exchange, chain) -> {
        // OPTIONS (Preflight) → respond immediately
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            headers.set("Access-Control-Allow-Origin", "http://localhost:4200");
            headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            headers.set("Access-Control-Allow-Headers", "*");
            headers.set("Access-Control-Allow-Credentials", "true");
            return Mono.empty();
        }
        
        // Actual requests → strip downstream CORS headers, add our own
        exchange.getResponse().beforeCommit(() -> {
            headers.remove("Access-Control-Allow-Origin"); // Remove downstream duplicates
            headers.set("Access-Control-Allow-Origin", "http://localhost:4200");
            headers.set("Access-Control-Allow-Credentials", "true");
            return Mono.empty();
        });
        return chain.filter(exchange);
    };
}
```

**Interview Answer:**
> "We had a tricky CORS bug where the browser rejected responses because both the API Gateway and individual services were adding their own `Access-Control-Allow-Origin` headers. The browser saw `http://localhost:4200` from the gateway AND `*` from the downstream service — two values on the same header is invalid per the spec. We fixed it by centralizing ALL CORS handling in the Gateway with a custom WebFilter at highest precedence. The filter strips any CORS headers added by downstream services before adding the correct single value."

---

## 3. Circuit Breaker (Resilience4j)

**Problem**: If the Order Service goes down during checkout, the entire Checkout Service could hang waiting for a timeout.

**Solution**: Resilience4j Circuit Breaker pattern.

```java
@CircuitBreaker(name = "orderService", fallbackMethod = "processPaymentFallback")
public PaymentResponse processPayment(PaymentRequest request) {
    // Normal payment processing...
}

public PaymentResponse processPaymentFallback(PaymentRequest request, Exception e) {
    throw new PaymentFailedException("Order service is currently unavailable. Please try again later.");
}
```

**How Circuit Breaker Works:**
1. **Closed State**: Normal operation. Requests go through.
2. **Open State**: After X failures, circuit "opens" — all requests immediately fail with fallback.
3. **Half-Open State**: After a timeout, allows a few test requests through. If they succeed, circuit closes again.

**Interview Answer:**
> "We use Resilience4j's circuit breaker on inter-service calls. For example, the checkout service calls the order service to create an order after payment. If the order service is down, instead of each request waiting 30 seconds to timeout, the circuit breaker trips after a few failures and immediately returns a user-friendly error. This prevents cascade failures across the system."

---

## 4. Exception Handling

Each service has a `@RestControllerAdvice` global exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CheckoutNotFoundException.class)
    → 404 NOT_FOUND
    
    @ExceptionHandler(CheckoutExpiredException.class)
    → 410 GONE
    
    @ExceptionHandler(PaymentFailedException.class)
    → 402 PAYMENT_REQUIRED
    
    @ExceptionHandler(IllegalArgumentException.class)
    → 400 BAD_REQUEST
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    → 422 UNPROCESSABLE_ENTITY (Validation errors)
    
    @ExceptionHandler(Exception.class)
    → 500 INTERNAL_SERVER_ERROR (Catch-all)
}
```

**Custom Exceptions:**
- `CheckoutNotFoundException` → Session not found
- `CheckoutExpiredException` → 30-minute checkout window expired
- `PaymentFailedException` → Payment processing error
- `OrderNotFoundException` → Order not found
- `UnauthorizedException` → User trying to cancel another user's order

---

## 5. Checkout Session Expiry

Checkout sessions have a **30-minute TTL**:
```java
@PrePersist
public void prePersist() {
    if (expiresAt == null) {
        expiresAt = LocalDateTime.now().plusMinutes(30);
    }
}

public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
}
```

If user attempts to pay after 30 minutes → `410 GONE` response.

---

## 6. Data Validation

### Backend (Jakarta Validation)
```java
public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) { ... }
```
- `@NotBlank` on name, email, password
- `@Email` on email field
- `@Min(1)` on quantities
- Validation errors return 422 with specific field error messages

### Frontend
- Phone number regex: `/^\+?[0-9]{10,15}$/`
- Required field checks before API calls
- Quantity clamping: `min(1)` to `max(availableStock)`
