# RevShop — Holistic Project Overview

> **For detailed documentation, see the individual files in this directory.**

## Project Summary
RevShop is a **microservices-based e-commerce platform** built with **Angular 19** (frontend) and **Spring Boot 3.2** (backend). It supports complete Buyer and Seller workflows powered by **6 business microservices**, **Eureka service discovery**, **Spring Cloud Gateway**, **JWT authentication**, and **MySQL**.

## Quick Architecture
```
Angular Frontend (4200)
    └── API Gateway (8080) ── Spring Cloud Gateway + CORS
         ├── Auth Service (8081) ── JWT, BCrypt, User CRUD
         ├── Product Service (8082) ── Catalog, Categories, Search
         ├── Cart Service (8083) ── Per-user shopping cart
         ├── Order Service (8084) ── Orders, Reviews, Notifications, Favorites
         └── Checkout Service (8085) ── Checkout sessions, Payment processing
              └── Eureka Discovery (8761) ── Service Registry
                   └── MySQL 8.0 (3306) ── revshop_p3 database
```

## Documentation Index
| File | Contents |
|------|----------|
| [01_Project_Overview.md](./01_Project_Overview.md) | Team, features, tech stack, run instructions |
| [02_Backend_Architecture.md](./02_Backend_Architecture.md) | All microservices, entities, endpoints |
| [03_Frontend_Architecture.md](./03_Frontend_Architecture.md) | Angular structure, routing, services, state |
| [04_API_Endpoints_and_Data_Flow.md](./04_API_Endpoints_and_Data_Flow.md) | Full API tables + sequence diagrams |
| [05_Security_CORS_and_Cross_Cutting.md](./05_Security_CORS_and_Cross_Cutting.md) | JWT, CORS, Circuit Breaker |
| [06_Docker_and_Deployment.md](./06_Docker_and_Deployment.md) | Docker, ports, build commands |
| [07_Interview_Questions_and_Answers.md](./07_Interview_Questions_and_Answers.md) | 29 detailed Q&As |
| [08_Bugs_Fixed_and_Challenges.md](./08_Bugs_Fixed_and_Challenges.md) | All bugs and solutions |
| [09_Database_Schema.md](./09_Database_Schema.md) | ER diagram, 12 tables detailed |
| [10_Future_Enhancements.md](./10_Future_Enhancements.md) | Production improvements |
