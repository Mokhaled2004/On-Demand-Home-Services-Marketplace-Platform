# Service Catalog - Elite Project Structure
## Following SOLID Principles, Design Patterns & Best Practices

```
service-catalog/
│
├── src/main/java/com/marketplace/catalog/
│   │
│   ├── ServiceCatalogApplication.java
│   │   └── 📌 Entry point | @SpringBootApplication, @EnableDiscoveryClient
│   │   └── 🎯 Bootstraps Spring context and registers with Eureka
│   │
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   │   └── 📌 Security configuration | @Configuration
│   │   │   └── 🎯 JWT token validation, CORS setup, role-based access control
│   │   │   └── 🔐 Returns JSON (not HTML) for 401/403 errors
│   │   │
│   │   └── JpaAuditingConfig.java
│   │       └── 📌 JPA auditing configuration | @Configuration
│   │       └── 🎯 Auto-populate createdAt, updatedAt fields
│   │
│   ├── entity/
│   │   ├── ServiceCategory.java
│   │   │   └── 📌 Service category entity | @Entity, @Table
│   │   │   └── 🎯 id, name (UNIQUE case-insensitive), description, created_at, updated_at
│   │   │   └── ✅ SOLID: Single Responsibility (only represents category data)
│   │   │
│   │   └── ServiceOffer.java
│   │       └── 📌 Service offer entity | @Entity, @Table
│   │       └── 🎯 id, provider_id (NOT FK), category_id (FK), title, description, price, available_from, available_to, status, created_at, updated_at
│   │       └── ✅ SOLID: Single Responsibility (only represents offer data)
│   │       └── 🔐 CHECK constraints: price > 0, available_to > available_from
│   │
│   ├── repository/
│   │   ├── ServiceCategoryRepository.java
│   │   │   └── 📌 Category data access | extends JpaRepository<ServiceCategory, Long>
│   │   │   └── 🎯 findByNameIgnoreCase, custom queries
│   │   │   └── ✅ SOLID: Dependency Inversion (depends on abstraction)
│   │   │
│   │   └── ServiceOfferRepository.java
│   │       └── 📌 Offer data access | extends JpaRepository<ServiceOffer, Long>
│   │       └── 🎯 findByProviderId, findByCategoryId, findByStatus, custom queries
│   │       └── ✅ SOLID: Dependency Inversion
│   │
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateCategoryRequest.java
│   │   │   │   └── 📌 Create category request | @Data, @Validated
│   │   │   │   └── 🎯 name, description
│   │   │   │   └── ✅ SOLID: Single Responsibility (only request data)
│   │   │   │
│   │   │   ├── UpdateCategoryRequest.java
│   │   │   │   └── 📌 Update category request | @Data, @Validated
│   │   │   │   └── 🎯 name, description
│   │   │   │   └── ✅ SOLID: Single Responsibility
│   │   │   │
│   │   │   ├── CreateOfferRequest.java
│   │   │   │   └── 📌 Create offer request | @Data, @Validated
│   │   │   │   └── 🎯 categoryId, title, description, price, available_from, available_to
│   │   │   │   └── ✅ SOLID: Single Responsibility
│   │   │   │
│   │   │   └── UpdateOfferRequest.java
│   │   │       └── 📌 Update offer request | @Data, @Validated
│   │   │       └── 🎯 title, description, price, available_from, available_to, status
│   │   │       └── ✅ SOLID: Single Responsibility
│   │   │
│   │   ├── response/
│   │   │   ├── ServiceCategoryResponse.java
│   │   │   │   └── 📌 Category response | @Data
│   │   │   │   └── 🎯 id, name, description, created_at, updated_at
│   │   │   │   └── ✅ SOLID: Single Responsibility (only response data)
│   │   │   │
│   │   │   ├── ServiceOfferResponse.java
│   │   │   │   └── 📌 Offer response | @Data
│   │   │   │   └── 🎯 id, provider_id, category_id, title, description, price, available_from, available_to, status, created_at, updated_at
│   │   │   │   └── ✅ SOLID: Single Responsibility
│   │   │   │
│   │   │   ├── AvailabilityResponse.java
│   │   │   │   └── 📌 Availability response | @Data
│   │   │   │   └── 🎯 available_from, available_to, status
│   │   │   │   └── ✅ SOLID: Single Responsibility
│   │   │   │
│   │   │   └── ApiResponse.java
│   │   │       └── 📌 Generic API response wrapper | @Data, @Generic<T>
│   │   │       └── 🎯 success, message, data, timestamp
│   │   │       └── 🔐 Design Pattern: Wrapper Pattern (wraps all responses)
│   │   │       └── ✅ SOLID: Single Responsibility (only wraps responses)
│   │   │
│   │   └── mapper/
│   │       ├── ServiceCategoryMapper.java
│   │       │   └── 📌 Category entity ↔ DTO mapper | @Component
│   │       │   └── 🎯 toDTO, toEntity, update methods (manual mapping)
│   │       │   └── 🔐 Design Pattern: Mapper Pattern (converts between layers)
│   │       │   └── ✅ SOLID: Single Responsibility (only mapping)
│   │       │
│   │       └── ServiceOfferMapper.java
│   │           └── 📌 Offer entity ↔ DTO mapper | @Component
│   │           └── 🎯 toDTO, toEntity methods (manual mapping)
│   │           └── 🔐 Design Pattern: Mapper Pattern
│   │           └── ✅ SOLID: Single Responsibility
│   │
│   ├── service/
│   │   ├── category/
│   │   │   ├── ServiceCategoryService.java (Interface)
│   │   │   │   └── 📌 Category business logic contract | interface
│   │   │   │   └── 🎯 createCategory, getCategory, getAllCategories, updateCategory, deleteCategory (soft), hardDeleteCategory
│   │   │   │   └── ✅ SOLID: Dependency Inversion (depend on abstraction)
│   │   │   │   └── ✅ SOLID: Interface Segregation (focused interface)
│   │   │   │
│   │   │   └── ServiceCategoryServiceImpl.java
│   │   │       └── 📌 Category business logic implementation | @Service, @Transactional
│   │   │       └── 🎯 Implements ServiceCategoryService interface
│   │   │       └── ✅ SOLID: Single Responsibility (only category logic)
│   │   │       └── ✅ SOLID: Open/Closed (open for extension via interface)
│   │   │
│   │   └── offer/
│   │       ├── ServiceOfferService.java (Interface)
│   │       │   └── 📌 Offer business logic contract | interface
│   │       │   └── 🎯 createOffer, getOffer, getOffersByProvider, getOffersByCategory, searchOffers, updateOffer, deleteOffer (soft), hardDeleteOffer, getAvailability
│   │       │   └── ✅ SOLID: Dependency Inversion
│   │       │   └── ✅ SOLID: Interface Segregation
│   │       │
│   │       └── ServiceOfferServiceImpl.java
│   │           └── 📌 Offer business logic implementation | @Service, @Transactional
│   │           └── 🎯 Implements ServiceOfferService interface
│   │           └── ✅ SOLID: Single Responsibility (only offer logic)
│   │           └── ✅ SOLID: Open/Closed
│   │
│   ├── controller/
│   │   ├── AdminCategoryController.java
│   │   │   └── 📌 Admin category REST endpoints | @RestController, @RequestMapping("/admin/categories")
│   │   │   └── 🎯 POST (create), GET (list), GET /{id}, PUT /{id}, DELETE /{id}
│   │   │   └── 🔐 @PreAuthorize("hasRole('ADMIN')")
│   │   │   └── ✅ SOLID: Single Responsibility (only admin category endpoints)
│   │   │
│   │   ├── ProviderOfferController.java
│   │   │   └── 📌 Provider offer REST endpoints | @RestController, @RequestMapping("/provider/offers")
│   │   │   └── 🎯 POST (create), GET (list own), GET /{id}, PUT /{id}, DELETE /{id}
│   │   │   └── 🔐 @PreAuthorize("hasRole('SERVICE_PROVIDER')")
│   │   │   └── ✅ SOLID: Single Responsibility (only provider offer endpoints)
│   │   │
│   │   └── CustomerServiceController.java
│   │       └── 📌 Customer service browse endpoints | @RestController, @RequestMapping("/services")
│   │       └── 🎯 GET (all), GET /category/{id}, GET /search, GET /{id}, GET /{id}/availability
│   │       └── 🔐 @PreAuthorize("isAuthenticated()")
│   │       └── ✅ SOLID: Single Responsibility (only customer browse endpoints)
│   │
│   ├── exception/
│   │   ├── CategoryNotFoundException.java
│   │   │   └── 📌 Custom exception | extends RuntimeException
│   │   │   └── 🎯 Thrown when category not found
│   │   │
│   │   ├── OfferNotFoundException.java
│   │   │   └── 📌 Custom exception | extends RuntimeException
│   │   │   └── 🎯 Thrown when offer not found
│   │   │
│   │   ├── DuplicateCategoryException.java
│   │   │   └── 📌 Custom exception | extends RuntimeException
│   │   │   └── 🎯 Thrown on duplicate category name
│   │   │
│   │   ├── UnauthorizedOfferAccessException.java
│   │   │   └── 📌 Custom exception | extends RuntimeException
│   │   │   └── 🎯 Thrown when provider tries to modify another's offer
│   │   │
│   │   ├── InvalidOfferDataException.java
│   │   │   └── 📌 Custom exception | extends RuntimeException
│   │   │   └── 🎯 Thrown on invalid offer data (negative price, invalid dates)
│   │   │
│   │   └── GlobalExceptionHandler.java
│   │       └── 📌 Global exception handler | @RestControllerAdvice
│   │       └── 🎯 Handles all exceptions, returns consistent error responses
│   │       └── 🔐 Design Pattern: Interceptor Pattern (intercepts exceptions)
│   │       └── ✅ SOLID: Single Responsibility (only exception handling)
│   │       └── 📊 Handles: 400, 401, 403, 404, 409, 500 status codes
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   │   └── 📌 JWT token validation | @Component
│   │   │   └── 🎯 validateToken, extractUserId, extractUsername, extractRole
│   │   │   └── 🔐 Uses same secret key as User Service
│   │   │   └── ✅ SOLID: Single Responsibility (only JWT logic)
│   │   │
│   │   └── JwtAuthenticationFilter.java
│   │       └── 📌 JWT authentication filter | extends OncePerRequestFilter
│   │       └── 🎯 Validates JWT token on each request, sets Spring Security context
│   │       └── ✅ SOLID: Single Responsibility (only JWT validation)
│   │
│   └── util/
│       ├── ApiResponseBuilder.java
│       │   └── 📌 API response builder | @Component
│       │   └── 🎯 buildSuccess, buildError
│       │   └── 🔐 Design Pattern: Builder Pattern (builds responses)
│       │   └── ✅ SOLID: Single Responsibility (only response building)
│       │
│       └── Constants.java
│           └── 📌 Application constants | final class
│           └── 🎯 Error messages, status codes, default values
│           └── ✅ SOLID: Single Responsibility (only constants)
│
├── src/main/resources/
│   ├── application.yml
│   │   └── 📌 Main configuration
│   │   └── 🎯 Spring app name, database, Eureka, logging, port 8083
│   │
│   ├── application-dev.yml
│   │   └── 📌 Development configuration
│   │   └── 🎯 Dev-specific settings
│   │
│   ├── application-prod.yml
│   │   └── 📌 Production configuration
│   │   └── 🎯 Prod-specific settings
│   │
│   └── db/migration/
│       ├── V1__create_service_categories_table.sql
│       │   └── 📌 Flyway migration | Version 1
│       │   └── 🎯 Creates service_categories table with UNIQUE(LOWER(name))
│       │
│       └── V2__create_service_offers_table.sql
│           └── 📌 Flyway migration | Version 2
│           └── 🎯 Creates service_offers table with CHECK constraints and indexes
│
├── src/test/java/com/marketplace/catalog/
│   ├── controller/
│   │   ├── AdminCategoryControllerTest.java
│   │   │   └── 📌 Admin category controller tests | @WebMvcTest
│   │   │   └── 🎯 Tests all admin category endpoints
│   │   │   └── 🔐 Design Pattern: Test Pattern (unit tests)
│   │   │
│   │   ├── ProviderOfferControllerTest.java
│   │   │   └── 📌 Provider offer controller tests | @WebMvcTest
│   │   │   └── 🎯 Tests all provider offer endpoints
│   │   │   └── 🔐 Design Pattern: Test Pattern
│   │   │
│   │   └── CustomerServiceControllerTest.java
│   │       └── 📌 Customer service controller tests | @WebMvcTest
│   │       └── 🎯 Tests all customer browse endpoints
│   │       └── 🔐 Design Pattern: Test Pattern
│   │
│   ├── service/
│   │   ├── ServiceCategoryServiceTest.java
│   │   │   └── 📌 Category service tests | @SpringBootTest
│   │   │   └── 🎯 Tests category business logic
│   │   │   └── 🔐 Design Pattern: Test Pattern
│   │   │
│   │   └── ServiceOfferServiceTest.java
│   │       └── 📌 Offer service tests | @SpringBootTest
│   │       └── 🎯 Tests offer business logic
│   │       └── 🔐 Design Pattern: Test Pattern
│   │
│   └── repository/
│       ├── ServiceCategoryRepositoryTest.java
│       │   └── 📌 Category repository tests | @DataJpaTest
│       │   └── 🎯 Tests category data access
│       │   └── 🔐 Design Pattern: Test Pattern
│       │
│       └── ServiceOfferRepositoryTest.java
│           └── 📌 Offer repository tests | @DataJpaTest
│           └── 🎯 Tests offer data access
│           └── 🔐 Design Pattern: Test Pattern
│
├── pom.xml
│   └── 📌 Maven configuration
│   └── 🎯 Dependencies, plugins, properties
│
├── README.md
│   └── 📌 Project documentation
│   └── 🎯 Setup, API docs, examples
│
└── .gitignore
    └── 📌 Git ignore rules
    └── 🎯 Exclude target/, .idea/, etc.
```

---

## 🎯 Design Patterns Used (REAL, Not Forced)

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Repository Pattern** | `repository/` | Abstracts data access layer |
| **Service Pattern** | `service/` | Encapsulates business logic |
| **Controller Pattern** | `controller/` | Handles HTTP requests |
| **DTO Pattern** | `dto/` | Transfers data between layers |
| **Mapper Pattern** | `dto/mapper/` | Converts between entities and DTOs |
| **Builder Pattern** | `util/ApiResponseBuilder.java` | Builds complex response objects |
| **Interceptor Pattern** | `exception/GlobalExceptionHandler.java` | Intercepts and handles exceptions |
| **Filter Pattern** | `security/JwtAuthenticationFilter.java` | Filters HTTP requests for authentication |
| **Wrapper Pattern** | `dto/ApiResponse.java` | Wraps all API responses consistently |

---

## ✅ SOLID Principles Applied

| Principle | Implementation |
|-----------|-----------------|
| **S**ingle Responsibility | Each class has one reason to change (ServiceCategoryService only handles categories, ServiceOfferService only handles offers) |
| **O**pen/Closed | Services are open for extension via interfaces, closed for modification |
| **L**iskov Substitution | All service implementations can be substituted for their interfaces |
| **I**nterface Segregation | Focused interfaces (ServiceCategoryService, ServiceOfferService) instead of one large interface |
| **D**ependency Inversion | Depend on abstractions (interfaces) not concrete implementations |

---

## 📋 API Endpoints Summary

### ADMIN ENDPOINTS (Role: ROLE_ADMIN)

```
POST   /admin/categories
  Request: CreateCategoryRequest (name, description)
  Response: ApiResponse<ServiceCategoryResponse>
  Status: 201 Created

GET    /admin/categories
  Response: ApiResponse<List<ServiceCategoryResponse>>
  Status: 200 OK

GET    /admin/categories/{categoryId}
  Response: ApiResponse<ServiceCategoryResponse>
  Status: 200 OK

PUT    /admin/categories/{categoryId}
  Request: UpdateCategoryRequest (name, description)
  Response: ApiResponse<ServiceCategoryResponse>
  Status: 200 OK

DELETE /admin/categories/{categoryId}
  Response: ApiResponse<Void>
  Status: 204 No Content (soft delete)

DELETE /admin/categories/{categoryId}?hard=true
  Response: ApiResponse<Void>
  Status: 204 No Content (hard delete)
```

### PROVIDER ENDPOINTS (Role: ROLE_SERVICE_PROVIDER)

```
POST   /provider/offers
  Request: CreateOfferRequest (categoryId, title, description, price, available_from, available_to)
  Response: ApiResponse<ServiceOfferResponse>
  Status: 201 Created

GET    /provider/offers
  Response: ApiResponse<List<ServiceOfferResponse>>
  Status: 200 OK

GET    /provider/offers/{offerId}
  Response: ApiResponse<ServiceOfferResponse>
  Status: 200 OK

PUT    /provider/offers/{offerId}
  Request: UpdateOfferRequest (title, description, price, available_from, available_to, status)
  Response: ApiResponse<ServiceOfferResponse>
  Status: 200 OK

DELETE /provider/offers/{offerId}
  Response: ApiResponse<Void>
  Status: 204 No Content (soft delete - set status to INACTIVE)

DELETE /provider/offers/{offerId}?hard=true
  Response: ApiResponse<Void>
  Status: 204 No Content (hard delete)
```

### CUSTOMER ENDPOINTS (Role: ROLE_CUSTOMER or any authenticated user)

```
GET    /services
  Response: ApiResponse<List<ServiceOfferResponse>>
  Status: 200 OK
  Filters: status = ACTIVE, available_from >= NOW()

GET    /services/category/{categoryId}
  Response: ApiResponse<List<ServiceOfferResponse>>
  Status: 200 OK
  Filters: category_id = categoryId, status = ACTIVE, available_from >= NOW()

GET    /services/search?keyword=...&categoryId=...&minPrice=...&maxPrice=...
  Response: ApiResponse<List<ServiceOfferResponse>>
  Status: 200 OK
  Filters: status = ACTIVE, available_from >= NOW()

GET    /services/{offerId}
  Response: ApiResponse<ServiceOfferResponse>
  Status: 200 OK
  Validation: offer must be ACTIVE

GET    /services/{offerId}/availability
  Response: ApiResponse<AvailabilityResponse>
  Status: 200 OK
```

---

## 🔐 Security & JWT

- ✅ All endpoints require valid JWT token
- ✅ JWT token contains: userId, username, role
- ✅ JWT validated locally using same secret key as User Service
- ✅ Role-based access control via Spring Security @PreAuthorize
- ✅ Provider can only modify their own offers
- ✅ Admin can manage categories
- ✅ Customers can browse (read-only)
- ✅ Returns JSON (not HTML) for 401/403 errors

---

## 📊 Error Handling

All endpoints handle:
- ✅ **400 Bad Request** - Invalid input (negative price, invalid dates, missing fields)
- ✅ **401 Unauthorized** - Missing or invalid JWT token
- ✅ **403 Forbidden** - User doesn't have required role
- ✅ **404 Not Found** - Category/offer doesn't exist
- ✅ **409 Conflict** - Duplicate category name
- ✅ **500 Internal Server Error** - Database errors

---

## 🏆 This Structure Provides

✅ **Clean Architecture** - Clear separation of concerns  
✅ **SOLID Principles** - Maintainable and extensible code  
✅ **Design Patterns** - Proven solutions to common problems  
✅ **Testability** - Easy to unit test each layer  
✅ **Scalability** - Easy to add new features  
✅ **Security** - JWT authentication, role-based access control  
✅ **Error Handling** - Global exception handling with proper HTTP status codes  
✅ **API Documentation** - Clear endpoint structure  
✅ **Database Migrations** - Version-controlled schema changes  
✅ **Microservice Communication** - JWT validation without calling User Service  

**This is production-ready, elite-level architecture!** 🔥

---

## 📝 Key Design Decisions

| Decision | Reason |
|----------|--------|
| **Soft & Hard Delete** | Soft delete (status/flag) for audit trail; hard delete for compliance |
| **JWT Local Validation** | No calls to User Service - validates JWT locally using same secret key |
| **Role-Based Access** | Spring Security @PreAuthorize for clean, declarative authorization |
| **Provider Ownership** | Extract provider_id from JWT token, validate ownership on updates/deletes |
| **Case-Insensitive Categories** | Prevents duplicate categories due to case differences |
| **Composite Indexes** | Optimize common queries (category_id + status) |
| **Time Slot Support** | available_from/available_to for realistic scheduling |
| **No Pagination** | Per requirements, browse endpoints return all results |
| **No Sorting** | Per requirements, browse endpoints return results in default order |

---

## 🎓 Viva Talking Points (Elite Level)

### 1. Microservices Isolation
**Q: Why doesn't Service Catalog call User Service to validate JWT?**
```
A: "JWT tokens are self-contained and cryptographically signed. 
   Since both services use the same secret key, Service Catalog can validate 
   the token locally without calling User Service. This reduces latency, 
   improves resilience, and follows the microservices principle of independence."
```

### 2. Role-Based Access Control
**Q: How do you enforce role-based access?**
```
A: "We use Spring Security's @PreAuthorize annotation with role checks.
   For example, @PreAuthorize(\"hasRole('ADMIN')\") ensures only admins 
   can access category management endpoints. This is declarative, testable, 
   and follows the principle of least privilege."
```

### 3. Provider Ownership Validation
**Q: How do you ensure providers can only modify their own offers?**
```
A: "We extract provider_id from the JWT token (not from request body).
   When updating/deleting an offer, we verify that the offer's provider_id 
   matches the JWT's userId. This prevents unauthorized modifications."
```

### 4. Soft vs Hard Delete
**Q: Why support both soft and hard delete?**
```
A: "Soft delete (marking as inactive) preserves historical data and audit trails.
   Hard delete removes data completely for compliance (e.g., GDPR).
   We support both: soft delete by default, hard delete via query parameter."
```

### 5. Case-Insensitive Categories
**Q: Why use UNIQUE(LOWER(name)) for categories?**
```
A: "Prevents duplicate categories due to case differences. 
   'Plumbing' and 'plumbing' are the same category. 
   This ensures data consistency and prevents user confusion."
```

### 6. Time Slot Design
**Q: Why available_from and available_to instead of just available_date?**
```
A: "Real-world services need time slots, not just dates. 
   A plumber might be available 9 AM-5 PM on May 15.
   This design supports multiple bookings per day and realistic scheduling."
```

### 7. No Pagination/Sorting
**Q: Why no pagination or sorting in browse endpoints?**
```
A: "Per functional requirements, customers browse all services by category.
   This keeps the API simple and matches the requirement specification.
   If performance becomes an issue, pagination can be added later."
```

---

## 🔄 Service Catalog Workflow

### Admin: Add New Category
```
1. Admin calls: POST /admin/categories
2. Request: { name: "Plumbing", description: "Pipe repair services" }
3. Service validates: name is unique (case-insensitive)
4. Database: INSERT INTO service_categories (name, description)
5. Response: { id: 1, name: "Plumbing", ... }
```

### Provider: Create Service Offer
```
1. Provider calls: POST /provider/offers
2. JWT token extracted: provider_id = 5
3. Request: { categoryId: 1, title: "Pipe Repair", price: 80.00, ... }
4. Service validates: price > 0, available_to > available_from, category exists
5. Database: INSERT INTO service_offers (provider_id=5, category_id=1, ...)
6. Response: { id: 100, provider_id: 5, category_id: 1, ... }
```

### Customer: Browse Services by Category
```
1. Customer calls: GET /services/category/1
2. Database: SELECT * FROM service_offers 
             WHERE category_id = 1 AND status = 'ACTIVE' AND available_from >= NOW()
3. Response: [ { id: 100, title: "Pipe Repair", price: 80.00, ... }, ... ]
```

### Customer: Search Services
```
1. Customer calls: GET /services/search?keyword=pipe&minPrice=50&maxPrice=150
2. Database: SELECT * FROM service_offers 
             WHERE (title LIKE '%pipe%' OR description LIKE '%pipe%')
             AND price BETWEEN 50 AND 150
             AND status = 'ACTIVE' AND available_from >= NOW()
3. Response: [ { id: 100, title: "Pipe Repair", price: 80.00, ... }, ... ]
```

---

## ✅ Ready to Build!

This structure is:
- ✅ Microservices-correct (no cross-DB FKs)
- ✅ Security-first (JWT validation, role-based access)
- ✅ SOLID-compliant (single responsibility, dependency inversion)
- ✅ Production-ready (error handling, logging, migrations)
- ✅ Testable (clear layer separation)
- ✅ Scalable (easy to add new features)

**Next step: Create the Spring Boot project and implement all endpoints!** 🚀
