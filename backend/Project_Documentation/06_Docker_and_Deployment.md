# 🐳 Docker & Deployment

## 1. Docker Compose Architecture

All services are containerized using Docker and orchestrated via `docker-compose.yml`:

```yaml
version: '3.8'
services:
  mysql:            # MySQL 8.0 database (Port 3306)
  config-server:    # Config Server (Port 8888)
  discovery-server: # Eureka Server (Port 8761) — depends on config-server
  api-gateway:      # API Gateway (Port 8080) — depends on discovery-server
  auth-service:     # Auth (internal) — depends on mysql, discovery-server
  product-service:  # Products (internal) — depends on mysql, discovery-server
  cart-service:     # Cart (internal) — depends on mysql, discovery-server
  checkout-service: # Checkout (internal) — depends on mysql, discovery-server
  order-service:    # Orders (internal) — depends on mysql, discovery-server

networks:
  revshop-network: bridge

volumes:
  mysql-data:       # Persistent MySQL data
```

### Startup Order (Health-check based):
```
MySQL
  └── Config Server (waits for MySQL health)
       └── Discovery Server (waits for Config Server health)
            └── API Gateway + All Business Services (wait for Discovery health)
```

### Environment Variables for Docker:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/revshop_p3?createDatabaseIfNotExist=true
  - SPRING_DATASOURCE_USERNAME=root
  - SPRING_DATASOURCE_PASSWORD=root
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/
```

> **Note**: In Docker, services use container names as hostnames (`mysql` instead of `localhost`, `discovery-server` instead of `localhost:8761`).

---

## 2. Individual Dockerfiles

Each service has a multi-stage Dockerfile:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Local Development (Without Docker)

For faster development without Docker overhead:

```bash
# Uses start-all.sh script
bash start-all.sh
```

**What the script does:**
1. Starts Discovery Server first (waits for it to be ready)
2. Starts all other services in parallel
3. Logs output to `/tmp/<service>.log`
4. Reports service status after 15 seconds

### Port Map (Local):
| Service           | Port  |
|-------------------|-------|
| Discovery Server  | 8761  |
| API Gateway       | 8080  |
| Auth Service      | 8081  |
| Product Service   | 8082  |
| Cart Service      | 8083  |
| Order Service     | 8084  |
| Checkout Service  | 8085  |
| Config Server     | 8888  |
| MySQL             | 3306  |
| Angular Frontend  | 4200  |

---

## 4. Database Configuration

### Local MySQL:
```yaml
# application.yml (each service)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/revshop_p3?createDatabaseIfNotExist=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update     # Auto-create/update tables from entities
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
```

### Docker MySQL:
Same credentials but `mysql` hostname instead of `localhost`.

---

## 5. Build & Deploy Commands

```bash
# Build all backend services
cd RevShop_P3
mvn clean package -DskipTests

# Build & start with Docker
docker-compose up --build -d

# View logs
docker-compose logs -f api-gateway
docker-compose logs -f auth-service

# Stop all
docker-compose down

# Remove volumes (reset data)
docker-compose down -v
```
