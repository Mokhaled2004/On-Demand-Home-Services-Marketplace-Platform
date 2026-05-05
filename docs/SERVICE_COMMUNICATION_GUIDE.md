# Service Communication Guide

## 🎯 Quick Reference: Who Calls Who?

### **User Service**
```
Outbound Calls:
├── Booking Service
│   └── getBooking(bookingId)
│   └── getBookingStatus(bookingId)
│
└── Notification Service
    └── sendNotification(request)

Inbound Calls From:
├── Booking Service (wallet operations)
├── Catalog Service (provider verification)
└── React Frontend (auth, user management)
```

### **Booking Service**
```
Outbound Calls:
├── User Service
│   ├── verifyCustomer(customerId)
│   ├── verifyProvider(providerId)
│   ├── deductBalance(userId, amount, bookingId)
│   ├── refundBalance(userId, amount, bookingId)
│   └── getWalletBalance(userId)
│
├── Catalog Service
│   ├── getServiceOffer(offerId)
│   └── verifyServiceOffer(offerId)
│
└── Notification Service
    ├── sendBookingConfirmation(customerId, bookingId)
    ├── sendProviderNotification(providerId, bookingId)
    └── sendBookingRejection(customerId, reason)

Inbound Calls From:
├── React Frontend (booking management)
└── Catalog Service (availability check)
```

### **Catalog Service**
```
Outbound Calls:
├── User Service
│   ├── verifyProvider(providerId)
│   └── getProvider(providerId)
│
└── Booking Service
    └── getBookingCount(serviceOfferId)

Inbound Calls From:
├── React Frontend (browse services)
└── Booking Service (get offer details)
```

### **Notification Service**
```
Outbound Calls:
└── (NONE - only receives calls)

Inbound Calls From:
├── User Service (send notifications)
├── Booking Service (send confirmations)
└── RabbitMQ Events (async notifications)
```

---

## 📡 REST Endpoints by Service

### **User Service (Port 8081)**
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
POST   /wallet/validate
```

### **Booking Service (Port 8082)**
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

### **Catalog Service (Port 8083)**
```
GET    /catalog/categories
POST   /catalog/categories
GET    /catalog/categories/{categoryId}

GET    /catalog/offers
POST   /catalog/offers
GET    /catalog/offers/{offerId}
PUT    /catalog/offers/{offerId}
DELETE /catalog/offers/{offerId}
GET    /catalog/offers/provider/{providerId}
```

### **Notification Service (Port 8084)**
```
GET    /notifications
GET    /notifications/{notificationId}
POST   /notifications/send
PUT    /notifications/{notificationId}/read
GET    /notifications/user/{userId}
```

---

## 🔄 Critical Flows

### **Flow 1: User Registration**
```
1. React Frontend
   POST /users/register
   {
     "username": "john_doe",
     "email": "john@example.com",
     "password": "secure_password",
     "role": "CUSTOMER"
   }

2. User Service
   - Validate input
   - Hash password
   - Create user
   - Create wallet (balance = 0)
   - Publish UserRegisteredEvent

3. RabbitMQ
   - Receives UserRegisteredEvent

4. Notification Service
   - Listens to UserRegisteredEvent
   - Sends welcome email

5. Response to Frontend
   {
     "status": "success",
     "data": {
       "userId": 1,
       "username": "john_doe",
       "email": "john@example.com",
       "token": "jwt_token_here"
     }
   }
```

### **Flow 2: Create Booking (Happy Path)**
```
1. React Frontend
   POST /bookings
   {
     "customerId": 1,
     "serviceOfferId": 5,
     "serviceDate": "2026-05-15",
     "serviceStartTime": "10:00",
     "serviceEndTime": "11:00"
   }

2. Booking Service (Controller)
   - Validate JWT token
   - Validate input

3. Booking Service (Service Layer)
   - Call UserServiceClient.verifyCustomer(1)
     ✅ Customer exists
   
   - Call CatalogServiceClient.getServiceOffer(5)
     ✅ Offer exists, price = $50
   
   - Call UserServiceClient.getWalletBalance(1)
     ✅ Balance = $100
   
   - Call UserServiceClient.deductBalance(1, 50, bookingId)
     ✅ Balance deducted, now $50
   
   - Create booking record
   - Publish BookingCreatedEvent

4. RabbitMQ
   - Receives BookingCreatedEvent

5. Notification Service
   - Listens to BookingCreatedEvent
   - Sends confirmation to customer
   - Sends notification to provider

6. Response to Frontend
   {
     "status": "success",
     "data": {
       "bookingId": 123,
       "status": "CONFIRMED",
       "amount": 50,
       "serviceDate": "2026-05-15"
     }
   }
```

### **Flow 3: Create Booking (Insufficient Balance)**
```
1. React Frontend
   POST /bookings
   {
     "customerId": 1,
     "serviceOfferId": 5,
     "serviceDate": "2026-05-15"
   }

2. Booking Service
   - Call UserServiceClient.verifyCustomer(1)
     ✅ Customer exists
   
   - Call CatalogServiceClient.getServiceOffer(5)
     ✅ Offer exists, price = $50
   
   - Call UserServiceClient.getWalletBalance(1)
     ❌ Balance = $20 (insufficient)
   
   - Publish BookingFailedEvent

3. RabbitMQ
   - Receives BookingFailedEvent

4. Notification Service
   - Listens to BookingFailedEvent
   - Sends rejection notification to customer

5. Response to Frontend
   {
     "status": "error",
     "message": "Insufficient wallet balance",
     "data": {
       "required": 50,
       "available": 20
     }
   }
```

### **Flow 4: Wallet Deduction with Compensation**
```
1. Booking Service
   - Call UserServiceClient.deductBalance(userId, amount, bookingId)

2. User Service
   - Verify balance
   - Deduct amount
   - Create WalletTransaction record
   - Publish WalletDeductedEvent

3. RabbitMQ
   - Receives WalletDeductedEvent

4. Compensation Log Listener (User Service)
   - Listens to WalletDeductedEvent
   - Creates CompensationLog entry
   - Tracks deduction for audit

5. Notification Service
   - Listens to WalletDeductedEvent
   - Sends transaction notification to user
```

---

## ⚠️ Error Handling

### **Service Call Failures**

**Scenario: Booking Service calls User Service, but User Service is down**

```java
// BookingServiceImpl.java
try {
    userServiceClient.deductBalance(userId, amount, bookingId);
} catch (ServiceUnavailableException e) {
    // User Service is down
    // Publish BookingFailedEvent
    bookingEventPublisher.publishBookingFailed(bookingId, "Service temporarily unavailable");
    
    // Return error to client
    throw new BookingException("Unable to process booking at this time");
}
```

**Scenario: Booking Service calls Notification Service, but it fails**

```java
// BookingServiceImpl.java
try {
    notificationClient.sendBookingConfirmation(customerId, bookingId);
} catch (NotificationServiceException e) {
    // Notification failed, but booking is already created
    // Log the error
    logger.error("Failed to send notification for booking: " + bookingId, e);
    
    // Publish event for retry
    notificationEventPublisher.publishNotificationRetry(bookingId);
    
    // Don't fail the booking - notification is non-critical
}
```

---

## 🔐 Security: JWT Token Flow

```
1. User logs in
   POST /users/login
   {
     "username": "john_doe",
     "password": "secure_password"
   }

2. User Service
   - Verify credentials
   - Generate JWT token
   - Return token

3. React Frontend
   - Stores JWT token in localStorage
   - Adds to Authorization header for all requests

4. Booking Service receives request
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   
5. JwtAuthenticationFilter
   - Extract token from header
   - Validate token signature
   - Extract userId from token
   - Set SecurityContext

6. Controller
   - Access userId from SecurityContext
   - Process request
```

---

## 📊 Service Ports

```
Eureka Server:        8761
User Service:         8081
Booking Service:      8082
Catalog Service:      8083
Notification Service: 8084
RabbitMQ:             5672 (AMQP), 15672 (Management)
PostgreSQL:           5432
```

---

## 🚀 Testing Service Communication

### **Test 1: User Registration**
```bash
curl -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

### **Test 2: Create Booking**
```bash
curl -X POST http://localhost:8082/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "serviceOfferId": 1,
    "serviceDate": "2026-05-15",
    "serviceStartTime": "10:00",
    "serviceEndTime": "11:00"
  }'
```

### **Test 3: Get Wallet Balance**
```bash
curl -X GET http://localhost:8081/wallet/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 Implementation Order

1. **User Service** (Foundation)
   - Entities, Repositories, Services
   - Controllers, DTOs, Mappers
   - JWT authentication

2. **Catalog Service** (Independent)
   - Entities, Repositories, Services
   - Controllers, DTOs, Mappers
   - User Service Client

3. **Booking Service** (Depends on User & Catalog)
   - Entities, Repositories, Services
   - Controllers, DTOs, Mappers
   - User Service Client
   - Catalog Service Client
   - Notification Service Client

4. **Notification Service** (Depends on RabbitMQ)
   - Entities, Repositories, Services
   - Controllers, DTOs, Mappers
   - RabbitMQ listeners

5. **RabbitMQ Integration**
   - Event publishers in each service
   - Event listeners in Notification Service
   - Event listeners in User Service (Compensation Log)

---

## ✅ Checklist

- [ ] All services registered with Eureka
- [ ] All services can communicate via REST
- [ ] JWT tokens validated in each service
- [ ] RabbitMQ events published and consumed
- [ ] Error handling in place
- [ ] Logging configured
- [ ] Tests written for each flow
