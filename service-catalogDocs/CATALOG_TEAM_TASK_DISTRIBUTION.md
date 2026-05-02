# Service Catalog - Team Task Distribution
## MIKEY (API Layer First) & JUDII (Backend Layer)

---

## 📋 Project Status
- ✅ Project structure created (service-catalog-service)
- ✅ ServiceCatalogApplication.java created
- ✅ Connected to Neon PostgreSQL (catalog_service_db)
- ✅ Server running on port 8083
- ✅ Registered with Eureka

---

## 👥 Team Members & Responsibilities

### **MIKEY** - API Layer (Controllers, DTOs, Exceptions, Security)
**Packages:** `dto/`, `controller/`, `exception/`, `config/`, `security/`, `util/`
**Estimated Time:** 3-4 hours
**Can start immediately** - No dependencies

### **JUDII** - Backend Layer (Entities, Repositories, Services)
**Packages:** `entity/`, `repository/`, `service/`
**Estimated Time:** 3-4 hours
**Depends on:** MIKEY's DTO signatures

---

## 👨‍💻 MIKEY'S TASKS (API Layer - Start First!)

### Task 1: Create Request DTOs (45 mins)
**Location:** `src/main/java/com/marketplace/user/dto/request/`

**TODO:**
1. **CreateCategoryRequest.java**
   - Fields: `name` (String, @NotBlank, @Size(2-255)), `description` (String, @Size(max=1000))
   - Used by: POST /admin/categories

2. **UpdateCategoryRequest.java**
   - Fields: `name` (String, @NotBlank, @Size(2-255)), `description` (String, @Size(max=1000))
   - Used by: PUT /admin/categories/{categoryId}

3. **CreateOfferRequest.java**
   - Fields: `categoryId` (Long, @NotNull), `title` (String, @NotBlank, @Size(3-255)), `description` (String, @Size(max=2000)), `price` (BigDecimal, @NotNull, @Positive), `availableFrom` (LocalDateTime, @NotNull), `availableTo` (LocalDateTime, @NotNull)
   - Used by: POST /provider/offers
   - Note: Provider ID extracted from JWT token (not in request)

4. **UpdateOfferRequest.java**
   - Fields: `title` (String, @NotBlank, @Size(3-255)), `description` (String, @Size(max=2000)), `price` (BigDecimal, @NotNull, @Positive), `availableFrom` (LocalDateTime, @NotNull), `availableTo` (LocalDateTime, @NotNull), `status` (String, @Pattern("ACTIVE|INACTIVE"))
   - Used by: PUT /provider/offers/{offerId}

---

### Task 2: Create Response DTOs (45 mins)
**Location:** `src/main/java/com/marketplace/user/dto/response/`

**TODO:**
1. **ServiceCategoryResponse.java**
   - Fields: `id` (Long), `name` (String), `description` (String), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime)
   - Returned by: GET /admin/categories, POST /admin/categories, etc.

2. **ServiceOfferResponse.java**
   - Fields: `id` (Long), `providerId` (Long), `categoryId` (Long), `title` (String), `description` (String), `price` (BigDecimal), `availableFrom` (LocalDateTime), `availableTo` (LocalDateTime), `status` (String), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime)
   - Returned by: GET /services, POST /provider/offers, etc.

3. **AvailabilityResponse.java**
   - Fields: `availableFrom` (LocalDateTime), `availableTo` (LocalDateTime), `status` (String), `isAvailable` (Boolean)
   - Returned by: GET /services/{offerId}/availability

4. **ApiResponse.java** (Generic wrapper)
   - Fields: `success` (Boolean), `message` (String), `data` (T generic), `timestamp` (LocalDateTime)
   - Helper methods: `success(message, data)`, `error(message)`
   - Used by: All endpoints

---

### Task 3: Create Exception Classes (30 mins)
**Location:** `src/main/java/com/marketplace/user/exception/`

**TODO:**
1. **CategoryNotFoundException.java**
   - Extends RuntimeException
   - Constructors: `(String message)`, `(Long categoryId)`
   - HTTP Status: 404

2. **OfferNotFoundException.java**
   - Extends RuntimeException
   - Constructors: `(String message)`, `(Long offerId)`
   - HTTP Status: 404

3. **DuplicateCategoryException.java**
   - Extends RuntimeException
   - Constructor: `(String categoryName)`
   - HTTP Status: 409

4. **UnauthorizedOfferAccessException.java**
   - Extends RuntimeException
   - Constructors: `(String message)`, `(Long providerId, Long offerId)`
   - HTTP Status: 403

5. **InvalidOfferDataException.java**
   - Extends RuntimeException
   - Constructor: `(String message)`
   - HTTP Status: 400

6. **GlobalExceptionHandler.java**
   - @RestControllerAdvice
   - Handle: CategoryNotFoundException (404), OfferNotFoundException (404), DuplicateCategoryException (409), UnauthorizedOfferAccessException (403), InvalidOfferDataException (400), MethodArgumentNotValidException (400), AuthenticationException (401), AccessDeniedException (403), NoHandlerFoundException (404), Exception (500)
   - All return ApiResponse<Void> with appropriate HTTP status

---

### Task 4: Create Controllers (1.5 hours)
**Location:** `src/main/java/com/marketplace/user/controller/`

#### 4.1 AdminCategoryController.java
**Annotations:** @RestController, @RequestMapping("/admin/categories"), @RequiredArgsConstructor, @Slf4j, @PreAuthorize("hasRole('ADMIN')")

**Endpoints:**
1. **POST /admin/categories**
   - @PostMapping
   - Param: @Valid @RequestBody CreateCategoryRequest
   - Returns: 201 Created with ApiResponse<ServiceCategoryResponse>
   - Calls: serviceCategoryService.createCategory(name, description)
   - Throws: DuplicateCategoryException

2. **GET /admin/categories**
   - @GetMapping
   - Returns: 200 OK with ApiResponse<List<ServiceCategoryResponse>>
   - Calls: serviceCategoryService.getAllCategories()

3. **GET /admin/categories/{categoryId}**
   - @GetMapping("/{categoryId}")
   - Param: @PathVariable Long categoryId
   - Returns: 200 OK with ApiResponse<ServiceCategoryResponse>
   - Calls: serviceCategoryService.getCategoryById(categoryId)
   - Throws: CategoryNotFoundException

4. **PUT /admin/categories/{categoryId}**
   - @PutMapping("/{categoryId}")
   - Params: @PathVariable Long categoryId, @Valid @RequestBody UpdateCategoryRequest
   - Returns: 200 OK with ApiResponse<ServiceCategoryResponse>
   - Calls: serviceCategoryService.updateCategory(categoryId, name, description)
   - Throws: CategoryNotFoundException, DuplicateCategoryException

5. **DELETE /admin/categories/{categoryId}**
   - @DeleteMapping("/{categoryId}")
   - Params: @PathVariable Long categoryId, @RequestParam(required=false, defaultValue="false") boolean hard
   - Returns: 204 No Content
   - Calls: serviceCategoryService.softDeleteCategory(categoryId) or hardDeleteCategory(categoryId)
   - Throws: CategoryNotFoundException

---

#### 4.2 ProviderOfferController.java
**Annotations:** @RestController, @RequestMapping("/provider/offers"), @RequiredArgsConstructor, @Slf4j, @PreAuthorize("hasRole('SERVICE_PROVIDER')")

**Helper Method:**
- `getAuthenticatedProviderId(Authentication auth)` - Extract provider ID from JWT token

**Endpoints:**
1. **POST /provider/offers**
   - @PostMapping
   - Params: @Valid @RequestBody CreateOfferRequest, Authentication auth
   - Returns: 201 Created with ApiResponse<ServiceOfferResponse>
   - Calls: serviceOfferService.createOffer(providerId, categoryId, title, description, price, availableFrom, availableTo)
   - Throws: InvalidOfferDataException, CategoryNotFoundException

2. **GET /provider/offers**
   - @GetMapping
   - Param: Authentication auth
   - Returns: 200 OK with ApiResponse<List<ServiceOfferResponse>>
   - Calls: serviceOfferService.getOffersByProviderId(providerId)

3. **GET /provider/offers/{offerId}**
   - @GetMapping("/{offerId}")
   - Params: @PathVariable Long offerId, Authentication auth
   - Returns: 200 OK with ApiResponse<ServiceOfferResponse>
   - Calls: serviceOfferService.getOfferById(offerId)
   - Validation: Verify offer.providerId == authenticated providerId
   - Throws: OfferNotFoundException, UnauthorizedOfferAccessException

4. **PUT /provider/offers/{offerId}**
   - @PutMapping("/{offerId}")
   - Params: @PathVariable Long offerId, @Valid @RequestBody UpdateOfferRequest, Authentication auth
   - Returns: 200 OK with ApiResponse<ServiceOfferResponse>
   - Calls: serviceOfferService.updateOffer(offerId, title, description, price, availableFrom, availableTo, status)
   - Validation: Verify offer.providerId == authenticated providerId
   - Throws: OfferNotFoundException, UnauthorizedOfferAccessException, InvalidOfferDataException

5. **DELETE /provider/offers/{offerId}**
   - @DeleteMapping("/{offerId}")
   - Params: @PathVariable Long offerId, @RequestParam(required=false, defaultValue="false") boolean hard, Authentication auth
   - Returns: 204 No Content
   - Calls: serviceOfferService.softDeleteOffer(offerId) or hardDeleteOffer(offerId)
   - Validation: Verify offer.providerId == authenticated providerId
   - Throws: OfferNotFoundException, UnauthorizedOfferAccessException

---

#### 4.3 CustomerServiceController.java
**Annotations:** @RestController, @RequestMapping("/services"), @RequiredArgsConstructor, @Slf4j, @PreAuthorize("isAuthenticated()")

**Endpoints:**
1. **GET /services**
   - @GetMapping
   - Returns: 200 OK with ApiResponse<List<ServiceOfferResponse>>
   - Calls: serviceOfferService.getAllActiveOffers()
   - Filters: status = ACTIVE, availableFrom >= NOW()

2. **GET /services/category/{categoryId}**
   - @GetMapping("/category/{categoryId}")
   - Param: @PathVariable Long categoryId
   - Returns: 200 OK with ApiResponse<List<ServiceOfferResponse>>
   - Calls: serviceOfferService.getOffersByCategory(categoryId)
   - Filters: category_id = categoryId, status = ACTIVE, availableFrom >= NOW()
   - Throws: CategoryNotFoundException

3. **GET /services/search**
   - @GetMapping("/search")
   - Params: @RequestParam(required=false) String keyword, @RequestParam(required=false) Long categoryId, @RequestParam(required=false) BigDecimal minPrice, @RequestParam(required=false) BigDecimal maxPrice
   - Returns: 200 OK with ApiResponse<List<ServiceOfferResponse>>
   - Calls: serviceOfferService.searchOffers(keyword, categoryId, minPrice, maxPrice)
   - Filters: status = ACTIVE, availableFrom >= NOW()

4. **GET /services/{offerId}**
   - @GetMapping("/{offerId}")
   - Param: @PathVariable Long offerId
   - Returns: 200 OK with ApiResponse<ServiceOfferResponse>
   - Calls: serviceOfferService.getOfferById(offerId)
   - Validation: Offer must be ACTIVE
   - Throws: OfferNotFoundException

5. **GET /services/{offerId}/availability**
   - @GetMapping("/{offerId}/availability")
   - Param: @PathVariable Long offerId
   - Returns: 200 OK with ApiResponse<AvailabilityResponse>
   - Calls: serviceOfferService.getAvailability(offerId)
   - Throws: OfferNotFoundException

---

### Task 5: Create Configuration & Security (1 hour)
**Location:** `src/main/java/com/marketplace/user/config/` & `security/`

**TODO:**
1. **SecurityConfig.java**
   - @Configuration
   - Bean: SecurityFilterChain (configure JWT filter, role-based access, CORS)
   - Bean: PasswordEncoder (BCryptPasswordEncoder)
   - Configure: Return JSON (not HTML) for 401/403 errors

2. **JwtTokenProvider.java**
   - @Component
   - Methods: validateToken(token), extractUserId(token), extractUsername(token), extractRole(token)
   - Uses same secret key as User Service
   - No token generation (only validation)

3. **JwtAuthenticationFilter.java**
   - Extends OncePerRequestFilter
   - Validates JWT on every request
   - Sets Spring Security context with userId, username, role

4. **RestTemplateConfig.java** (if needed for future inter-service calls)
   - @Configuration
   - Bean: RestTemplate

---

### Task 6: Create Mappers & Utilities (30 mins)
**Location:** `src/main/java/com/marketplace/user/dto/mapper/` & `util/`

**TODO:**
1. **ServiceCategoryMapper.java**
   - @Component
   - Methods: toDTO(ServiceCategory), toEntity(CreateCategoryRequest)

2. **ServiceOfferMapper.java**
   - @Component
   - Methods: toDTO(ServiceOffer), toEntity(CreateOfferRequest)

3. **ApiResponseBuilder.java**
   - @Component
   - Methods: buildSuccess(message, data), buildError(message)

4. **Constants.java**
   - final class
   - Constants: Error messages, status codes, default values

---

## 👩‍💻 JUDII'S TASKS (Backend Layer - Start After MIKEY's DTOs)

### Task 1: Create Entity Classes (45 mins)
**Location:** `src/main/java/com/marketplace/user/entity/`

**TODO:**
1. **ServiceCategory.java**
   - @Entity, @Table(name="service_categories")
   - Fields: id (Long, @Id, @GeneratedValue), name (String, @Column(unique=true)), description (String), createdAt (LocalDateTime, @CreatedDate), updatedAt (LocalDateTime, @LastModifiedDate)
   - Annotations: @Data, @NoArgsConstructor, @AllArgsConstructor

2. **ServiceOffer.java**
   - @Entity, @Table(name="service_offers")
   - Fields: id (Long, @Id, @GeneratedValue), providerId (Long, NOT FK), categoryId (Long, @ManyToOne FK), title (String), description (String), price (BigDecimal, @Column(precision=15, scale=2)), availableFrom (LocalDateTime), availableTo (LocalDateTime), status (Enum: ACTIVE/INACTIVE), createdAt (LocalDateTime, @CreatedDate), updatedAt (LocalDateTime, @LastModifiedDate)
   - Annotations: @Data, @NoArgsConstructor, @AllArgsConstructor
   - Constraints: @Check("price > 0"), @Check("available_to > available_from")

---

### Task 2: Create Repository Interfaces (20 mins)
**Location:** `src/main/java/com/marketplace/user/repository/`

**TODO:**
1. **ServiceCategoryRepository.java**
   - Extends JpaRepository<ServiceCategory, Long>
   - Methods: findByNameIgnoreCase(String name), existsByNameIgnoreCase(String name)

2. **ServiceOfferRepository.java**
   - Extends JpaRepository<ServiceOffer, Long>
   - Methods: findByProviderId(Long providerId), findByCategoryId(Long categoryId), findByStatus(OfferStatus status), findByCategoryIdAndStatus(Long categoryId, OfferStatus status)

---

### Task 3: Create Service Interfaces (30 mins)
**Location:** `src/main/java/com/marketplace/user/service/`

#### 3.1 ServiceCategoryService.java (Interface)
**Methods:**
1. `createCategory(String name, String description)` → ServiceCategory
   - Throws: DuplicateCategoryException

2. `getCategoryById(Long categoryId)` → ServiceCategory
   - Throws: CategoryNotFoundException

3. `getAllCategories()` → List<ServiceCategory>

4. `updateCategory(Long categoryId, String name, String description)` → ServiceCategory
   - Throws: CategoryNotFoundException, DuplicateCategoryException

5. `softDeleteCategory(Long categoryId)` → void
   - Throws: CategoryNotFoundException

6. `hardDeleteCategory(Long categoryId)` → void
   - Throws: CategoryNotFoundException

---

#### 3.2 ServiceOfferService.java (Interface)
**Methods:**
1. `createOffer(Long providerId, Long categoryId, String title, String description, BigDecimal price, LocalDateTime availableFrom, LocalDateTime availableTo)` → ServiceOffer
   - Throws: InvalidOfferDataException, CategoryNotFoundException

2. `getOfferById(Long offerId)` → ServiceOffer
   - Throws: OfferNotFoundException

3. `getOffersByProviderId(Long providerId)` → List<ServiceOffer>

4. `getOffersByCategory(Long categoryId)` → List<ServiceOffer>
   - Filters: status = ACTIVE, availableFrom >= NOW()

5. `getAllActiveOffers()` → List<ServiceOffer>
   - Filters: status = ACTIVE, availableFrom >= NOW()

6. `searchOffers(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice)` → List<ServiceOffer>
   - Filters: status = ACTIVE, availableFrom >= NOW()

7. `updateOffer(Long offerId, String title, String description, BigDecimal price, LocalDateTime availableFrom, LocalDateTime availableTo, String status)` → ServiceOffer
   - Throws: OfferNotFoundException, InvalidOfferDataException

8. `softDeleteOffer(Long offerId)` → void
   - Throws: OfferNotFoundException

9. `hardDeleteOffer(Long offerId)` → void
   - Throws: OfferNotFoundException

10. `getAvailability(Long offerId)` → AvailabilityResponse
    - Throws: OfferNotFoundException

---

### Task 4: Implement Service Classes (2-3 hours)
**Location:** `src/main/java/com/marketplace/user/service/`

**TODO:**
1. **ServiceCategoryServiceImpl.java**
   - @Service, @Transactional
   - Inject: ServiceCategoryRepository
   - Implement all methods from ServiceCategoryService interface
   - Validation: Check for duplicate names (case-insensitive)
   - Logging: Log all operations

2. **ServiceOfferServiceImpl.java**
   - @Service, @Transactional
   - Inject: ServiceOfferRepository, ServiceCategoryRepository
   - Implement all methods from ServiceOfferService interface
   - Validation: price > 0, availableTo > availableFrom, category exists
   - Logging: Log all operations
   - Filtering: Only return ACTIVE offers with availableFrom >= NOW() where applicable

---

## 📅 Timeline

| Time | MIKEY | JUDII |
|------|-------|-------|
| **Hour 1** | Request DTOs (45 min) | Waiting for DTOs |
| **Hour 1.5** | Response DTOs (45 min) | Waiting for DTOs |
| **Hour 2** | Exception classes (30 min) | Entity classes (45 min) |
| **Hour 2.5** | Controllers part 1 (1 hour) | Repositories (20 min) |
| **Hour 3.5** | Controllers part 2 (1 hour) | Service interfaces (30 min) |
| **Hour 4** | Config + Security (1 hour) | Service implementation (1 hour) |
| **Hour 4.5** | Mappers + Utilities (30 min) | Service implementation (1 hour) |
| **Hour 5** | Testing & debugging | Service implementation (1 hour) |

---

## 🔗 Integration Points

**JUDII needs these from MIKEY:**
1. ✅ DTO class names and fields (provided above)
2. ✅ Exception class names (provided above)
3. ✅ Service method signatures (provided above)

**MIKEY needs these from JUDII:**
1. ✅ Entity class names and fields (provided above)
2. ✅ Repository method signatures (provided above)
3. ✅ Service interface signatures (provided above)

---

## ✅ Checklist

### MIKEY's Checklist
- [ ] CreateCategoryRequest.java created
- [ ] UpdateCategoryRequest.java created
- [ ] CreateOfferRequest.java created
- [ ] UpdateOfferRequest.java created
- [ ] ServiceCategoryResponse.java created
- [ ] ServiceOfferResponse.java created
- [ ] AvailabilityResponse.java created
- [ ] ApiResponse.java created
- [ ] CategoryNotFoundException.java created
- [ ] OfferNotFoundException.java created
- [ ] DuplicateCategoryException.java created
- [ ] UnauthorizedOfferAccessException.java created
- [ ] InvalidOfferDataException.java created
- [ ] GlobalExceptionHandler.java created
- [ ] AdminCategoryController.java created
- [ ] ProviderOfferController.java created
- [ ] CustomerServiceController.java created
- [ ] SecurityConfig.java created
- [ ] JwtTokenProvider.java created
- [ ] JwtAuthenticationFilter.java created
- [ ] ServiceCategoryMapper.java created
- [ ] ServiceOfferMapper.java created
- [ ] ApiResponseBuilder.java created
- [ ] Constants.java created
- [ ] All endpoints tested with Postman
- [ ] `mvn clean compile -DskipTests` passes

### JUDII's Checklist
- [ ] ServiceCategory.java created
- [ ] ServiceOffer.java created
- [ ] ServiceCategoryRepository.java created
- [ ] ServiceOfferRepository.java created
- [ ] ServiceCategoryService.java interface created
- [ ] ServiceOfferService.java interface created
- [ ] ServiceCategoryServiceImpl.java implemented
- [ ] ServiceOfferServiceImpl.java implemented
- [ ] All services tested locally
- [ ] `mvn clean compile -DskipTests` passes

---

## 🚀 Final Steps (After Both Complete)

1. Merge all code
2. Run `mvn clean install -DskipTests`
3. Run `mvn spring-boot:run`
4. Test all 15 endpoints with Postman
5. Verify database operations
6. Document any issues

---

## 📞 Communication

- **MIKEY** → Finish DTOs first (1.5 hours), then notify JUDII
- **JUDII** → Can start entities while MIKEY finishes controllers
- **Both** → Test integration once both layers are complete

**Good luck! 🚀**
