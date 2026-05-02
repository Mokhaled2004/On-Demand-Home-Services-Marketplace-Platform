# Request Bodies - Copy & Paste Ready

## User Management Endpoints

### 1. Register User
**Endpoint**: `POST /users/register`

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer",
  "initialBalance": 100.00
}
```

---

### 2. Login User
**Endpoint**: `POST /users/login`

```json
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

---

### 3. Get User by ID
**Endpoint**: `GET /users/{userId}`
**Headers**: `Authorization: Bearer {{jwt_token}}`
**No Body Required**

---

### 4. Update User
**Endpoint**: `PUT /users/{userId}`
**Headers**: `Authorization: Bearer {{jwt_token}}`

```json
{
  "email": "john.updated@example.com",
  "professionType": "Senior Software Engineer"
}
```

---

### 5. Delete User
**Endpoint**: `DELETE /users/{userId}`
**Headers**: `Authorization: Bearer {{jwt_token}}`
**No Body Required**

---

## Wallet Management Endpoints

### 1. Get Wallet Balance
**Endpoint**: `GET /wallet/{userId}`
**Headers**: `Authorization: Bearer {{jwt_token}}`
**No Body Required**

---

### 2. Add Funds
**Endpoint**: `POST /wallet/add-funds`
**Headers**: `Authorization: Bearer {{jwt_token}}`

```json
{
  "userId": 1,
  "amount": 100.00
}
```

---

### 3. Deduct Balance
**Endpoint**: `POST /wallet/deduct`
**Headers**: `Authorization: Bearer {{jwt_token}}`

```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345"
}
```

**Note**: Idempotency key is auto-generated

---

### 4. Refund Balance
**Endpoint**: `POST /wallet/refund`
**Headers**: `Authorization: Bearer {{jwt_token}}`

```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345",
  "idempotencyKey": "REFUND-UUID-12345"
}
```

---

### 5. Validate Balance
**Endpoint**: `POST /wallet/validate`
**Headers**: `Authorization: Bearer {{jwt_token}}`

```json
{
  "userId": 1,
  "amount": 50.00
}
```

---

## Test Data Examples

### Example 1: New User Registration
```json
{
  "username": "alice_smith",
  "email": "alice@example.com",
  "password": "AlicePassword123!",
  "role": "CUSTOMER",
  "professionType": "Nurse",
  "initialBalance": 50.00
}
```

### Example 2: Service Provider Registration
```json
{
  "username": "bob_plumber",
  "email": "bob@example.com",
  "password": "BobPassword123!",
  "role": "SERVICE_PROVIDER",
  "professionType": "Plumber",
  "initialBalance": 200.00
}
```

### Example 3: Large Wallet Deposit
```json
{
  "userId": 1,
  "amount": 1000.00
}
```

### Example 4: Multiple Deductions
```json
{
  "userId": 1,
  "amount": 50.00,
  "bookingId": "BOOKING-MULTI-001"
}
```

### Example 5: Refund with Custom Key
```json
{
  "userId": 1,
  "amount": 50.00,
  "bookingId": "BOOKING-MULTI-001",
  "idempotencyKey": "REFUND-MULTI-001-20260501"
}
```

---

## Response Examples

### Success Response (200)
```json
{
  "status": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "professionType": "Software Engineer"
  },
  "timestamp": "2026-05-01T22:20:00"
}
```

### Login Response (200)
```json
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6MSwiaWF0IjoxNzE0NTk2NDAwLCJleHAiOjE3MTQ2ODI4MDB9.signature",
    "role": "CUSTOMER"
  },
  "timestamp": "2026-05-01T22:20:00"
}
```

### Wallet Response (200)
```json
{
  "status": "SUCCESS",
  "data": {
    "userId": 1,
    "balance": 100.00,
    "currency": "USD"
  },
  "timestamp": "2026-05-01T22:20:00"
}
```

### Validation Response (200)
```json
{
  "status": "SUCCESS",
  "data": true,
  "timestamp": "2026-05-01T22:20:00"
}
```

### Error Response (401)
```json
{
  "status": "ERROR",
  "message": "Unauthorized - Invalid or expired JWT token",
  "data": null,
  "timestamp": "2026-05-01T22:20:00"
}
```

### Error Response (404)
```json
{
  "status": "ERROR",
  "message": "User not found: 999",
  "data": null,
  "timestamp": "2026-05-01T22:20:00"
}
```

### Error Response (409)
```json
{
  "status": "ERROR",
  "message": "Username already exists: john_doe",
  "data": null,
  "timestamp": "2026-05-01T22:20:00"
}
```

### Error Response (400 - Insufficient Balance)
```json
{
  "status": "ERROR",
  "message": "Insufficient balance. Required: 150.00, Available: 100.00",
  "data": null,
  "timestamp": "2026-05-01T22:20:00"
}
```

---

## Quick Reference Table

| Endpoint | Method | Auth | Body Required |
|----------|--------|------|---------------|
| /users/register | POST | ❌ | ✅ |
| /users/login | POST | ❌ | ✅ |
| /users/{id} | GET | ✅ | ❌ |
| /users/{id} | PUT | ✅ | ✅ |
| /users/{id} | DELETE | ✅ | ❌ |
| /wallet/{id} | GET | ✅ | ❌ |
| /wallet/add-funds | POST | ✅ | ✅ |
| /wallet/deduct | POST | ✅ | ✅ |
| /wallet/refund | POST | ✅ | ✅ |
| /wallet/validate | POST | ✅ | ✅ |

---

## Testing Workflow

### Step 1: Register
```json
{
  "username": "test_user",
  "email": "test@example.com",
  "password": "TestPassword123!",
  "role": "CUSTOMER",
  "professionType": "Developer",
  "initialBalance": 100.00
}
```

### Step 2: Login
```json
{
  "username": "test_user",
  "password": "TestPassword123!"
}
```
**→ Copy JWT token from response**

### Step 3: Add Funds
```json
{
  "userId": 1,
  "amount": 500.00
}
```

### Step 4: Deduct
```json
{
  "userId": 1,
  "amount": 100.00,
  "bookingId": "BOOKING-001"
}
```

### Step 5: Refund
```json
{
  "userId": 1,
  "amount": 100.00,
  "bookingId": "BOOKING-001",
  "idempotencyKey": "REFUND-001"
}
```

### Step 6: Update Profile
```json
{
  "email": "test.updated@example.com",
  "professionType": "Senior Developer"
}
```

---

**All bodies are ready to copy and paste!**

**Last Updated**: May 1, 2026
