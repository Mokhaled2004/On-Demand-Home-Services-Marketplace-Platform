# Quick Test Commands - User Service & Eureka

## Prerequisites

Make sure both services are running:

**Terminal 1 - Eureka Server:**
```bash
cd eureka-server
mvn spring-boot:run
```

**Terminal 2 - User Service:**
```bash
cd user-service
mvn spring-boot:run
```

---

## Quick Tests

### 1. Check Eureka is Running
```bash
curl http://localhost:8761/eureka/apps
```

### 2. Check User Service is Registered
```bash
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

### 3. Check User Service Health
```bash
curl http://localhost:8081/actuator/health
```

### 4. Register a User
```bash
curl -X POST "http://localhost:8081/users/register?username=testuser&password=pass123&email=test@example.com&role=CUSTOMER"
```

### 5. Get User (ID 1)
```bash
curl http://localhost:8081/users/1
```

### 6. Add Funds to Wallet
```bash
curl -X POST "http://localhost:8081/wallet/add-funds?userId=1&amount=500"
```

### 7. Check Wallet Balance
```bash
curl http://localhost:8081/wallet/1
```

### 8. Deduct from Wallet
```bash
curl -X POST "http://localhost:8081/wallet/deduct?userId=1&amount=100"
```

### 9. Refund to Wallet
```bash
curl -X POST "http://localhost:8081/wallet/refund?userId=1&amount=50"
```

---

## Expected Results

| Test | Expected Status | Expected Response |
|------|-----------------|-------------------|
| Eureka Apps | 200 | XML with USER-SERVICE |
| User Service Registered | 200 | Instance details with status UP |
| Health Check | 200 | `{"status":"UP"}` |
| Register User | 200 | User object with ID |
| Get User | 200 | User object |
| Add Funds | 200 | "Funds added successfully" |
| Check Balance | 200 | `500.0` |
| Deduct | 200 | "Balance deducted successfully" |
| Refund | 200 | "Balance refunded successfully" |

---

## Verify Everything Works

Run this sequence:

```bash
# 1. Check Eureka
curl http://localhost:8761/eureka/apps

# 2. Check User Service registered
curl http://localhost:8761/eureka/apps/USER-SERVICE

# 3. Check health
curl http://localhost:8081/actuator/health

# 4. Register user
curl -X POST "http://localhost:8081/users/register?username=john&password=pass123&email=john@example.com&role=CUSTOMER"

# 5. Get user
curl http://localhost:8081/users/1

# 6. Add funds
curl -X POST "http://localhost:8081/wallet/add-funds?userId=1&amount=1000"

# 7. Check balance
curl http://localhost:8081/wallet/1

# 8. Deduct
curl -X POST "http://localhost:8081/wallet/deduct?userId=1&amount=200"

# 9. Check balance again
curl http://localhost:8081/wallet/1
```

---

## Success Indicators

✅ All curl commands return 200 status  
✅ USER-SERVICE appears in Eureka with status UP  
✅ User can be registered and retrieved  
✅ Wallet operations work correctly  
✅ No errors in console logs  

---

## If Something Fails

### Service not in Eureka?
- Check logs for "Registering application USER-SERVICE"
- Verify @EnableDiscoveryClient is in main class
- Restart User Service

### Database connection error?
- Verify Neon credentials in application.yaml
- Check database name is `user_service_db`
- Test connection manually

### Port already in use?
- Change port in application.yaml
- Or kill process: `netstat -ano | findstr :8081`

---

## Next: Create Other Microservices

Once User Service is verified:
1. Create Booking Service (port 8082)
2. Create Notification Service (port 8083)
3. Create Catalog Service (port 8084)
4. Create Admin Service (port 8085)

Each will have:
- Separate database in Neon
- Separate port
- Registered with Eureka
- Own API endpoints

