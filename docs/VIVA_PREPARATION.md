# Viva Preparation Guide

## 🎤 Common Questions & Answers

### **Q1: Why did you choose microservices architecture?**

**Answer:**
> "Microservices architecture allows each service to be developed, deployed, and scaled independently. For this marketplace:
> - User Service handles authentication and wallet (critical, needs high availability)
> - Booking Service handles core business logic (needs to scale during peak hours)
> - Catalog Service manages service offers (relatively static)
> - Notification Service handles async notifications (can be scaled separately)
>
> This separation allows us to optimize each service for its specific needs without affecting others."

---

### **Q2: Why didn't you use an API Gateway?**

**Answer:**
> "For this system with 4 microservices and a single React client, an API Gateway would add unnecessary complexity. API Gateways are beneficial when you have:
> - Multiple clients (web, mobile, external APIs)
> - 5-10+ services
> - Need for centralized rate limiting, authentication, or routing
>
> Instead, I focused on what matters more:
> - Clean service-to-service communication via REST
> - Event-driven architecture with RabbitMQ
> - JWT authentication in each service
>
> However, in production with multiple clients, I would definitely introduce an API Gateway."

---

### **Q3: How do you prevent double bookings?**

**Answer:**
> "I use a UNIQUE constraint on the bookings table:
> ```sql
> UNIQUE (service_offer_id, service_start, service_end)
> ```
>
> This ensures that the same service offer cannot be booked for overlapping time slots. Additionally:
> - Idempotency key prevents duplicate bookings from retries
> - Optimistic locking on wallet prevents race conditions
> - Database-level constraints are enforced before application logic"

---

### **Q4: How do you handle wallet deduction failures?**

**Answer:**
> "Wallet deduction is critical and handled with:
>
> 1. **Optimistic Locking** - Version column prevents concurrent updates
> 2. **Idempotency Key** - Same request won't deduct twice
> 3. **Transaction Management** - @Transactional ensures atomicity
> 4. **Error Handling** - If deduction fails:
>    - Booking creation is rolled back
>    - BookingFailedEvent is published
>    - Customer receives rejection notification
>    - No money is deducted
>
> 5. **Compensation Log** - Tracks all deductions for audit"

---

### **Q5: How do services communicate?**

**Answer:**
> "Two patterns:
>
> 1. **Synchronous REST Calls** (for critical operations)
>    - Booking Service calls User Service to deduct wallet
>    - Booking Service calls Catalog Service to get offer details
>    - Uses RestTemplate with error handling
>
> 2. **Asynchronous Events** (for non-critical operations)
>    - Booking Service publishes BookingCreatedEvent
>    - Notification Service listens and sends notifications
>    - Uses RabbitMQ for decoupling
>
> This hybrid approach gives us the best of both worlds:
> - Critical operations are synchronous (guaranteed)
> - Non-critical operations are async (scalable)"

---

### **Q6: How do you ensure data consistency across services?**

**Answer:**
> "Since each service has its own database (microservices isolation):
>
> 1. **No Cross-Database Foreign Keys** - Services communicate via REST API
> 2. **Idempotency Keys** - Prevent duplicate operations on retries
> 3. **Event Sourcing** - All state changes are published as events
> 4. **Compensation Log** - Tracks all transactions for audit
> 5. **Optimistic Locking** - Prevents concurrent update conflicts
>
> Example: When booking is created:
> - Booking Service creates booking record
> - Publishes BookingCreatedEvent
> - Notification Service listens and sends notifications
> - If notification fails, event is retried
> - Compensation log tracks all changes"

---

### **Q7: What happens if a service goes down?**

**Answer:**
> "Depends on which service:
>
> 1. **User Service Down**
>    - Booking Service cannot create bookings (critical)
>    - Returns error to client
>    - Client can retry later
>
> 2. **Notification Service Down**
>    - Booking is still created (non-critical)
>    - Events are queued in RabbitMQ
>    - Notifications sent when service recovers
>
> 3. **Catalog Service Down**
>    - Booking Service cannot verify offers
>    - Returns error to client
>
> **Resilience Strategies:**
> - Retry logic with exponential backoff
> - Circuit breaker pattern (could be added)
> - Fallback responses
> - RabbitMQ queues events for later processing"

---

### **Q8: How do you handle JWT authentication?**

**Answer:**
> "JWT flow:
>
> 1. User logs in: POST /users/login
> 2. User Service validates credentials
> 3. User Service generates JWT token (signed with secret key)
> 4. Frontend stores JWT in localStorage
> 5. Frontend sends JWT in Authorization header for all requests
> 6. Each service validates JWT independently using JwtAuthenticationFilter
> 7. If valid, request is processed with user context
> 8. If invalid, request is rejected with 401 Unauthorized
>
> **Security:**
> - JWT is signed with secret key (only server knows)
> - Cannot be forged by client
> - Expires after set time (e.g., 24 hours)
> - Passwords are hashed with BCrypt"

---

### **Q9: Why use RabbitMQ instead of direct REST calls?**

**Answer:**
> "RabbitMQ provides:
>
> 1. **Decoupling** - Services don't need to know about each other
> 2. **Resilience** - If Notification Service is down, events are queued
> 3. **Scalability** - Multiple consumers can process events
> 4. **Ordering** - Events are processed in order
> 5. **Retry Logic** - Failed events can be retried
>
> Example: Booking created
> - Without RabbitMQ: Booking Service must call Notification Service directly
>   - If Notification Service is down, booking fails
> - With RabbitMQ: Booking Service publishes event
>   - Notification Service listens when ready
>   - Booking succeeds regardless of Notification Service status"

---

### **Q10: How do you test microservices?**

**Answer:**
> "Three levels of testing:
>
> 1. **Unit Tests** (@SpringBootTest)
>    - Test individual services in isolation
>    - Mock repositories and clients
>    - Example: UserServiceTest
>
> 2. **Integration Tests** (@WebMvcTest)
>    - Test controllers with mocked services
>    - Test REST endpoints
>    - Example: UserControllerTest
>
> 3. **End-to-End Tests**
>    - Test complete flows across services
>    - Start all services
>    - Test booking creation flow
>    - Verify notifications sent
>
> **Mocking:**
> - Mock external service calls (UserServiceClient)
> - Mock RabbitMQ events
> - Use Mockito for mocking"

---

### **Q11: What design patterns did you use?**

**Answer:**
> "I used 10 real design patterns:
>
> 1. **Repository Pattern** - Abstracts data access
> 2. **Service Pattern** - Encapsulates business logic
> 3. **Controller Pattern** - Handles HTTP requests
> 4. **DTO Pattern** - Transfers data between layers
> 5. **Mapper Pattern** - Converts between entities and DTOs
> 6. **Builder Pattern** - Builds response objects
> 7. **Observer Pattern** - Event-driven architecture
> 8. **Interceptor Pattern** - Global exception handling
> 9. **Filter Pattern** - JWT authentication
> 10. **Wrapper Pattern** - Wraps all API responses
>
> I avoided forced patterns like 'Strategy Pattern' for service implementations, which is common but not necessary."

---

### **Q12: How do you apply SOLID principles?**

**Answer:**
> "All 5 SOLID principles:
>
> 1. **Single Responsibility**
>    - UserService only handles users
>    - WalletService only handles wallets
>    - Each class has one reason to change
>
> 2. **Open/Closed**
>    - Services are open for extension via interfaces
>    - Closed for modification
>    - Can add new implementations without changing existing code
>
> 3. **Liskov Substitution**
>    - All service implementations can substitute their interfaces
>    - UserServiceImpl can replace UserService
>
> 4. **Interface Segregation**
>    - Focused interfaces (UserService, WalletService)
>    - Not one large interface
>    - Clients depend only on what they need
>
> 5. **Dependency Inversion**
>    - Depend on abstractions (interfaces), not concrete classes
>    - Controllers depend on UserService interface
>    - Services depend on Repository interfaces"

---

### **Q13: What's your database design philosophy?**

**Answer:**
> "Microservices database isolation:
>
> 1. **Each service has its own database**
>    - No cross-database foreign keys
>    - Services communicate via REST API
>    - Loose coupling
>
> 2. **Elite-level optimizations:**
>    - Idempotency keys for retry safety
>    - Optimistic locking (version column) for concurrency
>    - Composite indexes for query performance
>    - UNIQUE constraints to prevent duplicates
>    - ENUM types for type safety
>    - Time slots (TIMESTAMP ranges) instead of just dates
>
> 3. **Example: Booking table**
>    - UNIQUE (service_offer_id, service_start, service_end) prevents double bookings
>    - idempotency_key prevents duplicate bookings from retries
>    - event_published tracks RabbitMQ events
>    - Composite indexes on (customer_id, status) for queries"

---

### **Q14: How do you handle errors?**

**Answer:**
> "Three-layer error handling:
>
> 1. **Custom Exceptions**
>    - UserNotFoundException
>    - InsufficientBalanceException
>    - BookingConflictException
>    - Each exception is meaningful
>
> 2. **Global Exception Handler** (@RestControllerAdvice)
>    - Catches all exceptions
>    - Returns consistent error response
>    - Logs errors for debugging
>
> 3. **Service-Level Error Handling**
>    - Try-catch for external service calls
>    - Publish error events
>    - Fallback responses
>
> **Example Response:**
> ```json
> {
>   \"status\": \"error\",
>   \"message\": \"Insufficient wallet balance\",
>   \"data\": {
>     \"required\": 50,
>     \"available\": 20
>   },
>   \"timestamp\": \"2026-05-01T10:30:00Z\"
> }
> ```"

---

### **Q15: What would you improve in production?**

**Answer:**
> "If this were production:
>
> 1. **Add API Gateway** - For multiple clients and centralized auth
> 2. **Add Circuit Breaker** - Prevent cascading failures
> 3. **Add Distributed Tracing** - Track requests across services
> 4. **Add Metrics & Monitoring** - Prometheus, Grafana
> 5. **Add Caching** - Redis for frequently accessed data
> 6. **Add Rate Limiting** - Prevent abuse
> 7. **Add Logging** - ELK stack for centralized logging
> 8. **Add Load Balancing** - Multiple instances per service
> 9. **Add Database Replication** - High availability
> 10. **Add Security** - OAuth2, API key management
>
> But for this assignment, the current architecture is sufficient and demonstrates solid understanding."

---

## 📋 Architecture Diagram to Draw

```
┌─────────────────────────────────────────────────────────┐
│                  React Frontend                         │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┬──────────────┐
        ↓                ↓                ↓              ↓
   ┌─────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐
   │  User   │    │ Booking  │    │ Catalog  │    │Notification │
   │Service  │    │ Service  │    │ Service  │    │ Service      │
   │(8081)   │    │ (8082)   │    │ (8083)   │    │ (8084)       │
   └────┬────┘    └────┬─────┘    └────┬─────┘    └──────────────┘
        │              │               │
        └──────────────┼───────────────┘
                       │
        (Direct REST calls between services)
        
        
        ┌─────────────────────────────────┐
        │      RabbitMQ (Event Bus)       │
        │  (Async event communication)    │
        └─────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
   Booking Events  User Events  Notification Events
```

---

## 🎯 Key Points to Emphasize

1. **Microservices Isolation** - Each service independent
2. **Event-Driven** - RabbitMQ for loose coupling
3. **Security** - JWT per service, password hashing
4. **Database Design** - Elite-level optimizations
5. **SOLID Principles** - Applied throughout
6. **Design Patterns** - Real patterns, not forced
7. **Error Handling** - Comprehensive and consistent
8. **Scalability** - Services scale independently
9. **Resilience** - Handles failures gracefully
10. **Production-Ready** - Professional architecture

---

## 💡 If Asked About Limitations

**Q: What are the limitations of your architecture?**

**Answer:**
> "Some limitations:
>
> 1. **Distributed Transactions** - No ACID across services
>    - Solution: Saga pattern or event sourcing
>
> 2. **Network Latency** - REST calls between services
>    - Solution: Caching, gRPC for performance
>
> 3. **Debugging** - Harder to trace requests across services
>    - Solution: Distributed tracing (Jaeger, Zipkin)
>
> 4. **Operational Complexity** - More services to manage
>    - Solution: Kubernetes, Docker
>
> 5. **Data Consistency** - Eventual consistency model
>    - Solution: Compensation transactions, event sourcing
>
> But these are trade-offs for the benefits of microservices:
> - Independent scaling
> - Technology diversity
> - Team autonomy
> - Fault isolation"

---

## 🎤 Practice Answers

**Practice saying these out loud:**

1. "Microservices allow independent scaling and deployment"
2. "RabbitMQ provides loose coupling and resilience"
3. "JWT authentication is stateless and scalable"
4. "UNIQUE constraints prevent double bookings"
5. "Idempotency keys prevent duplicate operations"
6. "Optimistic locking prevents race conditions"
7. "Event-driven architecture improves scalability"
8. "Each service has its own database for isolation"
9. "SOLID principles make code maintainable"
10. "Design patterns solve recurring problems"

---

## ✅ Pre-Viva Checklist

- [ ] Understand the architecture diagram
- [ ] Know why each service exists
- [ ] Understand service communication flows
- [ ] Know how JWT authentication works
- [ ] Understand RabbitMQ event flow
- [ ] Know database design decisions
- [ ] Understand SOLID principles
- [ ] Know design patterns used
- [ ] Understand error handling
- [ ] Know how to handle failures
- [ ] Practice explaining the system
- [ ] Be ready to draw diagrams
- [ ] Have code examples ready
- [ ] Know what you'd improve in production

---

## 🎓 Final Tips

1. **Be confident** - You've built a solid system
2. **Explain clearly** - Use diagrams and examples
3. **Know your code** - Be ready to show implementations
4. **Admit limitations** - Shows maturity
5. **Suggest improvements** - Shows forward thinking
6. **Ask clarifying questions** - If you don't understand
7. **Don't memorize** - Understand the concepts
8. **Be honest** - If you don't know, say so
9. **Show enthusiasm** - You're proud of your work
10. **Practice** - Rehearse your explanations

---

## 🚀 You've Got This!

Your architecture is solid, well-designed, and production-ready. You understand the concepts deeply. Go in there and explain it with confidence! 💪

Good luck! 🎉
