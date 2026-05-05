# User Service

Microservice responsible for user identity, authentication, and wallet management in the On-Demand Home Services Marketplace Platform.

---

## Overview

The User Service handles everything related to users and their money:
- User registration and authentication (JWT-based)
- Role-based access control (CUSTOMER, SERVICE_PROVIDER, ADMIN)
- Wallet management with full transaction history
- Idempotent deductions and refunds for booking payments
- Compensation logging for audit and rollback support

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5.14 |
| Language | Java 17 |
| Database | PostgreSQL (Neon serverless) |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (JJWT 0.12.3) |
| Service Discovery | Netflix Eureka Client |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- Eureka Server running on `localhost:8761` (optional - service works without it)
- Neon PostgreSQL database (already configured)

### Start
```bash
cd user-service
mvn spring-boot:run
```

Service starts on **http://localhost:8081**

### Build only
```bash
mvn clean compile -DskipTests
```

---

## Project Structure

```
user-service/
└── src/main/java/com/marketplace/user/
    ├── UserServiceApplication.java       # Entry point
    ├── client/
    │   ├── BookingServiceClient.java     # REST client for Booking Service
    │   └── NotificationServiceClient.java # REST client for Notification Service
    ├── config/
    │   ├── JpaAuditingConfig.java        # Enables @CreatedDate auditing
    │   ├── RestTemplateConfig.java       # RestTemplate bean
    │   └── SecurityConfig.java          # JWT filter chain + role-based access
    ├── controller/
    │   ├── UserController.java           # /users endpoints
    │   ├── WalletController.java         # /wallet endpoints
    │   └── AdminController.java          # /admin endpoints (ADMIN only)
    ├── dto/
    │   ├── request/
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── UpdateUserRequest.java
    │   │   ├── AddFundsRequest.java
    │   │   ├── DeductBalanceRequest.java
    │   │   ├── RefundRequest.java
    │   │   └── ValidateBalanceRequest.java
    │   └── response/
    │       ├── ApiResponse.java
    │       ├── UserResponse.java
    │       ├── LoginResponse.java
    │       ├── WalletResponse.java
    │       ├── TransactionResponse.java
    │       └── CompensationLogResponse.java
    ├── entity/
    │   ├── User.java
    │   ├── Wallet.java                   # Optimistic locking (@Version)
    │   ├── WalletTransaction.java        # Full transaction audit trail
    │   └── CompensationLog.java          # Booking deduction/refund log
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── UserNotFoundException.java
    │   ├── UserAlreadyExistsException.java
    │   ├── InvalidCredentialsException.java
    │   └── InsufficientBalanceException.java
    ├── mapper/
    │   ├── UserMapper.java
    │   └── WalletMapper.java
    ├── repository/
    │   ├── UserRepository.java
    │   ├── WalletRepository.java
    │   ├── WalletTransactionRepository.java
    │   └── CompensationLogRepository.java
    ├── security/
    │   ├── JwtTokenProvider.java         # Token generation and validation
    │   ├── JwtAuthenticationFilter.java  # Reads JWT on every request
    │   └── PasswordEncoderUtil.java      # BCrypt wrapper
    ├── service/
    │   ├── user/
    │   │   ├── UserService.java
    │   │   └── UserServiceImpl.java
    │   ├── wallet/
    │   │   ├── WalletService.java
    │   │   └── WalletServiceImpl.java
    │   └── compensation/
    │       ├── CompensationService.java
    │       └── CompensationServiceImpl.java
    └── util/
        └── ApiResponseBuilder.java
```

---

## API Endpoints

Base URL: `http://localhost:8081`

### User Management

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/users/register` | ❌ Public | Register a new user |
| POST | `/users/login` | ❌ Public | Login and receive JWT token |
| GET | `/users/{userId}` | ✅ JWT | Get user details by ID |
| PUT | `/users/{userId}` | ✅ JWT | Update email or profession type |
| DELETE | `/users/{userId}` | ✅ JWT | Delete user account |

### Wallet Management

> All wallet endpoints use the **authenticated user's ID from the JWT token**. You cannot operate on another user's wallet.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/wallet/me` | ✅ JWT | Get your own wallet balance |
| POST | `/wallet/add-funds` | ✅ JWT | Add funds to your wallet |
| POST | `/wallet/deduct` | ✅ JWT | Deduct balance for a booking |
| POST | `/wallet/refund` | ✅ JWT | Refund balance for a cancelled booking |
| POST | `/wallet/validate` | ✅ JWT | Check if you have sufficient balance |

### Admin Management

> Requires a JWT token from a user registered with `role: ADMIN`.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/users` | ✅ ADMIN | View all registered users |
| GET | `/admin/transactions` | ✅ ADMIN | View all wallet transactions |
| GET | `/admin/compensation-log` | ✅ ADMIN | View all compensation log entries |
| GET | `/admin/users/{userId}/transactions` | ✅ ADMIN | View transactions for a specific user |
| GET | `/admin/users/{userId}/compensation-log` | ✅ ADMIN | View compensation log for a specific user |

---

## Request & Response Examples

### Register User
```http
POST /users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer",
  "initialBalance": 200.00
}
```
> `role` options: `CUSTOMER`, `SERVICE_PROVIDER`, `ADMIN`
> `professionType` is optional, used for `SERVICE_PROVIDER` role

**Response `201`:**
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "professionType": "Software Engineer"
  },
  "timestamp": "2026-05-01T22:20:00"
}
```

---

### Login
```http
POST /users/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

**Response `200`:**
```json
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "role": "CUSTOMER"
  }
}
```
> Copy the `token` value and use it as `Bearer <token>` in the `Authorization` header for all protected endpoints.

---

### Add Funds
```http
POST /wallet/add-funds
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 100.00
}
```

---

### Deduct Balance (Booking Payment)
```http
POST /wallet/deduct
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 50.00,
  "bookingId": "BOOKING-12345"
}
```
> This also writes a `DEDUCT` entry to the `compensation_log` table for audit/rollback.

---

### Refund Balance (Booking Cancellation)
```http
POST /wallet/refund
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 50.00,
  "bookingId": "BOOKING-12345",
  "idempotencyKey": "REFUND-BOOKING-12345-001"
}
```
> This also writes a `REFUND` entry to the `compensation_log` table.

---

## Database Schema

The service uses 4 tables in `user_service_db` (Neon PostgreSQL):

| Table | Purpose |
|-------|---------|
| `users` | User identity, credentials, role |
| `wallets` | Current balance per user (1-to-1 with users) |
| `wallet_transactions` | Full audit trail of all money movements |
| `compensation_log` | Booking-related deductions and refunds for rollback support |

Key design decisions:
- `wallets.version` — optimistic locking to prevent race conditions on concurrent balance updates
- `wallet_transactions.idempotency_key` — unique constraint prevents duplicate charges on retries
- `compensation_log` — written on every `deduct` and `refund` so any booking failure can be traced and reversed
- `CHECK (balance >= 0)` — database-level constraint prevents negative balance

---

## Security

- Passwords are hashed with **BCrypt** before storage
- JWT tokens are signed with **HS512** and expire after 24 hours
- The token payload includes `userId`, `username`, and `role`
- The `JwtAuthenticationFilter` validates the token on every request and sets the Spring Security context
- Admin endpoints use `hasRole("ADMIN")` — only tokens with `role: ADMIN` can access `/admin/**`
- Wallet endpoints extract `userId` from the JWT — a user **cannot** manipulate another user's wallet

---

## Eureka Registration

The service registers itself with Eureka Server on startup:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
```

Service name in Eureka: **`user-service`**

Other services can discover it using this name via the Eureka registry.

---

## Environment Configuration

All configuration is in `src/main/resources/application.yaml`:

| Property | Value |
|----------|-------|
| Server port | `8081` |
| Database | Neon PostgreSQL (`user_service_db`) |
| Eureka server | `http://localhost:8761/eureka/` |
| JWT expiration | 24 hours (`86400000` ms) |
| DDL auto | `validate` (schema must exist in DB) |

---

## Inter-Service Communication

This service includes REST clients for future integration:

- `BookingServiceClient` → calls Booking Service at `http://localhost:8082`
- `NotificationServiceClient` → calls Notification Service at `http://localhost:8084`

These are used by the Booking Service to trigger wallet deductions and refunds during the booking flow.
