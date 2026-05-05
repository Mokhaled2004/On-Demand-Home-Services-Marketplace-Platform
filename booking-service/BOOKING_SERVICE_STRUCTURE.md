# Booking Service - Project Structure
## Jakarta EE / EJB on WildFly 40

```
booking-service/
│
├── src/main/java/com/marketplace/booking/
│   │
│   ├── BookingApplication.java
│   │   └── 📌 JAX-RS Application entry point | extends Application
│   │   └── 🎯 @ApplicationPath("/api") — all endpoints under /api
│   │
│   ├── entity/
│   │   └── Booking.java
│   │       └── 📌 JPA Entity | @Entity, @Table(name="bookings")
│   │       └── 🎯 id, customerId, serviceOfferId, providerId,
│   │               bookingDate, serviceStart, serviceEnd,
│   │               amount, status, idempotencyKey, eventPublished,
│   │               createdAt, updatedAt
│   │       └── 🔑 BookingStatus enum: PENDING, CONFIRMED, FAILED, CANCELLED
│   │
│   ├── repository/
│   │   └── BookingRepository.java
│   │       └── 📌 JPA data access | @Stateless (EJB)
│   │       └── 🎯 save, findById, findByCustomerId,
│   │               findByProviderId, findAll, updateStatus
│   │       └── ✅ Uses @PersistenceContext EntityManager
│   │
│   ├── ejb/
│   │   ├── BookingOrchestrationBean.java          ← EJB TYPE 1: @Stateless
│   │   │   └── 📌 Core booking business logic | @Stateless
│   │   │   └── 🎯 createBooking() — full orchestration flow:
│   │   │           1. Validate request
│   │   │           2. Call Catalog Service → get offer details + price
│   │   │           3. Call User Service   → verify customer exists
│   │   │           4. Call User Service   → deduct wallet balance
│   │   │           5. Save booking as CONFIRMED
│   │   │           6. Trigger event publisher
│   │   │           7. On failure → save as FAILED, trigger refund
│   │   │   └── 🎯 cancelBooking() — CONFIRMED → CANCELLED + refund
│   │   │   └── 🎯 getBooking(), getByCustomer(), getByProvider()
│   │   │   └── ✅ SOLID: Single Responsibility (only booking logic)
│   │   │   └── ✅ EJB: @TransactionAttribute(REQUIRED) on all methods
│   │   │
│   │   └── BookingEventPublisher.java             ← EJB TYPE 2: @Singleton
│   │       └── 📌 RabbitMQ connection manager + event publisher | @Singleton
│   │       └── 🎯 @PostConstruct init() — opens AMQP connection on startup
│   │       └── 🎯 @PreDestroy destroy() — closes connection on shutdown
│   │       └── 🎯 publishBookingConfirmed(bookingId, customerId, providerId, amount)
│   │       └── 🎯 publishBookingFailed(bookingId, customerId, reason)
│   │       └── 🎯 publishBookingCancelled(bookingId, customerId, amount)
│   │       └── ✅ @Lock(READ) on publish methods — allows concurrent publishing
│   │       └── ✅ Single shared RabbitMQ connection for the whole app
│   │       └── 🐇 Exchanges: booking.events (topic exchange)
│   │           Routing keys:
│   │             booking.confirmed
│   │             booking.failed
│   │             booking.cancelled
│   │
│   ├── rest/
│   │   └── BookingResource.java
│   │       └── 📌 JAX-RS REST endpoints | @Path("/bookings")
│   │       └── 🎯 POST   /api/bookings              → create booking
│   │       └── 🎯 GET    /api/bookings/{id}          → get booking by id
│   │       └── 🎯 GET    /api/bookings/customer/{id} → customer booking history
│   │       └── 🎯 GET    /api/bookings/provider/{id} → provider booking history
│   │       └── 🎯 GET    /api/bookings               → all bookings (admin)
│   │       └── 🎯 POST   /api/bookings/{id}/cancel   → cancel a booking
│   │       └── ✅ Injects BookingOrchestrationBean via @EJB
│   │       └── ✅ Reads JWT from Authorization header, forwards to User Service
│   │
│   ├── client/
│   │   ├── UserServiceClient.java
│   │   │   └── 📌 HTTP client for User Service (port 8081) | @ApplicationScoped
│   │   │   └── 🎯 getUser(userId)           → GET /users/{userId}
│   │   │   └── 🎯 deductWallet(amount, bookingId, jwtToken)
│   │   │                                    → POST /wallet/deduct
│   │   │   └── 🎯 refundWallet(amount, bookingId, idempotencyKey, jwtToken)
│   │   │                                    → POST /wallet/refund
│   │   │   └── ✅ Uses java.net.http.HttpClient (built-in, no extra deps)
│   │   │   └── ✅ Forwards JWT token in Authorization header
│   │   │
│   │   └── CatalogServiceClient.java
│   │       └── 📌 HTTP client for Catalog Service (port 8083) | @ApplicationScoped
│   │       └── 🎯 getServiceOffer(offerId, jwtToken)
│   │                                    → GET /services/{offerId}
│   │       └── 🎯 checkAvailability(offerId, jwtToken)
│   │                                    → GET /services/{offerId}/availability
│   │       └── ✅ Uses java.net.http.HttpClient
│   │
│   ├── dto/
│   │   ├── request/
│   │   │   └── CreateBookingRequest.java
│   │   │       └── 📌 Booking creation payload
│   │   │       └── 🎯 serviceOfferId, serviceStart, serviceEnd
│   │   │           (customerId comes from JWT token)
│   │   │
│   │   └── response/
│   │       ├── BookingResponse.java
│   │       │   └── 📌 Booking data returned to client
│   │       │   └── 🎯 id, customerId, serviceOfferId, providerId,
│   │       │           bookingDate, serviceStart, serviceEnd,
│   │       │           amount, status, createdAt
│   │       │
│   │       └── ApiResponse.java
│   │           └── 📌 Generic response wrapper
│   │           └── 🎯 success, message, data, timestamp
│   │
│   └── exception/
│       ├── BookingNotFoundException.java
│       │   └── 📌 Thrown when booking ID not found
│       │
│       ├── InsufficientBalanceException.java
│       │   └── 📌 Thrown when wallet deduction fails
│       │
│       ├── ServiceUnavailableException.java
│       │   └── 📌 Thrown when offer is not ACTIVE
│       │
│       └── BookingExceptionMapper.java
│           └── 📌 JAX-RS exception mapper | implements ExceptionMapper
│           └── 🎯 Maps exceptions to proper HTTP responses (404, 400, 503)
│           └── 🔐 Design Pattern: Interceptor Pattern
│
├── src/main/resources/
│   └── META-INF/
│       ├── persistence.xml
│       │   └── 📌 JPA config | persistence-unit name="bookingPU"
│       │   └── 🎯 Neon PostgreSQL connection, Hibernate dialect,
│       │           hbm2ddl.auto=update, HikariCP pool settings
│       │
│       └── beans.xml
│           └── 📌 CDI activation
│           └── 🎯 Enables CDI for @ApplicationScoped beans
│
├── pom.xml
│   └── 📌 Maven build config
│   └── 🎯 Jakarta EE APIs (provided), PostgreSQL driver,
│           Jackson JSON, RabbitMQ amqp-client (all bundled in WAR)
│
└── BOOKING_SERVICE_STRUCTURE.md  ← this file
```

---

## 🔑 Two EJB Types (Assignment Requirement)

| Bean | Type | Why This Type |
|------|------|---------------|
| `BookingOrchestrationBean` | **@Stateless** | No per-client state needed. Each booking request is independent. Container pools instances for performance. |
| `BookingEventPublisher` | **@Singleton** | One shared RabbitMQ connection for the whole application. @PostConstruct opens it once, @PreDestroy closes it cleanly. |

---

## 🐇 RabbitMQ — Booking Service is the PRODUCER

```
Booking Service (Producer)
        │
        │  AMQP connection via amqp-client library
        │  Host: localhost:5672
        │  Exchange: booking.events (topic)
        │
        ├── routing key: booking.confirmed  → payload: { bookingId, customerId, providerId, amount }
        ├── routing key: booking.failed     → payload: { bookingId, customerId, reason }
        └── routing key: booking.cancelled  → payload: { bookingId, customerId, amount }
                │
                ▼
          RabbitMQ Broker
                │
                ▼
    Notification Service (Consumer)  ← listens on these queues
```

The `BookingEventPublisher` (@Singleton EJB):
- Opens connection on `@PostConstruct`
- Declares the exchange on startup (idempotent)
- Publishes JSON messages using Jackson
- Closes connection on `@PreDestroy`

---

## 🔄 Booking Flow (inside BookingOrchestrationBean)

```
POST /api/bookings
  │
  ├─ 1. Parse JWT → extract customerId
  ├─ 2. CatalogServiceClient.getServiceOffer(offerId)
  │       → validates offer is ACTIVE, gets price + providerId
  ├─ 3. UserServiceClient.getUser(customerId)
  │       → validates customer exists
  ├─ 4. Save booking → status = PENDING
  ├─ 5. UserServiceClient.deductWallet(amount, bookingId, jwt)
  │       → deducts from customer wallet
  │
  ├─ SUCCESS PATH:
  │   ├─ 6. Update booking → status = CONFIRMED
  │   ├─ 7. BookingEventPublisher.publishBookingConfirmed(...)
  │   └─ 8. Return BookingResponse (CONFIRMED)
  │
  └─ FAILURE PATH (insufficient balance / service gone):
      ├─ 6. Update booking → status = FAILED
      ├─ 7. BookingEventPublisher.publishBookingFailed(...)
      └─ 8. Return 400 with reason
```

---

## 🌐 REST Endpoints Summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/bookings` | JWT (CUSTOMER) | Create a new booking |
| GET | `/api/bookings/{id}` | JWT | Get booking by ID |
| GET | `/api/bookings/customer/{customerId}` | JWT | Customer booking history |
| GET | `/api/bookings/provider/{providerId}` | JWT | Provider booking history |
| GET | `/api/bookings` | JWT (ADMIN) | All bookings |
| POST | `/api/bookings/{id}/cancel` | JWT (CUSTOMER) | Cancel a booking |

---

## 🔗 Inter-Service Calls

| Target | Endpoint | When |
|--------|----------|------|
| User Service `:8081` | `GET /users/{id}` | Verify customer exists |
| User Service `:8081` | `POST /wallet/deduct` | Deduct payment (JWT forwarded) |
| User Service `:8081` | `POST /wallet/refund` | Rollback on failure (JWT forwarded) |
| Catalog Service `:8083` | `GET /services/{offerId}` | Get price + provider + status |
| Catalog Service `:8083` | `GET /services/{offerId}/availability` | Double-check availability |

---

## 📦 Dependencies in pom.xml

| Dependency | Scope | Purpose |
|------------|-------|---------|
| `jakarta.ejb-api` | provided | EJB annotations (@Stateless, @Singleton) |
| `jakarta.persistence-api` | provided | JPA / @Entity |
| `jakarta.ws.rs-api` | provided | JAX-RS REST |
| `jakarta.enterprise.cdi-api` | provided | CDI @ApplicationScoped |
| `jakarta.transaction-api` | provided | @TransactionAttribute |
| `postgresql` | compile | JDBC driver (bundled in WAR) |
| `jackson-databind` | compile | JSON serialization for REST calls + RabbitMQ payloads |
| `jackson-datatype-jsr310` | compile | Java 8 date/time support in Jackson |
| `amqp-client` | compile | RabbitMQ AMQP producer |

---

## ✅ Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Repository Pattern** | `BookingRepository` | Abstracts JPA EntityManager |
| **Facade Pattern** | `BookingOrchestrationBean` | Single entry point for all booking logic |
| **Singleton Pattern** | `BookingEventPublisher` | One shared RabbitMQ connection |
| **DTO Pattern** | `dto/request`, `dto/response` | Clean data transfer between layers |
| **Wrapper Pattern** | `ApiResponse` | Consistent response envelope |
| **Interceptor Pattern** | `BookingExceptionMapper` | Maps exceptions to HTTP responses |
| **Client Pattern** | `UserServiceClient`, `CatalogServiceClient` | Encapsulates inter-service HTTP calls |
