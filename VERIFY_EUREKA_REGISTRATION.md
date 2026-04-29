# Verify User Service Registration with Eureka

## Step 1: Update application.yaml with Correct Database

Update `user-service/src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: USER-SERVICE
  
  datasource:
    # Use user_service_db (created in Neon)
    url: jdbc:postgresql://[neon-host]:5432/user_service_db?sslmode=require
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

## Step 2: Add @EnableDiscoveryClient to Main Class

Update `user-service/src/main/java/com/marketplace/user_service/UserServiceApplication.java`:

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

## Step 3: Verify Eureka Server is Running

**Check if Eureka is still running:**

```bash
curl http://localhost:8761/eureka/apps
```

Expected response: XML with empty applications or existing services

If not running, start it:
```bash
cd eureka-server
mvn spring-boot:run
```

## Step 4: Run User Service

From `user-service` directory:

```bash
mvn clean spring-boot:run
```

### Expected Logs (Look for these):

```
[INFO] Scanning for projects...
[INFO] Building  0.0.1-SNAPSHOT
...
Hibernate: create table users (...)
Hibernate: create table wallets (...)
...
Registering application USER-SERVICE with eureka with initial status UP
DiscoveryClient_USER-SERVICE/USER-SERVICE:8081: registering service...
Started UserServiceApplication in X.XXX seconds
```

**Key indicators:**
- ✅ "Registering application USER-SERVICE with eureka"
- ✅ "Started UserServiceApplication"
- ✅ No errors about connection

## Step 5: Verify Registration in Eureka Dashboard

### Method 1: Check Eureka REST API

```bash
curl http://localhost:8761/eureka/apps
```

**Expected response (XML):**
```xml
<applications>
  <versions__delta>2</versions__delta>
  <apps__hashcode>UP_1_</apps__hashcode>
  <application>
    <name>USER-SERVICE</name>
    <instance>
      <instanceId>USER-SERVICE:8081</instanceId>
      <hostName>localhost</hostName>
      <app>USER-SERVICE</app>
      <ipAddr>127.0.0.1</ipAddr>
      <status>UP</status>
      <port enabled="true">8081</port>
      <securePort enabled="false">443</securePort>
      <homePageUrl>http://localhost:8081/</homePageUrl>
      <statusPageUrl>http://localhost:8081/actuator/info</statusPageUrl>
      <healthCheckUrl>http://localhost:8081/actuator/health</healthCheckUrl>
    </instance>
  </application>
</applications>
```

### Method 2: Check Specific Service

```bash
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

Should return the USER-SERVICE instance details.

## Step 6: Test User Service Endpoints

### Test 1: Health Check

```bash
curl http://localhost:8081/actuator/health
```

**Expected response:**
```json
{
  "status": "UP"
}
```

### Test 2: Register a User

```bash
curl -X POST "http://localhost:8081/users/register?username=john&password=pass123&email=john@example.com&role=CUSTOMER"
```

**Expected response:**
```json
{
  "id": 1,
  "username": "john",
  "password": "pass123",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "walletBalance": 0.0,
  "active": true,
  "createdAt": "2026-04-29T21:56:00",
  "updatedAt": "2026-04-29T21:56:00"
}
```

### Test 3: Get User

```bash
curl http://localhost:8081/users/1
```

**Expected response:**
```json
{
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "walletBalance": 0.0,
  "active": true,
  "createdAt": "2026-04-29T21:56:00",
  "updatedAt": "2026-04-29T21:56:00"
}
```

### Test 4: Get Wallet Balance

```bash
curl http://localhost:8081/wallet/1
```

**Expected response:**
```
0.0
```

### Test 5: Add Funds to Wallet

```bash
curl -X POST "http://localhost:8081/wallet/add-funds?userId=1&amount=100"
```

**Expected response:**
```
Funds added successfully
```

### Test 6: Check Updated Balance

```bash
curl http://localhost:8081/wallet/1
```

**Expected response:**
```
100.0
```

## Step 7: Verify Service Discovery

### Test Service Discovery from Eureka

```bash
curl http://localhost:8761/eureka/apps/USER-SERVICE/USER-SERVICE:8081
```

Should return the instance details showing:
- Status: UP
- Port: 8081
- Health Check URL: http://localhost:8081/actuator/health

## Troubleshooting

### Issue: Service not appearing in Eureka

**Check logs for:**
```
Registering application USER-SERVICE with eureka
```

**If missing:**
1. Verify `@EnableDiscoveryClient` annotation is present
2. Check `eureka.client.service-url.defaultZone` is correct
3. Verify Eureka Server is running on 8761
4. Check firewall allows localhost:8761

### Issue: Connection refused to database

**Error:** `Connection refused`

**Solution:**
1. Verify Neon connection string is correct
2. Check username and password
3. Verify database name is `user_service_db`
4. Test connection: `psql "postgresql://[user]:[pass]@[host]/user_service_db?sslmode=require"`

### Issue: Port 8081 already in use

**Error:** `Address already in use`

**Solution:**
1. Kill process on port 8081:
   ```bash
   # Windows
   netstat -ano | findstr :8081
   taskkill /PID [PID] /F
   ```
2. Or change port in application.yaml:
   ```yaml
   server:
     port: 8082
   ```

### Issue: Eureka shows service as DOWN

**Check:**
1. Health endpoint: `curl http://localhost:8081/actuator/health`
2. Database connection is working
3. Check logs for errors

## Complete Verification Checklist

- [ ] Eureka Server running on port 8761
- [ ] User Service running on port 8081
- [ ] application.yaml updated with Neon credentials
- [ ] @EnableDiscoveryClient annotation added
- [ ] USER-SERVICE appears in Eureka dashboard
- [ ] USER-SERVICE status is UP
- [ ] Health check returns UP
- [ ] Can register a user
- [ ] Can retrieve user
- [ ] Can add funds to wallet
- [ ] Can check wallet balance
- [ ] Service discovery working

## Summary

If all tests pass:
✅ User Service is properly registered with Eureka
✅ Service discovery is working
✅ Database connection is established
✅ API endpoints are functional
✅ Ready for next microservice

## Next Steps

1. Create Booking Service (EJB)
2. Create Notification Service
3. Create Catalog Service
4. Create Admin Service
5. Set up RabbitMQ messaging
6. Create architecture diagram on Miro

