# Interview Preparation: RevShop Microservices Project

When interviewing for a Backend/Full-Stack Software Engineering role with a Microservices project like this, the interviewer will primarily test your understanding of **system design trade-offs, security, distributed transactions, and real-world debugging**. 

Here are the most critical technical questions you can expect, categorized by topic:

---

## 1. Architecture & System Design Questions

**Q: Why did you choose a Microservices architecture over a Monolith for this e-commerce project?**
> **How to answer:** Mention scalability. In an e-commerce platform during a sale (like Black Friday), the `cart-service` or `order-service` will get massive traffic, while the `auth-service` might not. Microservices allow you to scale only the highly isolated, bottlenecked services independently rather than copying the entire mammoth monolith application. Also, mention fault isolation—if the Review Service goes down, people can still buy things.

**Q: Can you explain the role of an API Gateway in your architecture?**
> **How to answer:** The API Gateway is the central entry point. It hides the complexity of internal microservices from the Angular frontend. Explain that avoiding cross-origin (CORS) nightmares, centralizing security/JWT validation in one place, and handling routing based on URL paths are its main duties.

**Q: How do your microservices communicate with each other?**
> **How to answer:** Mention **Spring Cloud OpenFeign**. Explain that it's a declarative, synchronous REST client. You just create interfaces with annotations, and Spring handles the HTTP request under the hood. 

**Q: In your system, what happens if the `product-service` crashes while someone is looking at their cart?**
> **How to answer:** Discuss the **Circuit Breaker Pattern (Resilience4j)**. Explain that if `cart-service` calls `product-service` and it times out, the circuit breaker opens. Instead of the cart failing entirely, it falls back to a default method or cached data, ensuring graceful degradation of the user experience.

---

## 2. Security Questions

**Q: How is Authentication handled in a distributed microservices environment?**
> **How to answer:** Explain **JSON Web Tokens (JWT)**. The `auth-service` verifies credentials and issues a cryptographically signed token. The API Gateway intercepts all requests, validates the signature, extracts user details, and passes them to internal services via HTTP HTTP Headers (e.g., `X-User-Id`). Contrast this by explaining why traditional Session IDs don't work well across multiple decentralized servers.

**Q: Did you face any security vulnerabilities while building the checkout process? How did you fix them?**
> **How to answer** *(This is based on the fix I applied for you)*: Talk about **Price Manipulation/Tampering**. Explain that originally, the frontend passed the total cart price to the `order-service`. A hacker could intercept the API request and change the `totalAmount` to $1. You fixed this by forcing the backend `order-service` to ignore the user's total, make a secure internal `Feign` call to the `product-service` to fetch the true prices directly from the database, and recalculate the subtotal server-side.

---

## 3. Advanced / Deep-Dive Technical Questions

**Q: What is the "N+1 Query Problem" and did you deal with it?**
> **How to answer** *(This is based on the fix I applied for you)*: Explain that in the Angular `cart.service`, looping through every item in a cart and making a separate HTTP request for each product's details creates a massive bottleneck. If a cart has 50 items, it triggers 50 API calls. You solved this by creating a **Batch Fetch API** (`POST /api/products/batch`) so the frontend sends an array of Product IDs, and the backend resolves them in a single fast database query.

**Q: If the `checkout-service` successfully processes a payment, but the `order-service` fails to save the order to the database, what state is your system in?**
> **How to answer:** Admit that this is the hardest problem in microservices: **Distributed Transactions**. Currently, the system experiences a critical failure—the user's credit card was charged, but the order doesn't exist. 
> *Follow-up:* To solve this permanently, explain that you would implement the **Saga Pattern** combined with a Message Broker (like **Apache Kafka or RabbitMQ**). Instead of synchronous HTTP calls, services emit events (e.g., "PaymentCompletedEvent"). If the `order-service` fails, it returns an "OrderFailedEvent", causing the `checkout-service` to issue an automatic refund (compensating transaction).

**Q: How do you manage frontend state in Angular?**
> **How to answer:** Discuss **RxJS `BehaviorSubject`**. Give the example of the `CartService`. It holds the global cart state in a `BehaviorSubject`. The Navbar badge component subscribes to it. As soon as the user adds an item to the cart on a product page, `.next()` is called, and the stream immediately pushes the new cart count to the Navbar instantly without requiring a page refresh.

**Q: How do you handle database architecture? Does every microservice have its own database?**
> **How to answer:** Mention the "Database per Microservice" pattern. Even if you run them internally on the same MySQL server for local testing, they use isolated schemas. This ensures that the `order-service` cannot write directly to the `product-service` tables, enforcing strict data ownership and decoupled domain logic.
