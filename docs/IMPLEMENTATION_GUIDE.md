# 🚀 Implementation Guide - Step by Step

## Phase 1: Netflix Eureka Server (Foundation)

### Step 1️⃣: Create Eureka Server Project

**Create new Spring Boot project:**
```bash
mvn archetype:generate -DgroupId=com.marketplace -DartifactId=eureka-server -DarchetypeArtifactId=maven-archetype-quickstart
```

**Or use Spring Initializr:**
- Go to: https://start.spring.io/
- Project: Maven
- Language: Java
- Spring Boot: 3.x
- Dependencies: Eureka Server
- Group: com.marketplace
- Artifact: eureka-server

### Step 2️⃣: Add Eureka Server Dependency

**pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Add Spring Cloud dependency management:**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Step 3️⃣: Create Main Application Class

**EurekaServerApplication.java:**
```java
package com.marketplace.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### Step 4️⃣: Configure application.yml

**src/main/resources/application.yml:**
```yaml
spring:
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 3000

logging:
  level:
    root: INFO
    com.netflix.eureka: DEBUG
```

### Step 5️⃣: Run Eureka Server

```bash
mvn spring-boot:run
```

### Step 6️⃣: Verify Eureka Dashboard

**Open in browser:**
```
http://localhost:8761
```

✅ **Success Indicators:**
- Page loads without errors
- Shows "Instances currently registered with Eureka"
- Registry is empty (no services registered yet)
- No error messages

---

## Phase 2: User Service (Spring Boot)

### Step 1️⃣: Create User Service Project

**Using Spring Initializr:**
- Artifact: user-service
- Dependencies:
  - Spring Web
  - Spring Data JPA
  - PostgreSQL Driver
  - Eureka Discovery Client
  - Lombok

### Step 2️⃣: Add Dependencies

**pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Step 3️⃣: Configure application.yml

**src/main/resources/application.yml:**
```yaml
spring:
  application:
    name: USER-SERVICE
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### Step 4️⃣: Create User Entity

**User.java:**
```java
package com.marketplace.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String role; // CUSTOMER, PROVIDER, ADMIN
    
    @Column(nullable = false)
    private Double walletBalance;
    
    @Column(nullable = false)
    private Boolean active = true;
}
```

### Step 5️⃣: Create Wallet Entity

**Wallet.java:**
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
}
```

### Step 6️⃣: Create Repository

**UserRepository.java:**
```java
package com.marketplace.user.repository;

import com.marketplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
```

**WalletRepository.java:**
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

### Step 7️⃣: Create Wallet Service

**WalletService.java:**
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

### Step 8️⃣: Create Wallet Controller

**WalletController.java:**
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

### Step 9️⃣: Create Main Application Class

**UserServiceApplication.java:**
```java
package com.marketplace.user;

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

### Step 🔟: Run User Service

```bash
mvn spring-boot:run
```

### Step 1️⃣1️⃣: Verify Registration

**Check Eureka Dashboard:**
```
http://localhost:8761
```

✅ **Success Indicators:**
- USER-SERVICE appears in the registry
- Status shows "UP"
- Instance count shows 1

---

## Next Phases (Coming Soon)

- Phase 3: Service Catalog Service
- Phase 4: Booking Service (EJB)
- Phase 5: Notification Service
- Phase 6: Integration Testing

---

## Database Setup

### PostgreSQL Setup

**Create databases:**
```sql
CREATE DATABASE user_db;
CREATE DATABASE catalog_db;
CREATE DATABASE booking_db;
CREATE DATABASE notification_db;
```

**Connect as user:**
```bash
psql -U postgres -d user_db
```

---

## Troubleshooting

### Eureka not showing services?
- Check if Eureka is running on :8761
- Check if service has `@EnableDiscoveryClient`
- Check application.yml eureka.client.service-url

### Service won't start?
- Check port conflicts
- Check database connection
- Check Spring Cloud version compatibility

### Database connection error?
- Verify PostgreSQL is running
- Check username/password
- Check database exists
