# User Service - Quick Reference Card

## 🚀 Start Server
```bash
cd user-service
mvn spring-boot:run -DskipTests
```
**URL**: http://localhost:8081

---

## 📮 Postman Setup

1. Import: `postman/collections/User-Service.postman_collection.json`
2. Import: `postman/environments/User-Service-Local.postman_environment.json`
3. Select environment: **User Service - Local**
4. Start testing!

---

## 🔑 API Quick Reference

### User Endpoints
| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/users/register` | ❌ | Create account |
| POST | `/users/login` | ❌ | Get JWT token |
| GET | `/users/{id}` | ✅ | Get user info |
| PUT | `/users/{id}` | ✅ | Update profile |
| DELETE | `/users/{id}` | ✅ | Delete account |

### Wallet Endpoints
| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/wallet/{id}` | ✅ | Check balance |
| POST | `/wallet/add-funds` | ✅ | Add money |
| POST | `/wallet/deduct` | ✅ | Pay for booking |
| POST | `/wallet/refund` | ✅ | Refund money |
| POST | `/wallet/validate` | ✅ | Check if enough balance |

---

## 📝 Sample Requests

### Register
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer"
}
```

### Login
```json
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

### Add Funds
```json
{
  "userId": 1,
  "amount": 100.00
}
```

### Deduct Balance
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345"
}
```

### Refund
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345",
  "idempotencyKey": "REFUND-UUID-12345"
}
```

---

## 🔐 Authentication

1. **Login** to get JWT token
2. Copy token from response
3. Add to Postman environment variable: `jwt_token`
4. Use in header: `Authorization: Bearer {{jwt_token}}`

---

## 📊 Response Format

### Success (200)
```json
{
  "status": "SUCCESS",
  "message": "Operation successful",
  "data": { /* response data */ },
  "timestamp": "2026-05-01T22:20:00"
}
```

### Error (4xx/5xx)
```json
{
  "status": "ERROR",
  "message": "Error description",
  "data": null,
  "timestamp": "2026-05-01T22:20:00"
}
```

---

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Login again, copy new JWT token |
| 404 Not Found | Check user ID exists |
| 409 Conflict | Username/email already exists |
| 400 Bad Request | Check JSON format |
| Connection refused | Start server with `mvn spring-boot:run` |

---

## 📂 Key Files

| File | Purpose |
|------|---------|
| `user-service/pom.xml` | Dependencies |
| `user-service/src/main/resources/application.yaml` | Configuration |
| `postman/collections/User-Service.postman_collection.json` | API tests |
| `postman/environments/User-Service-Local.postman_environment.json` | Test variables |
| `postman/README.md` | Detailed guide |

---

## 🎯 Test Workflow

1. ✅ Register user
2. ✅ Login (copy JWT token)
3. ✅ Get user details
4. ✅ Add funds (100.00)
5. ✅ Check balance
6. ✅ Deduct (25.50)
7. ✅ Validate balance
8. ✅ Refund (25.50)
9. ✅ Update profile
10. ✅ Delete user

---

## 💾 Database

- **Type**: PostgreSQL (Neon)
- **Tables**: users, wallets, wallet_transactions, compensation_logs
- **Connection**: Configured in `application.yaml`
- **Status**: ✅ Connected and validated

---

## 🔧 Configuration

- **Port**: 8081
- **JWT Expiry**: 24 hours
- **Password Encoding**: BCrypt
- **Validation**: Enabled
- **Logging**: DEBUG for com.marketplace

---

## 📞 Support

- **Detailed Guide**: `postman/README.md`
- **Setup Guide**: `POSTMAN_SETUP.md`
- **Architecture**: `ARCHITECTURE_SUMMARY.md`
- **Database**: `docs/DBdocs/USER_SERVICE_DB_SCHEMA.md`

---

**Status**: ✅ Ready to Test

**Last Updated**: May 1, 2026
