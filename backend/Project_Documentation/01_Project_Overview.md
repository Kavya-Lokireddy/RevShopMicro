# 📦 RevShop – Full Project Overview

## 1. What Is RevShop?

**RevShop** is a full-stack, distributed e-commerce platform built using a **microservices architecture**. It allows **Buyers** to browse, search, add to cart, checkout, pay, and review products; and **Sellers** to manage inventory, track orders, and monitor business metrics — all through a unified Angular frontend communicating with a Spring Boot backend via an API Gateway.

> **One-liner for interviews:**
> *"RevShop is a microservices-based e-commerce platform with an Angular frontend, Spring Cloud Gateway, Eureka service discovery, JWT authentication, and MySQL persistence — supporting complete buyer-seller workflows from product catalog to checkout and order tracking."*

---

## 2. Team Contributors
| Member     | Module Ownership                                 |
|------------|--------------------------------------------------|
| **Gotam**  | Order Management, Order List, Favorites          |
| **Manjula**| Authentication (Login, Register, Password Reset) |
| **Jatin**  | Buyer Dashboard, Product Search, Product Details |
| **Kavya**  | Seller Dashboard, Product CRUD, Reviews in Seller view |
| **Anusha** | Checkout, Payment, Order Confirmation            |
| **Pavan**  | Cart Management                                  |

---

## 3. Core Features At a Glance

### Buyer Features
- ✅ Register and Login with JWT
- ✅ Browse all products with pagination
- ✅ Search products by keyword
- ✅ View product details with reviews & ratings
- ✅ Add/remove products to/from cart
- ✅ Wishlist / Favorites
- ✅ Multi-step checkout: Cart → Checkout → Payment → Order Confirmation
- ✅ View past orders and their status (PENDING → CONFIRMED → SHIPPED → DELIVERED)
- ✅ Cancel pending/confirmed orders
- ✅ Rate & review purchased products
- ✅ Real-time notifications (order placed, shipped, delivered)

### Seller Features
- ✅ Register as SELLER and Login
- ✅ Add, edit, and delete products
- ✅ View aggregated business metrics: Total Sales, Total Orders, Low Stock Alerts
- ✅ View and manage incoming orders (update status)
- ✅ View customer reviews for their products

---

## 4. Technology Stack

### Backend
| Technology             | Purpose                                         |
|------------------------|--------------------------------------------------|
| Java 17                | Core language                                    |
| Spring Boot 3.2.0      | Microservice framework                           |
| Spring Cloud 2023.0.0  | Service Discovery, Gateway, Feign Clients        |
| Spring Cloud Gateway   | API routing, CORS handling                       |
| Netflix Eureka         | Service registration and discovery               |
| Spring Data JPA        | ORM with Hibernate                               |
| Spring Security        | Authentication, authorization                    |
| JWT (jjwt 0.12.3)      | Stateless token-based authentication             |
| BCrypt                 | Password hashing                                 |
| OpenFeign              | Declarative inter-service REST calls             |
| Resilience4j           | Circuit breaker for fault tolerance              |
| MySQL 8.0              | Relational database                              |
| Maven (Multi-module)   | Dependency and build management                  |
| Docker + Docker Compose| Container orchestration                          |

### Frontend
| Technology        | Purpose                                    |
|-------------------|--------------------------------------------|
| Angular 19        | SPA framework                              |
| TypeScript        | Typed JavaScript                           |
| RxJS              | Reactive state management (BehaviorSubject)|
| Angular Router    | Client-side routing with guards            |
| HttpClient        | HTTP communication with backend            |
| jwt-decode        | Client-side JWT token parsing              |
| CSS3              | Custom responsive styling                  |

### Infrastructure
| Component          | Purpose                        |
|--------------------|--------------------------------|
| Docker Compose     | Multi-container orchestration  |
| MySQL 8.0 Container| Database in Docker             |
| Custom start-all.sh| Local native startup script    |

---

## 5. How to Run the Project

### Option A: Run Locally (Without Docker)
```bash
# 1. Start MySQL (must be running on localhost:3306, user: root, password: root)
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS revshop_p3;"

# 2. Build all services
cd RevShop_P3
mvn clean package -DskipTests

# 3. Start all services (uses start-all.sh)
bash start-all.sh

# 4. Start frontend
cd frontend
npm install
npm start

# 5. Open browser
open http://localhost:4200
```

### Option B: Run with Docker Compose
```bash
cd RevShop_P3
docker-compose up --build
# Frontend must still be started separately
cd frontend && npm start
```

### Test Credentials
| Role   | Email                   | Password      |
|--------|-------------------------|---------------|
| Buyer  | gotam123@gmail.com      | password123   |
| Seller | seller@gotamsingh.com   | password123   |

---

## 6. Project Directory Structure

```
Project_P3_DesktopFolder/
├── RevShop_P3/                    # Backend (Multi-module Maven)
│   ├── pom.xml                    # Parent POM
│   ├── docker-compose.yml         # Docker orchestration
│   ├── start-all.sh               # Local startup script
│   ├── discovery-server/          # Eureka Server (Port 8761)
│   ├── config-server/             # Config Server (Port 8888)
│   ├── api-gateway/               # API Gateway (Port 8080)
│   ├── auth-service/              # Authentication (Port 8081)
│   ├── product-service/           # Product Catalog (Port 8082)
│   ├── cart-service/              # Shopping Cart (Port 8083)
│   ├── order-service/             # Orders & Reviews (Port 8084)
│   └── checkout-service/          # Checkout & Payment (Port 8085)
│
└── frontend/                      # Angular SPA (Port 4200)
    └── src/app/
        ├── core/                  # Guards, Interceptors, Services
        ├── features/              # Feature modules (auth, buyer, seller, cart, etc.)
        └── model/                 # Shared models
```
