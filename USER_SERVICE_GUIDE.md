# User Service Implementation Guide

## Overview
The User Service is a Spring Boot microservice responsible for:
- User registration and authentication
- Wallet management (balance tracking, deductions, refunds)
- User profile management
- Service discovery via Eureka

---

## Step 1: Create User Service Project

### Option A: Using Spring Initializr (Recommended)
1. Go to: https://start.spring.io/
2. Configure:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.5.14
   - **Group**: com.marketplace
   - **Artifact**: user-service
   - **Name**: User Service
   - **Package name**: com.marketplace.user
   - **Packaging**: JAR
   - **Java**: 17

3. **Dependencies** (search and add):
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - Eureka Discovery Client
   - Lombok
   - Spring Boot DevTools

4. Click "Generate" and extract the ZIP file

### Option B: Using Maven Command
```bash
mvn archetype:generate \
  -DgroupId=com.marketplace \
  -DartifactId=user-service \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

---

## Step 2: Update pom.xml

Add these properties and dependencies to your `pom.xml`:

### Properties Section
```xml
<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2024.0.0</spring-cloud.version>
</properties>
```

### Key Dependencies to Add
```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
    <scope>runtime</scope>
</dependency>

<!-- Eureka Discovery Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Spring Boot DevTools -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Dependency Management
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Build Plugins
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Step 3: Configure application.yml

Create `src/main/resources/application.yml` with Neon PostgreSQL configuration:

```yaml
spring:
  application:
    name: USER-SERVICE
  
  datasource:
    # Neon PostgreSQL Connection String
    # Format: postgresql://[user]:[password]@[host]/[database]
    # Get this from Neon Console or use: neonctl connection-string
    url: jdbc:postgresql://[neon-host]:5432/[database-name]?sslmode=require
    username: [neon-user]
    password: [neon-password]
    driver-class-name: org.postgresql.Driver
    
    # Connection Pool Configuration
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
          fetch_size: 50
    show-sql: false
    open-in-view: false

server:
  port: 8081
  servlet:
    context-path: /

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

logging:
  level:
    root: INFO
    com.marketplace.user: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Neon Connection String Setup

**Option 1: Get Connection String from Neon Console**
1. Go to: https://console.neon.tech
2. Select your project
3. Click "Connection string" button
4. Copy the PostgreSQL connection string
5. Replace placeholders in application.yml

**Option 2: Using Neon CLI**
```bash
# Install Neon CLI
npm install -g neonctl

# Login to Neon
neonctl auth

# Get connection string
neonctl connection-string [project-id]
```

**Connection String Format:**
```
postgresql://[user]:[password]@[host]/[database]?sslmode=require
```

Example:
```
postgresql://neondb_owner:AbCdEfGhIjKlMnOp@ep-cool-cloud-123456.us-east-1.aws.neon.tech/neondb?sslmode=require
```

### Environment Variables (Recommended)

Create `.env` file in project root:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://[neon-host]:5432/[database]?sslmode=require
SPRING_DATASOURCE_USERNAME=[neon-user]
SPRING_DATASOURCE_PASSWORD=[neon-password]
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

Then reference in `application.yml`:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

### Important Notes for Neon

1. **SSL Mode**: Always use `sslmode=require` for Neon connections
2. **Connection Pooling**: Neon supports connection pooling via HikariCP (configured above)
3. **Compute Autoscaling**: Neon automatically scales compute based on load
4. **Scale-to-Zero**: Development branches suspend after inactivity to save costs
5. **Database Branching**: Create dev/staging branches for testing schema changes
6. **Connection Limits**: Check your Neon plan for connection limits

---

## Step 4: Create Main Application Class

File: `src/main/java/com/marketplace/user/UserServiceApplication.java`

Key annotations needed:
- `@SpringBootApplication`
- `@EnableDiscoveryClient`

---

## Step 5: Create Entity Classes

### User Entity
File: `src/main/java/com/marketplace/user/entity/User.java`

Fields needed:
- `id` (Long, @Id, @GeneratedValue)
- `username` (String, unique, not null)
- `password` (String, not null)
- `email` (String, not null)
- `role` (String - CUSTOMER, PROVIDER, ADMIN)
- `walletBalance` (Double, not null)
- `active` (Boolean, default true)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### Wallet Entity
File: `src/main/java/com/marketplace/user/entity/Wallet.java`

Fields needed:
- `id` (Long, @Id, @GeneratedValue)
- `userId` (Long, not null)
- `balance` (Double, not null)
- `lastUpdated` (Long - timestamp)
- `transactionCount` (Integer)

---

## Step 6: Create Repository Interfaces

### UserRepository
File: `src/main/java/com/marketplace/user/repository/UserRepository.java`

Methods needed:
- `findByUsername(String username)`
- `findByEmail(String email)`
- `findById(Long id)`
- `save(User user)`

### WalletRepository
File: `src/main/java/com/marketplace/user/repository/WalletRepository.java`

Methods needed:
- `findByUserId(Long userId)`
- `save(Wallet wallet)`

---

## Step 7: Create Service Classes

### UserService
File: `src/main/java/com/marketplace/user/service/UserService.java`

Methods needed:
- `registerUser(UserDTO userDTO)` - Create new user
- `getUserById(Long userId)` - Fetch user details
- `getUserByUsername(String username)` - Fetch by username
- `updateUser(Long userId, UserDTO userDTO)` - Update user
- `deleteUser(Long userId)` - Soft delete user
- `authenticateUser(String username, String password)` - Login

### WalletService
File: `src/main/java/com/marketplace/user/service/WalletService.java`

Methods needed:
- `getWallet(Long userId)` - Get wallet details
- `getBalance(Long userId)` - Get current balance
- `deductBalance(Long userId, Double amount)` - Deduct money (for bookings)
- `refundBalance(Long userId, Double amount)` - Refund money (on cancellation)
- `addFunds(Long userId, Double amount)` - Add funds to wallet
- `validateBalance(Long userId, Double amount)` - Check if sufficient balance

---

## Step 8: Create DTOs (Data Transfer Objects)

### UserDTO
File: `src/main/java/com/marketplace/user/dto/UserDTO.java`

Fields:
- `id`
- `username`
- `email`
- `role`
- `walletBalance`
- `active`

### WalletDTO
File: `src/main/java/com/marketplace/user/dto/WalletDTO.java`

Fields:
- `userId`
- `balance`
- `lastUpdated`

### LoginRequest
File: `src/main/java/com/marketplace/user/dto/LoginRequest.java`

Fields:
- `username`
- `password`

### LoginResponse
File: `src/main/java/com/marketplace/user/dto/LoginResponse.java`

Fields:
- `userId`
- `username`
- `token` (JWT token - optional for now)
- `role`

---

## Step 9: Create REST Controllers

### UserController
File: `src/main/java/com/marketplace/user/controller/UserController.java`

Endpoints needed:
- `POST /users/register` - Register new user
- `POST /users/login` - User login
- `GET /users/{userId}` - Get user details
- `PUT /users/{userId}` - Update user
- `DELETE /users/{userId}` - Delete user
- `GET /users/username/{username}` - Get user by username

### WalletController
File: `src/main/java/com/marketplace/user/controller/WalletController.java`

Endpoints needed:
- `GET /wallet/{userId}` - Get wallet balance
- `POST /wallet/deduct` - Deduct balance (params: userId, amount)
- `POST /wallet/refund` - Refund balance (params: userId, amount)
- `POST /wallet/add-funds` - Add funds (params: userId, amount)
- `POST /wallet/validate` - Validate balance (params: userId, amount)

---

## Step 10: Create Exception Handling

### Custom Exceptions
File: `src/main/java/com/marketplace/user/exception/`

Create:
- `UserNotFoundException.java`
- `InsufficientBalanceException.java`
- `UserAlreadyExistsException.java`
- `InvalidCredentialsException.java`

### Global Exception Handler
File: `src/main/java/com/marketplace/user/exception/GlobalExceptionHandler.java`

Use `@RestControllerAdvice` and `@ExceptionHandler` annotations

---

## Step 11: Database Setup with Neon

### Create Neon Project

1. **Go to Neon Console**: https://console.neon.tech
2. **Create New Project**:
   - Click "New Project"
   - Name: `marketplace-user-service`
   - Region: Choose closest to your location
   - PostgreSQL Version: 15 or 16 (latest recommended)
   - Click "Create Project"

3. **Get Connection Details**:
   - Project created with default database `neondb`
   - Default role: `neondb_owner`
   - Copy connection string from console

### Create Database Schema

**Option 1: Using Neon Console SQL Editor**
1. Go to your project in Neon Console
2. Click "SQL Editor"
3. Run the following SQL:

```sql
-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    wallet_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create wallets table
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated BIGINT,
    transaction_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
```

**Option 2: Using Spring Boot JPA (Automatic)**
- Set `spring.jpa.hibernate.ddl-auto: update` in application.yml
- Tables will be created automatically on first run
- Recommended for development only

### Create Development Branch (Optional)

For safe testing before production:

```bash
# Using Neon CLI
neonctl branches create --project-id [project-id] --name dev

# Get dev branch connection string
neonctl connection-string [project-id] --branch dev
```

### Verify Connection

Test connection from your application:
```bash
mvn spring-boot:run
```

Check logs for:
```
Hibernate: create table users (...)
Hibernate: create table wallets (...)
```

### Neon Features for Development

1. **Database Branching**: Create isolated copies for testing
2. **Autoscaling**: Automatic compute scaling based on load
3. **Scale-to-Zero**: Development branches suspend after inactivity
4. **Point-in-Time Recovery**: Restore database to any point in time
5. **Connection Pooling**: Built-in connection pooling support

### Troubleshooting Neon Connection

**Error: "SSL connection error"**
- Ensure `sslmode=require` in connection string
- Check firewall allows outbound HTTPS

**Error: "Too many connections"**
- Increase HikariCP pool size in application.yml
- Or use Neon's connection pooling feature

**Error: "Authentication failed"**
- Verify username and password from Neon Console
- Check connection string format

**Error: "Database does not exist"**
- Verify database name in connection string
- Create database in Neon Console if missing

---

## Step 12: Run the Service

```bash
cd user-service
mvn clean spring-boot:run
```

### Expected Output
- Service starts on port 8081
- Connects to PostgreSQL
- Registers with Eureka at http://localhost:8761/eureka/
- Logs show: "Started UserServiceApplication"

### Verify Registration
Check Eureka dashboard:
```
http://localhost:8761/eureka/apps
```

You should see `USER-SERVICE` in the registry.

---

## Step 13: Test the Service

### Test User Registration
```bash
curl -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }'
```

### Test Get User
```bash
curl http://localhost:8081/users/1
```

### Test Wallet Balance
```bash
curl http://localhost:8081/wallet/1
```

---

## Troubleshooting

### Service won't start
- Check PostgreSQL is running
- Verify database `user_db` exists
- Check port 8081 is not in use

### Can't connect to Eureka
- Verify Eureka Server is running on 8761
- Check `eureka.client.service-url.defaultZone` in application.yml

### Database connection error
- Verify PostgreSQL credentials in application.yml
- Check database URL is correct

---

## Next Steps
1. Create Booking Service (EJB)
2. Create Notification Service
3. Set up RabbitMQ messaging
4. Create architecture diagram on Miro
5. Implement circuit breakers and bulkheads

