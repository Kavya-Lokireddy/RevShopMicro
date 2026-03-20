

# RevShop

RevShop is a Spring Boot + Angular e‑commerce platform built as a microservices system with service discovery, centralized config, an API gateway, and domain services for auth, catalog, cart, checkout, and orders.

## Repository Layout
```
RevShopMicro/
├── backend/ # Spring Boot microservices and config
│ ├── api-gateway/ # Gateway + JWT auth
│ ├── auth-service/ # Login/registration + JWT issuing
│ ├── product-service/ # Catalog & seller ops
│ ├── cart-service/ # Cart & wishlist
│ ├── checkout-service/ # Checkout/payment orchestration
│ ├── order-service/ # Orders, reviews, notifications
│ ├── config-server/ # Spring Cloud Config (native profile)
│ ├── discovery-server/ # Eureka registry
│ └── pom.xml # Parent POM (Spring Boot 3.2, Java 17)
└── frontend/ # Angular 16 client (dev port 3000)
```

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2, Spring Cloud 2023.0, Netflix Eureka, Spring Cloud Gateway, JWT (jjwt 0.12.3), MySQL 8
- **Frontend:** Angular 16
- **Build/Tools:** Maven; Node/NPM for Angular

## Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8 (database `revshop_p3`, user/pass default `root`/`root`)
- Node.js (v18+ recommended) and npm

## Running Locally (no Docker)
1) Start MySQL and ensure database `revshop_p3` exists (or let Hibernate create tables).
2) In separate terminals (from `backend/`), start services in this order:
```bash
cd config-server      && mvn spring-boot:run    # port 8888
cd discovery-server   && mvn spring-boot:run    # port 8761 (depends on config-server)
cd api-gateway        && mvn spring-boot:run    # port 8080 (depends on discovery)
cd auth-service       && mvn spring-boot:run    # port 8081
cd product-service    && mvn spring-boot:run    # port 8082
cd cart-service       && mvn spring-boot:run    # port 8083
cd order-service      && mvn spring-boot:run    # port 8084
cd checkout-service   && mvn spring-boot:run    # port 8085
Verify:
Eureka dashboard: http://localhost:8761
API Gateway: http://localhost:8080
Frontend Dev Server
cd frontend
npm install
npm start             # Angular dev server on http://localhost:3000
Proxy rules (frontend/proxy.conf.json):

/api → http://localhost:8080 (gateway)
/api/seller, /api/products, /api/categories → http://localhost:8082 (product service)
Build for production:

npm run build         # outputs to frontend/dist/
Authentication
Gateway validates JWT and forwards user context via headers:
X-User-Id, X-User-Email, X-User-Role (BUYER/SELLER)
Token payload example:
{ "sub": "user@example.com", "role": "BUYER", "userId": 1, "iat": 0, "exp": 0 }
Key API Routes (through Gateway http://localhost:8080)

Path	Service	Auth?	Purpose
/api/auth/**	auth-service	No	Login/registration/password
/api/products/**	product-service	Yes	Browse products
/api/seller/**	product-service	Yes	Seller product CRUD
/api/cart/**	cart-service	Yes	Cart operations
/api/favorites/**	cart-service	Yes	Wishlist
/api/checkout/**	checkout-service	Yes	Checkout flow
/api/payment/**	checkout-service	Yes	Payment handling
/api/orders/**	order-service	Yes	Orders
/api/reviews/**	order-service	Yes	Reviews/ratings
/api/notifications/**	order-service	Yes	Notifications

Environment Configuration
Common environment variables (per service application.yml defaults shown in parentheses):

SPRING_PROFILES_ACTIVE (default)
SPRING_DATASOURCE_URL (jdbc:mysql://127.0.0.1:3306/revshop_p3?...)
SPRING_DATASOURCE_USERNAME (root)
SPRING_DATASOURCE_PASSWORD (root)
SPRING_DATASOURCE_DRIVER_CLASS_NAME (com.mysql.cj.jdbc.Driver)
SPRING_JPA_DATABASE_PLATFORM (org.hibernate.dialect.MySQLDialect)
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE (http://localhost:8761/eureka/)
Testing
cd backend   && mvn test
cd frontend  && npm test
