# User Service - Team Task Distribution
## Mikey & Judii - Complete Today

---

## 📋 Project Status
- ✅ Project structure created
- ✅ UserServiceApplication.java created
- ✅ User.java entity created
- ✅ Server running on port 8081
- ✅ Connected to Neon PostgreSQL

---

## 👥 Team Members & Responsibilities

### **MIKEY** - Backend Logic Layer
**Packages:** `entity/`, `repository/`, `service/`
**Estimated Time:** 4-5 hours
**No dependencies** - Can start immediately

### **JUDII** - API Layer
**Packages:** `dto/`, `controller/`, `exception/`, `config/`, `security/`, `client/`, `util/`
**Estimated Time:** 4-5 hours
**Depends on:** Mikey's service signatures

---

## 🎯 MIKEY'S TASKS (Entity + Repository + Service)

### Task 1: Complete Entity Classes (30 mins)
**Location:** `src/main/java/com/marketplace/user/entity/`

Already done:
- ✅ User.java

**TODO:**
1. **Wallet.java**
   ```java
   @Entity
   @Table(name = "wallets")
   public class Wallet {
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(nullable = false)
       private Long userId;
       
       @Column(nullable = false, precision = 19, scale = 2)
       private BigDecimal balance;
       
       @Column(nullable = false, length = 3)
       private String currency; // "USD"
       
       @Version
       private Long version; // Optimistic locking
       
       @CreatedDate
       @Column(nullable = false, updatable = false)
       private LocalDateTime createdAt;
       
       @LastModifiedDate
       @Column(nullable = false)
       private LocalDateTime updatedAt;
   }
   ```

2. **WalletTransaction.java**
   ```java
   @Entity
   @Table(name = "wallet_transactions")
   public class WalletTransaction {
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(nullable = false)
       private Long walletId;
       
       @Column(nullable = false, length = 20)
       @Enumerated(EnumType.STRING)
       private TransactionType transactionType; // DEBIT, CREDIT
       
       @Column(nullable = false, precision = 19, scale = 2)
       private BigDecimal amount;
       
       @Column(nullable = false, length = 20)
       @Enumerated(EnumType.STRING)
       private TransactionStatus status; // PENDING, SUCCESS, FAILED
       
       @Column(unique = true, length = 100)
       private String idempotencyKey; // Prevent duplicate transactions
       
       @CreatedDate
       @Column(nullable = false, updatable = false)
       private LocalDateTime createdAt;
       
       public enum TransactionType { DEBIT, CREDIT }
       public enum TransactionStatus { PENDING, SUCCESS, FAILED }
   }
   ```

3. **CompensationLog.java**
   ```java
   @Entity
   @Table(name = "compensation_log")
   public class CompensationLog {
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(nullable = false)
       private Long bookingId;
       
       @Column(nullable = false)
       private Long userId;
       
       @Column(nullable = false)
       private Long transactionId;
       
       @Column(nullable = false, length = 50)
       @Enumerated(EnumType.STRING)
       private CompensationAction action; // DEDUCTED, REFUNDED
       
       @Column(nullable = false, precision = 19, scale = 2)
       private BigDecimal amount;
       
       @Column(nullable = false, length = 20)
       @Enumerated(EnumType.STRING)
       private CompensationStatus status; // PENDING, COMPLETED, FAILED
       
       @CreatedDate
       @Column(nullable = false, updatable = false)
       private LocalDateTime createdAt;
       
       public enum CompensationAction { DEDUCTED, REFUNDED }
       public enum CompensationStatus { PENDING, COMPLETED, FAILED }
   }
   ```

---

### Task 2: Create Repository Interfaces (20 mins)
**Location:** `src/main/java/com/marketplace/user/repository/`

1. **UserRepository.java**
   ```java
   @Repository
   public interface UserRepository extends JpaRepository<User, Long> {
       Optional<User> findByUsername(String username);
       Optional<User> findByEmail(String email);
       boolean existsByUsername(String username);
       boolean existsByEmail(String email);
   }
   ```

2. **WalletRepository.java**
   ```java
   @Repository
   public interface WalletRepository extends JpaRepository<Wallet, Long> {
       Optional<Wallet> findByUserId(Long userId);
       boolean existsByUserId(Long userId);
   }
   ```

3. **WalletTransactionRepository.java**
   ```java
   @Repository
   public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
       List<WalletTransaction> findByWalletId(Long walletId);
       List<WalletTransaction> findByWalletIdAndStatus(Long walletId, WalletTransaction.TransactionStatus status);
       Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);
   }
   ```

4. **CompensationLogRepository.java**
   ```java
   @Repository
   public interface CompensationLogRepository extends JpaRepository<CompensationLog, Long> {
       List<CompensationLog> findByBookingId(Long bookingId);
       List<CompensationLog> findByUserId(Long userId);
       List<CompensationLog> findByUserIdAndStatus(Long userId, CompensationLog.CompensationStatus status);
   }
   ```

---

### Task 3: Create Service Interfaces (30 mins)
**Location:** `src/main/java/com/marketplace/user/service/`

**IMPORTANT:** These are INTERFACES only. Judii will use these signatures to build controllers.

1. **UserService.java (Interface)**
   ```java
   public interface UserService {
       /**
        * Register a new user
        * @param username - unique username
        * @param email - unique email
        * @param password - plain text password (will be hashed)
        * @param role - CUSTOMER or SERVICE_PROVIDER
        * @param professionType - profession if SERVICE_PROVIDER
        * @return User entity
        * @throws UserAlreadyExistsException if username or email exists
        */
       User register(String username, String email, String password, User.UserRole role, String professionType);
       
       /**
        * Authenticate user
        * @param username - username
        * @param password - plain text password
        * @return User entity if credentials valid
        * @throws InvalidCredentialsException if credentials invalid
        */
       User login(String username, String password);
       
       /**
        * Get user by ID
        * @param userId - user ID
        * @return User entity
        * @throws UserNotFoundException if user not found
        */
       User getUserById(Long userId);
       
       /**
        * Update user profile
        * @param userId - user ID
        * @param email - new email (optional)
        * @param professionType - new profession type (optional)
        * @return Updated User entity
        * @throws UserNotFoundException if user not found
        */
       User updateUser(Long userId, String email, String professionType);
       
       /**
        * Delete user account
        * @param userId - user ID
        * @throws UserNotFoundException if user not found
        */
       void deleteUser(Long userId);
       
       /**
        * Check if user exists
        * @param userId - user ID
        * @return true if exists, false otherwise
        */
       boolean userExists(Long userId);
   }
   ```

2. **WalletService.java (Interface)**
   ```java
   public interface WalletService {
       /**
        * Get wallet balance for user
        * @param userId - user ID
        * @return Wallet entity with current balance
        * @throws UserNotFoundException if user not found
        */
       Wallet getWalletBalance(Long userId);
       
       /**
        * Deduct amount from wallet (for booking payment)
        * @param userId - user ID
        * @param amount - amount to deduct
        * @param bookingId - booking ID (for tracking)
        * @param idempotencyKey - unique key to prevent duplicate deductions
        * @return Updated Wallet entity
        * @throws UserNotFoundException if user not found
        * @throws InsufficientBalanceException if balance < amount
        */
       Wallet deductBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey);
       
       /**
        * Refund amount to wallet (for booking cancellation)
        * @param userId - user ID
        * @param amount - amount to refund
        * @param bookingId - booking ID (for tracking)
        * @param idempotencyKey - unique key to prevent duplicate refunds
        * @return Updated Wallet entity
        * @throws UserNotFoundException if user not found
        */
       Wallet refundBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey);
       
       /**
        * Add funds to wallet (customer deposit)
        * @param userId - user ID
        * @param amount - amount to add
        * @return Updated Wallet entity
        * @throws UserNotFoundException if user not found
        */
       Wallet addFunds(Long userId, BigDecimal amount);
       
       /**
        * Validate if user has sufficient balance
        * @param userId - user ID
        * @param amount - amount to check
        * @return true if balance >= amount, false otherwise
        * @throws UserNotFoundException if user not found
        */
       boolean validateBalance(Long userId, BigDecimal amount);
       
       /**
        * Create wallet for new user
        * @param userId - user ID
        * @return New Wallet entity
        */
       Wallet createWallet(Long userId);
   }
   ```

3. **CompensationService.java (Interface)**
   ```java
   public interface CompensationService {
       /**
        * Log a deduction from wallet
        * @param bookingId - booking ID
        * @param userId - user ID
        * @param transactionId - wallet transaction ID
        * @param amount - amount deducted
        * @return CompensationLog entity
        */
       CompensationLog logDeduction(Long bookingId, Long userId, Long transactionId, BigDecimal amount);
       
       /**
        * Log a refund to wallet
        * @param bookingId - booking ID
        * @param userId - user ID
        * @param transactionId - wallet transaction ID
        * @param amount - amount refunded
        * @return CompensationLog entity
        */
       CompensationLog logRefund(Long bookingId, Long userId, Long transactionId, BigDecimal amount);
       
       /**
        * Get compensation history for user
        * @param userId - user ID
        * @return List of CompensationLog entries
        */
       List<CompensationLog> getCompensationHistory(Long userId);
       
       /**
        * Get compensation history for booking
        * @param bookingId - booking ID
        * @return List of CompensationLog entries
        */
       List<CompensationLog> getBookingCompensationHistory(Long bookingId);
   }
   ```

---

### Task 4: Implement Service Classes (2-3 hours)
**Location:** `src/main/java/com/marketplace/user/service/`

Create implementation classes:
- `UserServiceImpl.java` - implements UserService
- `WalletServiceImpl.java` - implements WalletService
- `CompensationServiceImpl.java` - implements CompensationService

**Key Implementation Details:**
- Use `@Service` and `@Transactional` annotations
- Inject repositories via constructor
- Handle all exceptions properly
- Use optimistic locking for wallet updates (version field)
- Validate inputs before processing
- Log important operations

---

## 👩‍💻 JUDII'S TASKS (API Layer)

### Task 1: Create DTOs (1 hour)
**Location:** `src/main/java/com/marketplace/user/dto/`

**Request DTOs:**
```java
// request/RegisterRequest.java
@Data @Validated
public class RegisterRequest {
    @NotBlank private String username;
    @Email private String email;
    @NotBlank @Size(min=8) private String password;
    @NotNull private User.UserRole role;
    private String professionType;
}

// request/LoginRequest.java
@Data @Validated
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}

// request/AddFundsRequest.java
@Data @Validated
public class AddFundsRequest {
    @NotNull private Long userId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
}

// request/DeductBalanceRequest.java
@Data @Validated
public class DeductBalanceRequest {
    @NotNull private Long userId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    @NotNull private Long bookingId;
}
```

**Response DTOs:**
```java
// response/UserResponse.java
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private User.UserRole role;
    private String professionType;
}

// response/LoginResponse.java
@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String token;
    private User.UserRole role;
}

// response/WalletResponse.java
@Data
public class WalletResponse {
    private Long userId;
    private BigDecimal balance;
    private String currency;
}

// response/ApiResponse.java
@Data @Generic<T>
public class ApiResponse<T> {
    private String status; // "SUCCESS" or "ERROR"
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

---

### Task 2: Create Exception Classes (30 mins)
**Location:** `src/main/java/com/marketplace/user/exception/`

```java
// UserNotFoundException.java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}

// InsufficientBalanceException.java
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) { super(message); }
}

// UserAlreadyExistsException.java
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) { super(message); }
}

// InvalidCredentialsException.java
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) { super(message); }
}

// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(
            ApiResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(400).body(
            ApiResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    // Similar handlers for other exceptions...
}
```

---

### Task 3: Create Controllers (1.5 hours)
**Location:** `src/main/java/com/marketplace/user/controller/`

**UserController.java**
```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword(), 
                                         req.getRole(), req.getProfessionType());
        return ResponseEntity.status(201).body(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("User registered successfully")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtTokenProvider.generateToken(user.getId());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .token(token)
                    .role(user.getRole())
                    .build())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest req) {
        User user = userService.updateUser(userId, req.getEmail(), req.getProfessionType());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("User updated successfully")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(204).build();
    }
}
```

**WalletController.java**
```java
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final WalletMapper walletMapper;
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<WalletResponse>> deductBalance(
            @Valid @RequestBody DeductBalanceRequest req) {
        Wallet wallet = walletService.deductBalance(
            req.getUserId(), 
            req.getAmount(), 
            req.getBookingId(),
            UUID.randomUUID().toString()
        );
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Balance deducted successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<WalletResponse>> refundBalance(
            @Valid @RequestBody RefundRequest req) {
        Wallet wallet = walletService.refundBalance(
            req.getUserId(), 
            req.getAmount(), 
            req.getBookingId(),
            UUID.randomUUID().toString()
        );
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Balance refunded successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/add-funds")
    public ResponseEntity<ApiResponse<WalletResponse>> addFunds(
            @Valid @RequestBody AddFundsRequest req) {
        Wallet wallet = walletService.addFunds(req.getUserId(), req.getAmount());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Funds added successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateBalance(
            @Valid @RequestBody ValidateBalanceRequest req) {
        boolean isValid = walletService.validateBalance(req.getUserId(), req.getAmount());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(isValid)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
```

---

### Task 4: Create Configuration & Security (1 hour)
**Location:** `src/main/java/com/marketplace/user/config/` & `security/`

- **SecurityConfig.java** - Spring Security configuration
- **RestTemplateConfig.java** - RestTemplate bean
- **JwtTokenProvider.java** - JWT token generation/validation
- **PasswordEncoderUtil.java** - Password encoding
- **JwtAuthenticationFilter.java** - JWT filter

---

### Task 5: Create Mappers & Utilities (30 mins)
**Location:** `src/main/java/com/marketplace/user/dto/mapper/` & `util/`

- **UserMapper.java** - User entity ↔ DTO mapping
- **WalletMapper.java** - Wallet entity ↔ DTO mapping
- **ApiResponseBuilder.java** - Build API responses
- **Constants.java** - Application constants

---

## 📅 Timeline

| Time | Mikey | Judii |
|------|-------|-------|
| **Hour 1** | Entity classes (30 min) + Repositories (20 min) | DTOs (1 hour) |
| **Hour 2** | Service interfaces (30 min) | Exception handling (30 min) |
| **Hour 3** | Service implementation (1 hour) | Controllers part 1 (1 hour) |
| **Hour 4** | Service implementation (1 hour) | Controllers part 2 (1 hour) |
| **Hour 5** | Testing & debugging | Config + Security (1 hour) |
| **Hour 5** | - | Mappers & Utilities (30 min) |

---

## 🔗 Integration Points

**Judii needs these from Mikey:**
1. ✅ Service interface signatures (provided above)
2. ✅ Entity class names and fields (provided above)
3. ✅ Exception class names (provided above)

**Mikey needs these from Judii:**
1. ✅ DTO class names (provided above)
2. ✅ Controller endpoint paths (provided above)

---

## ✅ Checklist

### Mikey's Checklist
- [ ] Wallet.java created
- [ ] WalletTransaction.java created
- [ ] CompensationLog.java created
- [ ] UserRepository.java created
- [ ] WalletRepository.java created
- [ ] WalletTransactionRepository.java created
- [ ] CompensationLogRepository.java created
- [ ] UserService.java interface created
- [ ] WalletService.java interface created
- [ ] CompensationService.java interface created
- [ ] UserServiceImpl.java implemented
- [ ] WalletServiceImpl.java implemented
- [ ] CompensationServiceImpl.java implemented
- [ ] All services tested locally
- [ ] `mvn clean compile -DskipTests` passes

### Judii's Checklist
- [ ] All Request DTOs created
- [ ] All Response DTOs created
- [ ] ApiResponse.java created
- [ ] All exception classes created
- [ ] GlobalExceptionHandler.java created
- [ ] UserController.java created
- [ ] WalletController.java created
- [ ] UserMapper.java created
- [ ] WalletMapper.java created
- [ ] SecurityConfig.java created
- [ ] JwtTokenProvider.java created
- [ ] PasswordEncoderUtil.java created
- [ ] JwtAuthenticationFilter.java created
- [ ] RestTemplateConfig.java created
- [ ] ApiResponseBuilder.java created
- [ ] Constants.java created
- [ ] All endpoints tested with Postman
- [ ] `mvn clean compile -DskipTests` passes

---

## 🚀 Final Steps (After Both Complete)

1. Merge all code
2. Run `mvn clean install -DskipTests`
3. Run `mvn spring-boot:run`
4. Test all endpoints with Postman
5. Verify database operations
6. Document any issues

---

## 📞 Communication

- **Mikey** → Finish service layer first, then notify Judii
- **Judii** → Can start DTOs immediately while waiting for services
- **Both** → Test integration once both layers are complete

**Good luck! 🚀**
