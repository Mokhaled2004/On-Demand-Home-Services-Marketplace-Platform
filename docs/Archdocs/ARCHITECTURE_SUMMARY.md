# Architecture Summary - On-Demand Home Services Marketplace

## 🎯 System Overview

**Project:** On-Demand Home Services Marketplace  
**Architecture:** Microservices with Event-Driven Communication  
**Tech Stack:** Spring Boot, Spring Cloud, PostgreSQL, RabbitMQ, Eureka, JWT  
**Deployment:** Neon (PostgreSQL), Local RabbitMQ  

---

## 🏗️ Microservices Architecture

### **4 Independent Microservices**

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
```

---

## 📊 Service Responsibilities

### **1. User Service (Port 8081)**
**Responsibility:** User authentication, wallet management, compensation tracking

**Key Features:**
- User registration and login
- JWT token generation and validation
- Wallet balance management
- Wallet transactions tracking
- Compensation log for refunds
- Optimistic locking for concurrent wallet updates

**Database:** user_service_db (4 tables)
- users
- wallets
- wallet_transactions
- compensation_log

**Clients:**
- BookingServiceClient (verify bookings)
- NotificationServiceClient (send notifications)

---

### **2. Booking Service (Port 8082)**
**Responsibility:** Booking management and orchestration

**Key Features:**
- Create bookings
- Verify customer and provider
- Deduct wallet balance
- Prevent double bookings (UNIQUE constraint)
- Idempotency for retry safety
- Event publishing for async notifications

**Database:** booking_db (1 table)
- bookings

**Clients:**
- UserServiceClient (wallet operations)
- CatalogServiceClient (service offer details)
- NotificationServiceClient (send confirmations)

**Critical Flow:**
1. Verify customer exists
2. Verify provider exists
3. Get service offer details
4. Check wallet balance
5. Deduct balance
6. Create booking
7. Publish event
8. Send notifications

---

### **3. Catalog Service (Port 8083)**
**Responsibility:** Service categories and offers management

**Key Features:**
- Manage service categories
- Manage service offers
- Provider verification
- Availability tracking
- Case-insensitive category names

**Database:** catalog_service_db (2 tables)
- service_categories
- service_offers

**Clients:**
- UserServiceClient (verify providers)
- BookingServiceClient (check availability)

---

### **4. Notification Service (Port 8084)**
**Responsibility:** Async notification delivery

**Key Features:**
- Send notifications via REST API
- Track notification status
- Retry failed notifications
- Listen to RabbitMQ events
- Support multiple notification types

**Database:** notification_service_db (2 tables)
- notifications
- notification_logs

**Event Listeners:**
- UserRegisteredEvent
- BookingCreatedEvent
- BookingFailedEvent
- WalletDeductedEvent
- WalletRefundedEvent

---

## 🔄 Communication Patterns

### **Pattern 1: Synchronous REST Calls**
Used for critical operations that need immediate response.

**Example: Booking Creation**
```
Booking Service → User Service (deduct balance)
Booking Service → Catalog Service (get offer details)
```

**Error Handling:**
- Retry logic with exponential backoff
- Circuit breaker pattern
- Fallback responses

---

### **Pattern 2: Asynchronous Events (RabbitMQ)**
Used for non-critical operations that can be processed later.

**Example: Sending Notifications**
```
Booking Service → Publish BookingCreatedEvent
                ↓
            RabbitMQ
                ↓
        Notification Service (listens)
                ↓
        Send email/SMS to customer and provider
```

**Benefits:**
- Loose coupling between services
- Better scalability
- Resilience (if Notification Service is down, events are queued)

---

## 🔐 Security Architecture

### **JWT Authentication**
```
1. User logs in
   POST /users/login
   
2. User Service validates credentials
   
3. User Service generates JWT token
   
4. Frontend stores JWT
   
5. Frontend sends JWT in Authorization header
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   
6. Each service validates JWT independently
   JwtAuthenticationFilter
   
7. Request processed with user context
```

### **Password Security**
- Passwords hashed using BCrypt
- Never stored in plain text
- Validated on login

### **Service-to-Service Communication**
- Services call each other using RestTemplate
- No authentication between services (internal network)
- Could add service-to-service JWT if needed

---

## 📡 Key Flows

### **Flow 1: User Registration**
```
1. Frontend: POST /users/register
2. User Service: Create user, create wallet, publish event
3. RabbitMQ: Receives UserRegisteredEvent
4. Notification Service: Sends welcome email
5. Response: User created with JWT token
```

### **Flow 2: Create Booking (Happy Path)**
```
1. Frontend: POST /bookings
2. Booking Service: Verify customer, verify provider
3. Booking Service: Get service offer details
4. Booking Service: Check wallet balance
5. User Service: Deduct balance
6. Booking Service: Create booking, publish event
7. RabbitMQ: Receives BookingCreatedEvent
8. Notification Service: Send confirmations
9. Response: Booking created successfully
```

### **Flow 3: Create Booking (Insufficient Balance)**
```
1. Frontend: POST /bookings
2. Booking Service: Verify customer, verify provider
3. Booking Service: Get service offer details
4. User Service: Check wallet balance → INSUFFICIENT
5. Booking Service: Publish BookingFailedEvent
6. RabbitMQ: Receives BookingFailedEvent
7. Notification Service: Send rejection notification
8. Response: Error - insufficient balance
```

---

## 🗄️ Database Design

### **User Service DB**
```sql
users (id, username, email, password_hash, role, profession_type, created_at, updated_at)
wallets (id, user_id, balance, currency, version, created_at, updated_at)
wallet_transactions (id, wallet_id, transaction_type, amount, status, idempotency_key, created_at)
compensation_log (id, booking_id, user_id, transaction_id, action, amount, status, created_at)
```

### **Booking DB**
```sql
bookings (
  id, customer_id, provider_id, service_offer_id, 
  service_start, service_end, amount, status, 
  idempotency_key, event_published, created_at, updated_at
)
```

### **Catalog DB**
```sql
service_categories (id, name, description, created_at, updated_at)
service_offers (
  id, category_id, provider_id, title, description, 
  price, available_from, available_to, status, created_at, updated_at
)
```

### **Notification DB**
```sql
notifications (
  id, user_id, booking_id, type, title, message, 
  read_status, created_at, read_at
)
notification_logs (
  id, notification_id, status, sent_at, 
  error_message, retry_count, created_at
)
```

---

## 🎯 Design Patterns Used

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Repository Pattern** | repository/ | Abstracts data access |
| **Service Pattern** | service/ | Encapsulates business logic |
| **Controller Pattern** | controller/ | Handles HTTP requests |
| **DTO Pattern** | dto/ | Transfers data between layers |
| **Mapper Pattern** | dto/mapper/ | Converts between entities and DTOs |
| **Builder Pattern** | util/ApiResponseBuilder | Builds response objects |
| **Observer Pattern** | event/ | Event-driven architecture |
| **Interceptor Pattern** | exception/GlobalExceptionHandler | Handles exceptions |
| **Filter Pattern** | security/JwtAuthenticationFilter | Validates JWT tokens |
| **Wrapper Pattern** | dto/ApiResponse | Wraps all responses |

---

## ✅ SOLID Principles

| Principle | Implementation |
|-----------|-----------------|
| **S**ingle Responsibility | Each class has one reason to change |
| **O**pen/Closed | Services open for extension via interfaces |
| **L**iskov Substitution | All implementations can substitute interfaces |
| **I**nterface Segregation | Focused interfaces (UserService, WalletService) |
| **D**ependency Inversion | Depend on abstractions, not concrete classes |

---

## 🚀 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 3.x |
| **Cloud** | Spring Cloud | 2023.x |
| **Service Discovery** | Eureka | Netflix |
| **Database** | PostgreSQL | 14+ |
| **Message Queue** | RabbitMQ | 3.x |
| **Authentication** | JWT | jjwt |
| **ORM** | JPA/Hibernate | 6.x |
| **Build Tool** | Maven | 3.x |
| **Testing** | JUnit 5, Mockito | Latest |

---

## 📋 Project Structure

```
marketplace/
├── eureka-server/
│   └── (Service discovery)
│
├── user-service/
│   ├── src/main/java/com/marketplace/user/
│   │   ├── config/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── dto/
│   │   ├── service/
│   │   ├── controller/
│   │   ├── client/
│   │   ├── security/
│   │   ├── exception/
│   │   ├── event/
│   │   └── util/
│   └── src/main/resources/
│
├── booking-service/
│   └── (Similar structure)
│
├── catalog-service/
│   └── (Similar structure)
│
└── notification-service/
    └── (Similar structure)
```

---

## 🔄 Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Neon (PostgreSQL)                    │
│  ┌──────────────┬──────────────┬──────────────┐         │
│  │ user_service │ booking_db   │ catalog_db   │         │
│  │ notification │                              │         │
│  └──────────────┴──────────────┴──────────────┘         │
└─────────────────────────────────────────────────────────┘
                         ↑
        ┌────────────────┼────────────────┐
        │                │                │
   ┌─────────┐    ┌──────────┐    ┌──────────────┐
   │  User   │    │ Booking  │    │ Notification │
   │Service  │    │ Service  │    │ Service      │
   └─────────┘    └──────────┘    └──────────────┘
        │              │               │
        └──────────────┼───────────────┘
                       │
        ┌──────────────────────────────┐
        │    RabbitMQ (Local/Cloud)    │
        └──────────────────────────────┘
```

---

## 📊 API Endpoints Summary

### **User Service**
```
POST   /users/register
POST   /users/login
GET    /users/{userId}
PUT    /users/{userId}
DELETE /users/{userId}
GET    /wallet/{userId}
POST   /wallet/deduct
POST   /wallet/refund
POST   /wallet/add-funds
```

### **Booking Service**
```
POST   /bookings
GET    /bookings/{bookingId}
GET    /bookings/customer/{customerId}
GET    /bookings/provider/{providerId}
PUT    /bookings/{bookingId}
DELETE /bookings/{bookingId}
POST   /bookings/{bookingId}/confirm
POST   /bookings/{bookingId}/cancel
```

### **Catalog Service**
```
GET    /catalog/categories
POST   /catalog/categories
GET    /catalog/offers
POST   /catalog/offers
GET    /catalog/offers/{offerId}
PUT    /catalog/offers/{offerId}
DELETE /catalog/offers/{offerId}
```

### **Notification Service**
```
GET    /notifications
GET    /notifications/{notificationId}
POST   /notifications/send
PUT    /notifications/{notificationId}/read
GET    /notifications/user/{userId}
```

---

## 🎓 Learning Outcomes

By implementing this system, you'll learn:

✅ Microservices architecture principles  
✅ Service-to-service communication (REST)  
✅ Event-driven architecture (RabbitMQ)  
✅ JWT authentication and security  
✅ Database design for microservices  
✅ SOLID principles in practice  
✅ Design patterns in Spring Boot  
✅ Spring Cloud and Eureka  
✅ Testing microservices  
✅ Error handling and resilience  

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `ARCHITECTURE_DECISIONS.md` | Why we chose this architecture |
| `SERVICE_COMMUNICATION_GUIDE.md` | How services communicate |
| `IMPLEMENTATION_ROADMAP.md` | Step-by-step implementation plan |
| `USER_SERVICE_ELITE_STRUCTURE.md` | User Service project structure |
| `USER_SERVICE_DB_SCHEMA.md` | User Service database schema |
| `BOOKING_DB_SCHEMA.md` | Booking Service database schema |
| `SERVICE_CATALOG_DB_SCHEMA.md` | Catalog Service database schema |
| `NOTIFICATION_DB_SCHEMA.md` | Notification Service database schema |
| `DATABASE_SUMMARY.md` | Overview of all databases |

---

## 🎯 Next Steps

1. **Start with User Service**
   - Create entity classes
   - Create repositories
   - Create service layer
   - Create controllers

2. **Move to Catalog Service**
   - Similar structure to User Service
   - Add client for User Service

3. **Implement Booking Service**
   - Most complex service
   - Orchestrates other services
   - Handles wallet deduction

4. **Implement Notification Service**
   - Event listeners
   - RabbitMQ integration

5. **Integration & Testing**
   - End-to-end tests
   - Performance tests
   - Documentation

---

## ✨ Key Highlights

🔥 **What Makes This Architecture Strong:**

1. **Microservices Isolation** - Each service has its own database
2. **Event-Driven** - Loose coupling via RabbitMQ
3. **Security** - JWT authentication per service
4. **Scalability** - Services can scale independently
5. **Resilience** - Async events prevent cascading failures
6. **Clean Code** - SOLID principles throughout
7. **Professional** - Production-ready patterns
8. **Testable** - Easy to unit and integration test

---

## 🏆 Ready to Build!

You have a solid architecture. Now let's implement it! 🚀

**Start here:** `IMPLEMENTATION_ROADMAP.md`
