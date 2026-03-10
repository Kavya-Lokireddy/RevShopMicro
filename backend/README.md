# RevShop Microservices Platform

## Overview

RevShop is a modern e-commerce platform built using a microservices architecture with Spring Boot 3.2 and Spring Cloud 2023.0.0. This project demonstrates a distributed system design with service discovery, API gateway, centralized configuration, and multiple business domain services.

**Project Owner:** Gotam (Order Management & Notification / Review)

## Architecture

### Core Infrastructure Services

1. **Config Server** (Port 8888)
   - Centralized configuration management
   - Native profile for local file-based configuration
   - Provides configuration to all microservices

2. **Discovery Server** (Port 8761)
   - Eureka-based service registry
   - Service discovery and registration
   - Load balancing support

3. **API Gateway** (Port 8080)
   - Single entry point for all client requests
   - JWT-based authentication and authorization
   - Request routing to appropriate microservices
   - CORS configuration for frontend integration

### Business Domain Services

4. **Auth Service**
   - User registration and authentication
   - JWT token generation and validation
   - User management (buyers and sellers)
   - Password reset functionality

5. **Product Service**
   - Product catalog management
   - Category management
   - Seller product operations
   - Product search and filtering
   - Stock management

6. **Cart Service**
   - Shopping cart management
   - Favorite/wishlist functionality
   - Cart item operations

7. **Checkout Service**
   - Checkout process
   - Payment processing
   - Order creation

8. **Order Service**
   - Order management
   - Order tracking
   - Review and rating system
   - Notification management

## Technology Stack

- **Java:** 17
- **Spring Boot:** 3.2.0
- **Spring Cloud:** 2023.0.0
- **Database:** PostgreSQL
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Authentication:** JWT (JSON Web Tokens)
- **Build Tool:** Maven
- **Containerization:** Docker & Docker Compose

## API Routes

All requests go through the API Gateway (http://localhost:8080):

| Path | Service | Auth Required | Description |
|------|---------|---------------|-------------|
| `/api/auth/**` | auth-service | No | Authentication endpoints |
| `/api/products/**` | product-service | Yes | Product browsing |
| `/api/seller/**` | product-service | Yes | Seller product management |
| `/api/cart/**` | cart-service | Yes | Shopping cart operations |
| `/api/favorites/**` | cart-service | Yes | Wishlist management |
| `/api/checkout/**` | checkout-service | Yes | Checkout process |
| `/api/payment/**` | checkout-service | Yes | Payment processing |
| `/api/orders/**` | order-service | Yes | Order management |
| `/api/reviews/**` | order-service | Yes | Product reviews |
| `/api/notifications/**` | order-service | Yes | User notifications |

## JWT Authentication

The API Gateway validates JWT tokens and extracts user information, adding the following headers to downstream requests:

- `X-User-Id`: User ID
- `X-User-Email`: User email
- `X-User-Role`: User role (BUYER or SELLER)

JWT Token Structure:
```json
{
  "sub": "user@example.com",
  "role": "BUYER",
  "userId": 1,
  "iat": 1234567890,
  "exp": 1234654290
}
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose
- PostgreSQL (if running locally without Docker)

### Running with Docker Compose

1. Clone the repository:
```bash
git clone <repository-url>
cd p3-revshop
```

2. Build all services:
```bash
mvn clean package -DskipTests
```

3. Start all services:
```bash
docker-compose up -d
```

4. Check service health:
```bash
docker-compose ps
```

5. View logs:
```bash
docker-compose logs -f [service-name]
```

6. Stop all services:
```bash
docker-compose down
```

### Running Locally (Development)

1. Start PostgreSQL database

2. Start Config Server:
```bash
cd config-server
mvn spring-boot:run
```

3. Start Discovery Server:
```bash
cd discovery-server
mvn spring-boot:run
```

4. Start API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
```

5. Start business services (in any order):
```bash
cd auth-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd checkout-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
```

### Service URLs

- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8080
- **Config Server:** http://localhost:8888

## Project Structure

```
p3-revshop/
├── config-server/          # Centralized configuration
├── discovery-server/       # Service registry (Eureka)
├── api-gateway/           # API Gateway with JWT auth
├── auth-service/          # Authentication & user management
├── product-service/       # Product catalog & seller operations
├── cart-service/          # Shopping cart & favorites
├── checkout-service/      # Checkout & payment
├── order-service/         # Orders, reviews & notifications
├── docker-compose.yml     # Docker orchestration
├── pom.xml               # Parent POM
└── README.md             # This file
```

## Development Guidelines

### Adding a New Service

1. Create module directory under root
2. Add module to parent `pom.xml`
3. Create service-specific `pom.xml` with parent reference
4. Implement Spring Boot application class
5. Configure `application.yml` with service name and port
6. Add Eureka client dependency
7. Update `docker-compose.yml` with new service

### Environment Variables

Services can be configured using environment variables:

- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default, docker)
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: Eureka server URL
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

## API Documentation

Each service exposes its own REST API. Common response format:

### Success Response
```json
{
  "data": { ... },
  "message": "Success",
  "timestamp": "2024-01-01T12:00:00"
}
```

### Error Response
```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2024-01-01T12:00:00"
}
```

## Database Schema

All services share a single PostgreSQL database with the following key tables:

- `users` - User accounts (buyers and sellers)
- `products` - Product catalog
- `categories` - Product categories
- `carts` - Shopping carts
- `cart_items` - Cart items
- `favorites` - User wishlists
- `orders` - Customer orders
- `order_items` - Order line items
- `reviews` - Product reviews
- `notifications` - User notifications

## Security

- JWT-based authentication with HS256 algorithm
- Passwords encrypted using BCrypt
- CORS enabled for http://localhost:3000
- Role-based access control (BUYER, SELLER)

## Testing

Run tests for all services:
```bash
mvn test
```

Run tests for a specific service:
```bash
cd [service-name]
mvn test
```

## Monitoring and Health Checks

Each service exposes health check endpoints:
- `http://[service-url]/actuator/health`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is developed as part of the Revature training program.

## Contact

**Owner:** Gotam
**Focus Area:** Order Management & Notification / Review Services
