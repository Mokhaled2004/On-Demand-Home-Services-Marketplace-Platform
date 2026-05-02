# User Service - Detailed Project Structure

## Complete Directory Tree

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/user/
│   │   │       ├── UserServiceApplication.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java
│   │   │       │   └── WalletController.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── UserService.java
│   │   │       │   └── WalletService.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   └── Wallet.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   └── WalletRepository.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── UserDTO.java
│   │   │       │   ├── WalletDTO.java
│   │   │       │   ├── LoginRequest.java
│   │   │       │   └── LoginResponse.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── UserNotFoundException.java
│   │   │       │   ├── InsufficientBalanceException.java
│   │   │       │   ├── UserAlreadyExistsException.java
│   │   │       │   ├── InvalidCredentialsException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       │
│   │   │       └── util/
│   │   │           └── PasswordEncoder.java (optional)
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml (optional)
│   │       ├── application-prod.yml (optional)
│   │       └── static/ (empty for now)
│   │
│   └── test/
│       └── java/
│           └── com/marketplace/user/
│               ├── UserServiceApplicationTests.java
│               ├── controller/
│               │   ├── UserControllerTest.java
│               │   └── WalletControllerTest.java
│               ├── service/
│               │   ├── UserServiceTest.java
│               │   └── WalletServiceTest.java
│               └── repository/
│                   ├── UserRepositoryTest.java
│                   └── WalletRepositoryTest.java
│
├── pom.xml
├── README.md
├── .gitignore
└── target/ (generated after build)
```

---

## File Descriptions

### Root Level Files

#### `pom.xml`
- Maven configuration file
- Contains dependencies, plugins, properties
- Spring Boot version: 3.2.0
- Spring Cloud version: 2023.0.0
- Java version: 17

#### `README.md`
- Project documentation
- Setup instructions
- API endpoints documentation

#### `.gitignore`
- Exclude target/, .idea/, *.class, etc.

---

## Java Package Structure

### 1. Main Application Class
**File**: `UserServiceApplication.java`
- Entry point of the application
- Annotations: `@SpringBootApplication`, `@EnableDiscoveryClient`

---

### 2. Controller Layer (`controller/`)

#### `UserController.java`
**Base Path**: `/users`

Endpoints:
```
POST   /users/register          - Register new user
POST   /users/login             - User login
GET    /users/{userId}          - Get user by ID
GET    /users/username/{username} - Get user by username
PUT    /users/{userId}          - Update user
DELETE /users/{userId}          - Delete user
```

#### `WalletController.java`
**Base Path**: `/wallet`

Endpoints:
```
GET    /wallet/{userId}         - Get wallet balance
POST   /wallet/deduct           - Deduct balance
POST   /wallet/refund           - Refund balance
POST   /wallet/add-funds        - Add funds
POST   /wallet/validate         - Validate sufficient balance
```

---

### 3. Service Layer (`service/`)

#### `UserService.java`
Business logic for user operations:
- User registration
- User authentication
- User profile management
- User lookup

#### `WalletService.java`
Business logic for wallet operations:
- Balance retrieval
- Balance deduction
- Balance refund
- Balance validation
- Fund addition

---

### 4. Entity Layer (`entity/`)

#### `User.java`
JPA Entity representing a user in database

Fields:
```
- id: Long (Primary Key)
- username: String (Unique)
- password: String
- email: String
- role: String (CUSTOMER, PROVIDER, ADMIN)
- walletBalance: Double
- active: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### `Wallet.java`
JPA Entity representing user wallet

Fields:
```
- id: Long (Primary Key)
- userId: Long (Foreign Key)
- balance: Double
- lastUpdated: Long (timestamp)
- transactionCount: Integer
```

---

### 5. Repository Layer (`repository/`)

#### `UserRepository.java`
Spring Data JPA interface extending `JpaRepository<User, Long>`

Custom query methods:
```
- findByUsername(String username)
- findByEmail(String email)
```

#### `WalletRepository.java`
Spring Data JPA interface extending `JpaRepository<Wallet, Long>`

Custom query methods:
```
- findByUserId(Long userId)
```

---

### 6. DTO Layer (`dto/`)

#### `UserDTO.java`
Data Transfer Object for User

Fields:
```
- id: Long
- username: String
- email: String
- role: String
- walletBalance: Double
- active: Boolean
```

#### `WalletDTO.java`
Data Transfer Object for Wallet

Fields:
```
- userId: Long
- balance: Double
- lastUpdated: Long
```

#### `LoginRequest.java`
Request DTO for login endpoint

Fields:
```
- username: String
- password: String
```

#### `LoginResponse.java`
Response DTO for login endpoint

Fields:
```
- userId: Long
- username: String
- token: String (JWT - optional)
- role: String
- message: String
```

---

### 7. Exception Layer (`exception/`)

#### `UserNotFoundException.java`
Custom exception when user not found

#### `InsufficientBalanceException.java`
Custom exception when wallet balance is insufficient

#### `UserAlreadyExistsException.java`
Custom exception when trying to register duplicate user

#### `InvalidCredentialsException.java`
Custom exception for invalid login credentials

#### `GlobalExceptionHandler.java`
Global exception handler using `@RestControllerAdvice`

Handles all custom exceptions and returns appropriate HTTP responses

---

### 8. Utility Layer (`util/`)

#### `PasswordEncoder.java` (Optional)
Utility class for password encoding/hashing
- Encode password on registration
- Verify password on login

---

### 9. Resources (`resources/`)

#### `application.yml`
Main configuration file

Contains:
- Spring application name: `USER-SERVICE`
- Database configuration (PostgreSQL)
- JPA/Hibernate settings
- Server port: 8081
- Eureka client configuration
- Logging levels

#### `application-dev.yml` (Optional)
Development-specific configuration

#### `application-prod.yml` (Optional)
Production-specific configuration

---

### 10. Test Layer (`test/`)

#### `UserServiceApplicationTests.java`
Main application tests

#### `UserControllerTest.java`
Unit tests for UserController

Test cases:
- Register user
- Login user
- Get user by ID
- Update user
- Delete user

#### `WalletControllerTest.java`
Unit tests for WalletController

Test cases:
- Get wallet balance
- Deduct balance
- Refund balance
- Add funds
- Validate balance

#### `UserServiceTest.java`
Unit tests for UserService

#### `WalletServiceTest.java`
Unit tests for WalletService

#### `UserRepositoryTest.java`
Integration tests for UserRepository

#### `WalletRepositoryTest.java`
Integration tests for WalletRepository

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    wallet_balance DOUBLE PRECISION NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Wallets Table
```sql
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    last_updated BIGINT,
    transaction_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Dependencies Summary

### Core Spring Boot
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - Database ORM
- `spring-boot-starter-test` - Testing framework

### Database
- `postgresql` - PostgreSQL JDBC driver

### Service Discovery
- `spring-cloud-starter-netflix-eureka-client` - Eureka registration

### Utilities
- `lombok` - Reduce boilerplate code

### Optional
- `spring-boot-devtools` - Hot reload during development
- `spring-security` - Authentication/Authorization (future)
- `spring-boot-starter-validation` - Input validation

---

## API Response Format

### Success Response
```json
{
  "status": "SUCCESS",
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2026-04-29T21:00:00Z"
}
```

### Error Response
```json
{
  "status": "ERROR",
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2026-04-29T21:00:00Z"
}
```

---

## Key Implementation Notes

1. **Lombok Usage**: Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities and DTOs
2. **JPA Annotations**: Use `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`
3. **REST Annotations**: Use `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
4. **Service Layer**: Use `@Service` and `@Transactional` annotations
5. **Repository**: Extend `JpaRepository<Entity, ID>`
6. **Exception Handling**: Use `@RestControllerAdvice` for global exception handling
7. **Logging**: Use SLF4J with Logback (included with Spring Boot)

---

## Build and Run

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

### Build JAR
```bash
mvn clean package -DskipTests
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

---

## Configuration Checklist

- [ ] Create project from Spring Initializr
- [ ] Update pom.xml with dependencies
- [ ] Create application.yml with correct configuration
- [ ] Create UserServiceApplication.java with @EnableDiscoveryClient
- [ ] Create all entity classes
- [ ] Create all repository interfaces
- [ ] Create all service classes
- [ ] Create all DTO classes
- [ ] Create all controller classes
- [ ] Create exception handling
- [ ] Create database and tables
- [ ] Test service registration with Eureka
- [ ] Test all API endpoints

