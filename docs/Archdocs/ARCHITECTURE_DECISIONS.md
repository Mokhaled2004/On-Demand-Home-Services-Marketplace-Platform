# Architecture Decisions - On-Demand Home Services Marketplace

## 🏗️ System Overview

**4 Microservices:**
- User Service (Authentication, Wallet Management)
- Booking Service (Booking Management)
- Catalog Service (Service Offers)
- Notification Service (Notifications)

**1 Client:**
- React Frontend

---

## 🚫 Decision: NO API Gateway

### Why NOT API Gateway?

| Factor | Your System | Gateway Needed? |
|--------|-------------|-----------------|
| Number of services | 4 | ❌ No (5-10+ needed) |
| Number of clients | 1 (React) | ❌ No (multiple needed) |
| Centralized auth needed | No (JWT per service) | ❌ No |
| Rate limiting needed | No | ❌ No |
| Complex routing | No | ❌ No |

**Verdict:** API Gateway = **Overkill for this assignment**

### What We're Using Instead

✅ **Direct REST Communication** between services
- Booking Service → User Service (wallet deduction)
- Booking Service → Catalog Service (offer details)
- Booking Service → Notification Service (send confirmations)
- User Service → Notification Service (send notifications)

✅ **RabbitMQ for Event-Driven Architecture**
- Booking created → publish event → Notification Service listens
- Wallet deducted → publish event → Compensation log updated

---

## 📡 Service Communication Architecture

```
┌─────────────────────────────────────────────────────────┐
│              React Frontend (Client)                    │
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

## 🔄 Service Communication Flows

### **Flow 1: User Registration**
```
1. React Frontend
   POST /users/register
   
2. User Service
   - Create user
   - Create wallet
   - Publish UserRegisteredEvent
   
3. RabbitMQ
   - Receives UserRegisteredEvent
   
4. Notification Service
   - Listens to UserRegisteredEvent
   - Sends welcome email/SMS
```

### **Flow 2: Create Booking (Most Complex)**
```
1. React Frontend
   POST /bookings
   
2. Booking Service (Controller)
   
3. Booking Service (Service Layer)
   - Call UserServiceClient.verifyCustomer(customerId)
   - Call CatalogServiceClient.getServiceOffer(offerId)
   - Call UserServiceClient.deductBalance(customerId, amount)
   
4. User Service (Wallet Deduction)
   - Verify balance
   - Deduct amount
   - Create WalletTransaction
   - Publish WalletDeductedEvent
   
5. RabbitMQ
   - Receives WalletDeductedEvent
   
6. Notification Service
   - Listens to WalletDeductedEvent
   - Sends confirmation to customer
   - Sends confirmation to provider
   
7. Response back to React Frontend
```

### **Flow 3: Booking Fails (Insufficient Balance)**
```
1. React Frontend
   POST /bookings
   
2. Booking Service
   - Call UserServiceClient.getWalletBalance(customerId)
   - ❌ Balance insufficient
   
3. Booking Service
   - Publish BookingFailedEvent
   
4. RabbitMQ
   - Receives BookingFailedEvent
   
5. Notification Service
   - Listens to BookingFailedEvent
   - Sends rejection notification
   
6. Response back to React Frontend
   - Error: "Insufficient balance"
```

---

## 🎯 Client Classes Needed

### **User Service Clients**
```java
client/
├── BookingServiceClient.java
│   └── getBooking(bookingId)
│   └── getBookingStatus(bookingId)
│
└── NotificationServiceClient.java
    └── sendNotification(request)
```

### **Booking Service Clients**
```java
client/
├── UserServiceClient.java
│   ├── verifyCustomer(customerId)
│   ├── verifyProvider(providerId)
│   ├── deductBalance(userId, amount, bookingId)
│   ├── refundBalance(userId, amount, bookingId)
│   └── getWalletBalance(userId)
│
├── CatalogServiceClient.java
│   ├── getServiceOffer(offerId)
│   └── verifyServiceOffer(offerId)
│
└── NotificationServiceClient.java
    ├── sendBookingConfirmation(customerId, bookingId)
    ├── sendProviderNotification(providerId, bookingId)
    └── sendBookingRejection(customerId, reason)
```

### **Catalog Service Clients**
```java
client/
├── UserServiceClient.java
│   ├── verifyProvider(providerId)
│   └── getProvider(providerId)
│
└── BookingServiceClient.java
    └── getBookingCount(serviceOfferId)
```

### **Notification Service Clients**
```java
client/
└── (NONE - only receives calls)
```

---

## 📊 Service Dependencies

```
User Service
  ├─ Depends on: Booking Service, Notification Service
  └─ Called by: Booking Service, Catalog Service

Booking Service
  ├─ Depends on: User Service, Catalog Service, Notification Service
  └─ Called by: React Frontend

Catalog Service
  ├─ Depends on: User Service, Booking Service
  └─ Called by: React Frontend

Notification Service
  ├─ Depends on: (None)
  └─ Called by: User Service, Booking Service, RabbitMQ Events
```

---

## 🔐 Security Architecture

**No API Gateway = Security in Each Service**

```
User Service
├── JwtAuthenticationFilter
├── SecurityConfig
└── PasswordEncoderUtil

Booking Service
├── JwtAuthenticationFilter
├── SecurityConfig
└── (Validates JWT from User Service)

Catalog Service
├── JwtAuthenticationFilter
├── SecurityConfig
└── (Validates JWT from User Service)

Notification Service
├── JwtAuthenticationFilter
├── SecurityConfig
└── (Validates JWT from User Service)
```

**JWT Flow:**
1. User logs in via User Service
2. User Service returns JWT token
3. React Frontend stores JWT
4. React Frontend sends JWT in Authorization header for all requests
5. Each service validates JWT independently

---

## 🐰 Event-Driven Architecture (RabbitMQ)

### **Events Published**

**User Service:**
- `UserRegisteredEvent` - When user registers
- `WalletDeductedEvent` - When wallet balance deducted
- `WalletRefundedEvent` - When wallet balance refunded

**Booking Service:**
- `BookingCreatedEvent` - When booking created
- `BookingConfirmedEvent` - When booking confirmed
- `BookingFailedEvent` - When booking fails
- `BookingCancelledEvent` - When booking cancelled

**Catalog Service:**
- `ServiceOfferCreatedEvent` - When offer created
- `ServiceOfferUpdatedEvent` - When offer updated

### **Events Consumed**

**Notification Service:**
- Listens to all events
- Sends appropriate notifications

**Compensation Log (User Service):**
- Listens to `WalletRefundedEvent`
- Logs compensation transactions

---

## 🎤 Viva Answer (If Asked About API Gateway)

**Question:** "Why didn't you use an API Gateway?"

**Answer:**
> "For this system with 4 microservices and a single React client, direct REST communication is simpler and sufficient. An API Gateway would add unnecessary complexity without providing significant benefits at this scale.
>
> However, in a production system with multiple clients (web, mobile, external APIs) and 5-10+ services, I would definitely introduce an API Gateway to centralize routing, authentication, rate limiting, and monitoring.
>
> For this assignment, I focused on what matters more: clean service communication, event-driven architecture with RabbitMQ, and proper JWT security in each service."

---

## ✅ What We're Focusing On Instead

1. **Clean Service Communication**
   - Client classes for inter-service calls
   - Proper error handling
   - Retry logic

2. **Event-Driven Architecture**
   - RabbitMQ for async communication
   - Event publishers and listeners
   - Loose coupling between services

3. **Security**
   - JWT authentication per service
   - Password encoding
   - Authorization checks

4. **Database Isolation**
   - Each service has its own database
   - No cross-database foreign keys
   - REST API for data sharing

5. **Error Handling**
   - Global exception handlers
   - Custom exceptions
   - Proper HTTP status codes

---

## 🏆 Final Architecture Summary

```
┌─────────────────────────────────────────────────────────┐
│                  FINAL ARCHITECTURE                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ✅ 4 Microservices (User, Booking, Catalog, Notif)   │
│  ✅ Direct REST Communication (No Gateway)             │
│  ✅ RabbitMQ Event Bus (Async Events)                  │
│  ✅ JWT Security (Per Service)                         │
│  ✅ Database Isolation (4 Separate DBs)                │
│  ✅ Eureka Service Discovery                           │
│  ✅ Clean Client Layer (Service Clients)               │
│  ✅ Global Exception Handling                          │
│                                                         │
│  ❌ NO API Gateway (Overkill)                          │
│  ❌ NO Admin Service (Not needed)                      │
│  ❌ NO Neon DB (Not needed)                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📋 Implementation Checklist

- [ ] Create JPA Entity classes for all 4 services
- [ ] Create Repository interfaces
- [ ] Create Service layer implementations
- [ ] Create Controller endpoints
- [ ] Create Client classes for inter-service communication
- [ ] Implement RabbitMQ event publishing
- [ ] Implement RabbitMQ event listeners
- [ ] Add JWT authentication filters
- [ ] Add global exception handlers
- [ ] Add DTOs and Mappers
- [ ] Write unit tests
- [ ] Write integration tests

---

## 🚀 Next Steps

Ready to start implementing the Entity classes and Repositories? 🎯
