# Implementation Roadmap

## 📋 Current Status

✅ **Completed:**
- All 4 database schemas designed (User, Catalog, Booking, Notification)
- All 4 ERDs created on Miro
- User Service DB created in PostgreSQL
- Catalog Service DB created in PostgreSQL
- Booking DB created in PostgreSQL
- Notification DB created in PostgreSQL
- User Service project structure designed with design patterns and SOLID principles
- Architecture decisions documented (NO API Gateway)
- Service communication flows documented

---

## 🚀 Implementation Phases

### **Phase 1: User Service (Foundation)**
**Duration:** ~2-3 days
**Priority:** CRITICAL (all other services depend on this)

#### 1.1 Entity Classes
- [ ] User.java
- [ ] Wallet.java
- [ ] WalletTransaction.java
- [ ] CompensationLog.java

#### 1.2 Repository Interfaces
- [ ] UserRepository.java
- [ ] WalletRepository.java
- [ ] WalletTransactionRepository.java
- [ ] CompensationLogRepository.java

#### 1.3 DTOs & Mappers
- [ ] RegisterRequest.java
- [ ] LoginRequest.java
- [ ] UserResponse.java
- [ ] LoginResponse.java
- [ ] WalletResponse.java
- [ ] AddFundsRequest.java
- [ ] DeductBalanceRequest.java
- [ ] UserMapper.java
- [ ] WalletMapper.java

#### 1.4 Service Layer
- [ ] UserService.java (interface)
- [ ] UserServiceImpl.java
- [ ] WalletService.java (interface)
- [ ] WalletServiceImpl.java
- [ ] CompensationService.java (interface)
- [ ] CompensationServiceImpl.java

#### 1.5 Controller Layer
- [ ] UserController.java
- [ ] WalletController.java

#### 1.6 Security & Exception Handling
- [ ] JwtTokenProvider.java
- [ ] PasswordEncoderUtil.java
- [ ] JwtAuthenticationFilter.java
- [ ] SecurityConfig.java
- [ ] UserNotFoundException.java
- [ ] InsufficientBalanceException.java
- [ ] UserAlreadyExistsException.java
- [ ] InvalidCredentialsException.java
- [ ] GlobalExceptionHandler.java

#### 1.7 Events
- [ ] UserRegisteredEvent.java
- [ ] UserEventListener.java
- [ ] WalletDeductedEvent.java
- [ ] WalletRefundedEvent.java

#### 1.8 Configuration
- [ ] RestTemplateConfig.java
- [ ] JpaAuditingConfig.java
- [ ] application.yml

#### 1.9 Tests
- [ ] UserControllerTest.java
- [ ] UserServiceTest.java
- [ ] WalletServiceTest.java
- [ ] UserRepositoryTest.java

---

### **Phase 2: Catalog Service (Independent)**
**Duration:** ~1-2 days
**Priority:** HIGH (needed for Booking Service)

#### 2.1 Entity Classes
- [ ] ServiceCategory.java
- [ ] ServiceOffer.java

#### 2.2 Repository Interfaces
- [ ] ServiceCategoryRepository.java
- [ ] ServiceOfferRepository.java

#### 2.3 DTOs & Mappers
- [ ] CreateCategoryRequest.java
- [ ] CategoryResponse.java
- [ ] CreateOfferRequest.java
- [ ] OfferResponse.java
- [ ] CategoryMapper.java
- [ ] OfferMapper.java

#### 2.4 Service Layer
- [ ] CategoryService.java (interface)
- [ ] CategoryServiceImpl.java
- [ ] OfferService.java (interface)
- [ ] OfferServiceImpl.java

#### 2.5 Controller Layer
- [ ] CategoryController.java
- [ ] OfferController.java

#### 2.6 Client Layer
- [ ] UserServiceClient.java (verify provider)
- [ ] BookingServiceClient.java (check availability)

#### 2.7 Exception Handling
- [ ] CategoryNotFoundException.java
- [ ] OfferNotFoundException.java
- [ ] GlobalExceptionHandler.java

#### 2.8 Configuration
- [ ] RestTemplateConfig.java
- [ ] application.yml

#### 2.9 Tests
- [ ] CategoryControllerTest.java
- [ ] OfferServiceTest.java
- [ ] OfferRepositoryTest.java

---

### **Phase 3: Booking Service (Complex)**
**Duration:** ~3-4 days
**Priority:** CRITICAL (main business logic)

#### 3.1 Entity Classes
- [ ] Booking.java

#### 3.2 Repository Interfaces
- [ ] BookingRepository.java

#### 3.3 DTOs & Mappers
- [ ] CreateBookingRequest.java
- [ ] BookingResponse.java
- [ ] BookingMapper.java

#### 3.4 Service Layer
- [ ] BookingService.java (interface)
- [ ] BookingServiceImpl.java (complex logic here)

#### 3.5 Controller Layer
- [ ] BookingController.java

#### 3.6 Client Layer (CRITICAL)
- [ ] UserServiceClient.java
  - verifyCustomer(customerId)
  - verifyProvider(providerId)
  - deductBalance(userId, amount, bookingId)
  - refundBalance(userId, amount, bookingId)
  - getWalletBalance(userId)
  
- [ ] CatalogServiceClient.java
  - getServiceOffer(offerId)
  - verifyServiceOffer(offerId)
  
- [ ] NotificationServiceClient.java
  - sendBookingConfirmation(customerId, bookingId)
  - sendProviderNotification(providerId, bookingId)
  - sendBookingRejection(customerId, reason)

#### 3.7 Exception Handling
- [ ] BookingNotFoundException.java
- [ ] InvalidBookingException.java
- [ ] BookingConflictException.java
- [ ] GlobalExceptionHandler.java

#### 3.8 Events
- [ ] BookingCreatedEvent.java
- [ ] BookingConfirmedEvent.java
- [ ] BookingFailedEvent.java
- [ ] BookingCancelledEvent.java
- [ ] BookingEventPublisher.java

#### 3.9 Configuration
- [ ] RestTemplateConfig.java
- [ ] application.yml

#### 3.10 Tests
- [ ] BookingControllerTest.java
- [ ] BookingServiceTest.java (complex scenarios)
- [ ] BookingRepositoryTest.java

---

### **Phase 4: Notification Service (Event-Driven)**
**Duration:** ~2 days
**Priority:** HIGH (async communication)

#### 4.1 Entity Classes
- [ ] Notification.java
- [ ] NotificationLog.java

#### 4.2 Repository Interfaces
- [ ] NotificationRepository.java
- [ ] NotificationLogRepository.java

#### 4.3 DTOs & Mappers
- [ ] NotificationRequest.java
- [ ] NotificationResponse.java
- [ ] NotificationMapper.java

#### 4.4 Service Layer
- [ ] NotificationService.java (interface)
- [ ] NotificationServiceImpl.java

#### 4.5 Controller Layer
- [ ] NotificationController.java

#### 4.6 Event Listeners (CRITICAL)
- [ ] UserRegisteredEventListener.java
- [ ] BookingCreatedEventListener.java
- [ ] BookingFailedEventListener.java
- [ ] WalletDeductedEventListener.java
- [ ] WalletRefundedEventListener.java

#### 4.7 Exception Handling
- [ ] NotificationNotFoundException.java
- [ ] NotificationSendException.java
- [ ] GlobalExceptionHandler.java

#### 4.8 Configuration
- [ ] RabbitMQConfig.java
- [ ] application.yml

#### 4.9 Tests
- [ ] NotificationControllerTest.java
- [ ] NotificationServiceTest.java
- [ ] EventListenerTest.java

---

### **Phase 5: RabbitMQ Integration**
**Duration:** ~1-2 days
**Priority:** HIGH (event-driven architecture)

#### 5.1 Event Classes (Shared)
- [ ] UserRegisteredEvent.java
- [ ] BookingCreatedEvent.java
- [ ] BookingFailedEvent.java
- [ ] WalletDeductedEvent.java
- [ ] WalletRefundedEvent.java

#### 5.2 Event Publishers
- [ ] UserEventPublisher.java (in User Service)
- [ ] BookingEventPublisher.java (in Booking Service)
- [ ] WalletEventPublisher.java (in User Service)

#### 5.3 Event Listeners
- [ ] NotificationEventListener.java (in Notification Service)
- [ ] CompensationEventListener.java (in User Service)

#### 5.4 RabbitMQ Configuration
- [ ] RabbitMQConfig.java (in each service)
- [ ] Queue definitions
- [ ] Exchange definitions
- [ ] Binding definitions

#### 5.5 Tests
- [ ] EventPublisherTest.java
- [ ] EventListenerTest.java
- [ ] RabbitMQIntegrationTest.java

---

### **Phase 6: Integration & Testing**
**Duration:** ~2-3 days
**Priority:** CRITICAL

#### 6.1 End-to-End Tests
- [ ] User registration flow
- [ ] Booking creation flow (happy path)
- [ ] Booking creation flow (insufficient balance)
- [ ] Wallet deduction and compensation flow

#### 6.2 Integration Tests
- [ ] Service-to-service communication
- [ ] RabbitMQ event flow
- [ ] Error handling and recovery

#### 6.3 Performance Tests
- [ ] Load testing
- [ ] Concurrent booking creation
- [ ] Wallet deduction under load

#### 6.4 Documentation
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Deployment guide
- [ ] Troubleshooting guide

---

## 📊 Dependency Graph

```
User Service (Foundation)
    ↓
    ├─→ Catalog Service (Independent)
    │       ↓
    │       └─→ Booking Service (Depends on User + Catalog)
    │               ↓
    │               └─→ Notification Service (Depends on all)
    │
    └─→ Booking Service (Depends on User)
            ↓
            └─→ Notification Service (Depends on all)

RabbitMQ Integration (Across all services)
```

---

## 🎯 Implementation Strategy

### **Week 1: Foundation**
- [ ] User Service (complete)
- [ ] Catalog Service (complete)
- [ ] Basic testing

### **Week 2: Core Business Logic**
- [ ] Booking Service (complete)
- [ ] Service-to-service communication
- [ ] Integration testing

### **Week 3: Event-Driven & Polish**
- [ ] Notification Service (complete)
- [ ] RabbitMQ integration
- [ ] End-to-end testing
- [ ] Documentation

---

## 🔍 Quality Checklist

For each service, ensure:

- [ ] All entities have proper JPA annotations
- [ ] All repositories extend JpaRepository
- [ ] All services have interfaces
- [ ] All controllers have proper HTTP methods
- [ ] All DTOs have validation annotations
- [ ] All exceptions are custom and meaningful
- [ ] All tests have >80% code coverage
- [ ] All code follows SOLID principles
- [ ] All code has proper logging
- [ ] All code has proper error handling
- [ ] All code has proper documentation
- [ ] All code is formatted consistently

---

## 🚀 Next Immediate Steps

1. **Start with User Service Entity Classes**
   - Create User.java
   - Create Wallet.java
   - Create WalletTransaction.java
   - Create CompensationLog.java

2. **Create Repositories**
   - UserRepository.java
   - WalletRepository.java
   - WalletTransactionRepository.java
   - CompensationLogRepository.java

3. **Create DTOs**
   - Request DTOs
   - Response DTOs
   - Mappers

4. **Create Service Layer**
   - UserService interface & implementation
   - WalletService interface & implementation
   - CompensationService interface & implementation

5. **Create Controllers**
   - UserController
   - WalletController

6. **Add Security**
   - JWT authentication
   - Password encoding
   - Exception handling

---

## 📝 Files to Create

**Total files to create: ~80-100**

- Entity classes: 10
- Repository interfaces: 10
- DTOs: 20
- Mappers: 5
- Service interfaces: 8
- Service implementations: 8
- Controllers: 8
- Client classes: 8
- Exception classes: 12
- Event classes: 8
- Configuration classes: 8
- Test classes: 20
- Other (filters, providers, etc.): 10

---

## ✅ Success Criteria

- [ ] All 4 services running independently
- [ ] All services registered with Eureka
- [ ] All services can communicate via REST
- [ ] All services validate JWT tokens
- [ ] RabbitMQ events published and consumed
- [ ] All endpoints tested and working
- [ ] All error scenarios handled
- [ ] All code documented
- [ ] All tests passing
- [ ] Ready for viva presentation

---

Ready to start? Let's begin with User Service Entity classes! 🚀
