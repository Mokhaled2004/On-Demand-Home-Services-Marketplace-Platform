# Service Catalog Service

Microservice responsible for managing service categories and service offers in the On-Demand Home Services Marketplace Platform.

---

## Overview

The Service Catalog Service handles everything related to service categories and offers:
- Admin management of service categories
- Provider creation and management of service offers
- Customer browsing and searching of available services
- JWT-based authentication and role-based access control
- Service availability checking

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
- Eureka Server running on `localhost:8761`
- Neon PostgreSQL database (already configured)

### Start
```bash
cd service-catalog-service
mvn spring-boot:run
```

Service starts on **http://localhost:8083**

### Build only
```bash
mvn clean compile -DskipTests
```

---

## Project Structure

```
service-catalog-service/
└── src/main/java/com/marketplace/user/
    ├── ServiceCatalogApplication.java    # Entry point
    ├── config/
    │   ├── JpaAuditingConfig.java        # Enables @CreatedDate auditing
    │   └── SecurityConfig.java           # JWT filter chain + role-based access
    ├── controller/
    │   ├── AdminCategoryController.java  # /admin/categories endpoints (ADMIN only)
    │   ├── ProviderOfferController.java  # /provider/offers endpoints (PROVIDER only)
    │   └── CustomerServiceController.java # /services endpoints (CUSTOMER browse)
    ├── dto/
    │   ├── request/
    │   │   ├── CreateCategoryRequest.java
    │   │   ├── UpdateCategoryRequest.java
    │   │   ├── CreateOfferRequest.java
    │   │   └── UpdateOfferRequest.java
    │   └── response/
    │       ├── ApiResponse.java
    │       ├── ServiceCategoryResponse.java
    │       ├── ServiceOfferResponse.java
    │       └── AvailabilityResponse.java
    ├── entity/
    │   ├── ServiceCategory.java
    │   └── ServiceOffer.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── CategoryNotFoundException.java
    │   ├── OfferNotFoundException.java
    │   ├── DuplicateCategoryException.java
    │   ├── UnauthorizedOfferAccessException.java
    │   └── InvalidOfferDataException.java
    ├── mapper/
    │   ├── ServiceCategoryMapper.java
    │   └── ServiceOfferMapper.java
    ├── repository/
    │   ├── ServiceCategoryRepository.java
    │   └── ServiceOfferRepository.java
    ├── security/
    │   ├── JwtTokenProvider.java         # Token validation (same secret as User Service)
    │   └── JwtAuthenticationFilter.java  # Reads JWT on every request
    ├── service/
    │   ├── category/
    │   │   ├── ServiceCategoryService.java
    │   │   └── ServiceCategoryServiceImpl.java
    │   └── offer/
    │       ├── ServiceOfferService.java
    │       └── ServiceOfferServiceImpl.java
    └── util/
        └── ApiResponseBuilder.java
```

---

## API Endpoints

Base URL: `http://localhost:8083`

### Admin Category Management (ADMIN only)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/admin/categories` | ✅ ADMIN | Create new service category |
| GET | `/admin/categories` | ✅ ADMIN | List all categories |
| GET | `/admin/categories/{categoryId}` | ✅ ADMIN | Get single category |
| PUT | `/admin/categories/{categoryId}` | ✅ ADMIN | Update category |
| DELETE | `/admin/categories/{categoryId}` | ✅ ADMIN | Delete category (soft delete) |

### Provider Offer Management (PROVIDER only)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/provider/offers` | ✅ PROVIDER | Create new service offer |
| GET | `/provider/offers` | ✅ PROVIDER | List own offers |
| GET | `/provider/offers/{offerId}` | ✅ PROVIDER | Get single offer |
| PUT | `/provider/offers/{offerId}` | ✅ PROVIDER | Update own offer |
| DELETE | `/provider/offers/{offerId}` | ✅ PROVIDER | Delete own offer (soft delete) |

### Customer Service Browsing (CUSTOMER or any authenticated user)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/services` | ✅ JWT | Browse all active services |
| GET | `/services/category/{categoryId}` | ✅ JWT | Browse services by category |
| GET | `/services/search` | ✅ JWT | Search services by keyword/price |
| GET | `/services/{offerId}` | ✅ JWT | Get service details |
| GET | `/services/{offerId}/availability` | ✅ JWT | Check service availability |

---

## Request & Response Examples

### Create Category (Admin)
```http
POST /admin/categories
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Plumbing",
  "description": "Pipe repair and installation services"
}
```

**Response `201`:**
```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    "id": 1,
    "name": "Plumbing",
    "description": "Pipe repair and installation services",
    "createdAt": "2026-05-02T10:00:00",
    "updatedAt": "2026-05-02T10:00:00"
  },
  "timestamp": "2026-05-02T10:00:00"
}
```

---

### Create Service Offer (Provider)
```http
POST /provider/offers
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "categoryId": 1,
  "title": "Pipe Repair",
  "description": "Fix leaking pipes",
  "price": 80.00,
  "availableFrom": "2026-05-15T09:00:00",
  "availableTo": "2026-05-15T17:00:00"
}
```

**Response `201`:**
```json
{
  "success": true,
  "message": "Offer created successfully",
  "data": {
    "id": 100,
    "providerId": 5,
    "categoryId": 1,
    "title": "Pipe Repair",
    "description": "Fix leaking pipes",
    "price": 80.00,
    "availableFrom": "2026-05-15T09:00:00",
    "availableTo": "2026-05-15T17:00:00",
    "status": "ACTIVE",
    "createdAt": "2026-05-02T10:00:00",
    "updatedAt": "2026-05-02T10:00:00"
  },
  "timestamp": "2026-05-02T10:00:00"
}
```

---

### Browse Services by Category (Customer)
```http
GET /services/category/1
Authorization: Bearer <jwt_token>
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": 100,
      "providerId": 5,
      "categoryId": 1,
      "title": "Pipe Repair",
      "description": "Fix leaking pipes",
      "price": 80.00,
      "availableFrom": "2026-05-15T09:00:00",
      "availableTo": "2026-05-15T17:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-05-02T10:00:00",
      "updatedAt": "2026-05-02T10:00:00"
    }
  ],
  "timestamp": "2026-05-02T10:00:00"
}
```

---

### Search Services (Customer)
```http
GET /services/search?keyword=pipe&minPrice=50&maxPrice=150
Authorization: Bearer <jwt_token>
```

---

## Database Schema

The service uses 2 tables in `service_catalog_db` (Neon PostgreSQL):

| Table | Purpose |
|-------|---------|
| `service_categories` | Service categories (Plumbing, Carpentry, etc.) |
| `service_offers` | Service offers created by providers |

Key design decisions:
- `service_categories.name` — UNIQUE constraint with LOWER() for case-insensitive uniqueness
- `service_offers.provider_id` — NOT a foreign key (references User Service database)
- `service_offers.category_id` — foreign key to service_categories
- `service_offers.status` — ACTIVE or INACTIVE (soft delete pattern)
- `CHECK (price > 0)` — ensures positive prices
- `CHECK (available_to > available_from)` — ensures valid time ranges

---

## Security

- JWT tokens are validated locally using the same secret key as User Service
- The token payload includes `userId`, `username`, and `role`
- The `JwtAuthenticationFilter` validates the token on every request and sets the Spring Security context
- Admin endpoints use `@PreAuthorize("hasRole('ADMIN')")` — only ADMIN role can access
- Provider endpoints use `@PreAuthorize("hasRole('SERVICE_PROVIDER')")` — only PROVIDER role can access
- Customer endpoints use `@PreAuthorize("isAuthenticated()")` — any authenticated user can access
- Provider can only modify their own offers — `provider_id` is extracted from JWT token

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

Service name in Eureka: **`service-catalog-service`**

Other services can discover it using this name via the Eureka registry.

---

## Environment Configuration

All configuration is in `src/main/resources/application.yaml`:

| Property | Value |
|----------|-------|
| Server port | `8083` |
| Database | Neon PostgreSQL (`service_catalog_db`) |
| Eureka server | `http://localhost:8761/eureka/` |
| JWT expiration | 24 hours (same as User Service) |
| DDL auto | `validate` (schema must exist in DB) |

---

## JWT Token Validation

This service validates JWT tokens locally without calling the User Service:

1. Token is extracted from `Authorization: Bearer <token>` header
2. Token signature is verified using the same secret key as User Service
3. Token expiration is checked
4. `userId`, `username`, and `role` are extracted from token claims
5. Spring Security context is set with the user's role

This ensures fast, independent operation without inter-service dependencies.
