# User Service - Current Status & Setup Instructions

## ✅ What's Already Done

1. **Project Structure**: Created ✓
   - Maven project with correct folder structure
   - pom.xml with Spring Boot 3.5.14
   - Main application class: `UserServiceApplication.java`

2. **Dependencies**: Configured ✓
   - Spring Boot 3.5.14
   - Spring Data JPA
   - PostgreSQL Driver
   - Eureka Client
   - Lombok
   - DevTools

3. **Spring Cloud Version**: 2025.0.2 ✓
   - Compatible with Spring Boot 3.5.14

## ⚠️ What Needs to Be Done

### 1. Update application.yaml Configuration

**Current file**: `user-service/src/main/resources/application.yaml`

**Replace with**:
```yaml
spring:
  application:
    name: USER-SERVICE
  
  datasource:
    # Replace with your Neon connection string
    url: jdbc:postgresql://[neon-host]:5432/[database]?sslmode=require
    username: [neon-user]
    password: [neon-password]
    driver-class-name: org.postgresql.Driver
    
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
```

### 2. Add @EnableDiscoveryClient Annotation

**File**: `user-service/src/main/java/com/marketplace/user_service/UserServiceApplication.java`

**Update to**:
```java
package com.marketplace.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
```

### 3. Create Entity Classes

Create the following files in `user-service/src/main/java/com/marketplace/user/entity/`:

**User.java**
```java
package com.marketplace.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private Double walletBalance;
    
    @Column(nullable = false)
    private Boolean active;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Wallet.java**
```java
package com.marketplace.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Double balance;
    
    @Column(nullable = false)
    private Long lastUpdated;
    
    @Column(nullable = false)
    private Integer transactionCount;
}
```

### 4. Create Repository Interfaces

Create in `user-service/src/main/java/com/marketplace/user/repository/`:

**UserRepository.java**
```java
package com.marketplace.user.repository;

import com.marketplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}
```

**WalletRepository.java**
```java
package com.marketplace.user.repository;

import com.marketplace.user.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUserId(Long userId);
}
```

### 5. Create Service Classes

Create in `user-service/src/main/java/com/marketplace/user/service/`:

**UserService.java**
```java
package com.marketplace.user.service;

import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User registerUser(String username, String password, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        user.setWalletBalance(0.0);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
```

**WalletService.java**
```java
package com.marketplace.user.service;

import com.marketplace.user.entity.Wallet;
import com.marketplace.user.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId);
    }
    
    public Double getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId);
        return wallet != null ? wallet.getBalance() : 0.0;
    }
    
    public void deductBalance(Long userId, Double amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet != null && wallet.getBalance() >= amount) {
            wallet.setBalance(wallet.getBalance() - amount);
            wallet.setLastUpdated(System.currentTimeMillis());
            walletRepository.save(wallet);
        } else {
            throw new RuntimeException("Insufficient balance");
        }
    }
    
    public void refundBalance(Long userId, Double amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet != null) {
            wallet.setBalance(wallet.getBalance() + amount);
            wallet.setLastUpdated(System.currentTimeMillis());
            walletRepository.save(wallet);
        }
    }
    
    public void addFunds(Long userId, Double amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet != null) {
            wallet.setBalance(wallet.getBalance() + amount);
            wallet.setLastUpdated(System.currentTimeMillis());
            walletRepository.save(wallet);
        }
    }
}
```

### 6. Create Controllers

Create in `user-service/src/main/java/com/marketplace/user/controller/`:

**UserController.java**
```java
package com.marketplace.user.controller;

import com.marketplace.user.entity.User;
import com.marketplace.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String role) {
        User user = userService.registerUser(username, password, email, role);
        return ResponseEntity.ok(user);
    }
}
```

**WalletController.java**
```java
package com.marketplace.user.controller;

import com.marketplace.user.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    
    @Autowired
    private WalletService walletService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<Double> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }
    
    @PostMapping("/deduct")
    public ResponseEntity<String> deductBalance(
            @RequestParam Long userId,
            @RequestParam Double amount) {
        try {
            walletService.deductBalance(userId, amount);
            return ResponseEntity.ok("Balance deducted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/refund")
    public ResponseEntity<String> refundBalance(
            @RequestParam Long userId,
            @RequestParam Double amount) {
        try {
            walletService.refundBalance(userId, amount);
            return ResponseEntity.ok("Balance refunded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/add-funds")
    public ResponseEntity<String> addFunds(
            @RequestParam Long userId,
            @RequestParam Double amount) {
        try {
            walletService.addFunds(userId, amount);
            return ResponseEntity.ok("Funds added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

## How to Run

### Step 1: Update Configuration
1. Open `user-service/src/main/resources/application.yaml`
2. Replace `[neon-host]`, `[neon-user]`, `[neon-password]`, `[database]` with your Neon credentials
3. Save the file

### Step 2: Build the Project
```bash
cd user-service
mvn clean package
```

### Step 3: Run the Service
```bash
mvn spring-boot:run
```

### Step 4: Verify Registration
Check Eureka dashboard:
```
http://localhost:8761/eureka/apps
```

You should see `USER-SERVICE` registered.

### Step 5: Test Endpoints

**Register a user:**
```bash
curl -X POST "http://localhost:8081/users/register?username=john&password=pass123&email=john@example.com&role=CUSTOMER"
```

**Get user:**
```bash
curl http://localhost:8081/users/1
```

**Get wallet balance:**
```bash
curl http://localhost:8081/wallet/1
```

## Checklist

- [ ] Update application.yaml with Neon credentials
- [ ] Add @EnableDiscoveryClient to UserServiceApplication
- [ ] Create User entity
- [ ] Create Wallet entity
- [ ] Create UserRepository
- [ ] Create WalletRepository
- [ ] Create UserService
- [ ] Create WalletService
- [ ] Create UserController
- [ ] Create WalletController
- [ ] Run `mvn clean package`
- [ ] Run `mvn spring-boot:run`
- [ ] Verify service appears in Eureka dashboard
- [ ] Test API endpoints

## Troubleshooting

**Error: "Cannot find symbol"**
- Make sure all classes are in correct packages
- Package structure: `com.marketplace.user.entity`, `com.marketplace.user.repository`, etc.

**Error: "Connection refused"**
- Verify Neon connection string is correct
- Check Eureka Server is running on port 8761

**Error: "Port 8081 already in use"**
- Change port in application.yaml
- Or kill process using port 8081

**Error: "Table already exists"**
- This is normal on first run
- JPA creates tables automatically

