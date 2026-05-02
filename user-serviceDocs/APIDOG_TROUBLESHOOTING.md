# APIdog Troubleshooting Guide

## ❌ Error: "Invalid URI 'http://users/register'"

### Root Cause
The environment variable `{{base_url}}` is not being resolved. This happens when:
1. Environment is not selected
2. URL doesn't use the variable
3. Variable is not defined in environment

---

## ✅ Fix Steps

### Step 1: Select Environment

1. Look at **top-right** of APIdog
2. Find the environment dropdown (currently shows "none" or empty)
3. Click it
4. Select: **User Service - Local**

**Screenshot**: Should show "User Service - Local" in top-right

---

### Step 2: Update Request URLs

All URLs need to use `{{base_url}}` variable.

#### Current (Wrong):
```
POST /users/register
```

#### Fixed (Correct):
```
POST {{base_url}}/users/register
```

---

### Step 3: Verify Environment Variables

1. Click **Environments** in left sidebar
2. Select **User Service - Local**
3. Verify these variables exist:
   - `base_url` = `http://localhost:8081`
   - `jwt_token` = (empty initially)

---

## 🔧 Manual URL Fix for All Endpoints

### User Management

| Endpoint | Wrong | Correct |
|----------|-------|---------|
| Register | `/users/register` | `{{base_url}}/users/register` |
| Login | `/users/login` | `{{base_url}}/users/login` |
| Get User | `/users/{userId}` | `{{base_url}}/users/{userId}` |
| Update | `/users/{userId}` | `{{base_url}}/users/{userId}` |
| Delete | `/users/{userId}` | `{{base_url}}/users/{userId}` |

### Wallet Management

| Endpoint | Wrong | Correct |
|----------|-------|---------|
| Get Balance | `/wallet/{userId}` | `{{base_url}}/wallet/{userId}` |
| Add Funds | `/wallet/add-funds` | `{{base_url}}/wallet/add-funds` |
| Deduct | `/wallet/deduct` | `{{base_url}}/wallet/deduct` |
| Refund | `/wallet/refund` | `{{base_url}}/wallet/refund` |
| Validate | `/wallet/validate` | `{{base_url}}/wallet/validate` |

---

## 📋 Complete Checklist

- [ ] Environment selected: **User Service - Local**
- [ ] All URLs use `{{base_url}}`
- [ ] Server running on `http://localhost:8081`
- [ ] `base_url` variable = `http://localhost:8081`
- [ ] Request body is valid JSON
- [ ] Content-Type header = `application/json`

---

## 🚀 Quick Fix (Copy-Paste URLs)

### Register User Request
```
POST {{base_url}}/users/register
```

### Login Request
```
POST {{base_url}}/users/login
```

### Get User Request
```
GET {{base_url}}/users/1
```

### Add Funds Request
```
POST {{base_url}}/wallet/add-funds
```

### Deduct Balance Request
```
POST {{base_url}}/wallet/deduct
```

### Refund Request
```
POST {{base_url}}/wallet/refund
```

### Validate Balance Request
```
POST {{base_url}}/wallet/validate
```

---

## 🔍 Verify Environment Setup

### In APIdog:

1. **Click Environments** (left sidebar)
2. **Select User Service - Local**
3. **Verify variables**:
   ```
   base_url = http://localhost:8081
   jwt_token = (empty)
   user_id = 1
   test_username = john_doe
   test_password = SecurePassword123!
   test_email = john@example.com
   booking_id = BOOKING-12345
   ```

---

## 📝 Step-by-Step Fix

### 1. Select Environment
```
Top-right dropdown → User Service - Local
```

### 2. Update First Request (Register)
```
URL: {{base_url}}/users/register
Method: POST
Body: {
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer"
}
```

### 3. Send Request
```
Click Send button
Should get 201 Created response
```

### 4. If Still Error
```
Check:
- Is server running? (mvn spring-boot:run)
- Is environment selected?
- Does URL have {{base_url}}?
- Is JSON valid?
```

---

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Invalid URI | Use `{{base_url}}/endpoint` in URL |
| 404 Not Found | Check endpoint path is correct |
| 400 Bad Request | Verify JSON body is valid |
| 401 Unauthorized | Add JWT token to Authorization header |
| Connection refused | Start server: `mvn spring-boot:run` |
| Environment not found | Import: `postman/environments/User-Service-Local.postman_environment.json` |

---

## ✅ Correct Setup Example

### Register User Request (Correct)

**URL**: `{{base_url}}/users/register`

**Method**: POST

**Headers**:
```
Content-Type: application/json
```

**Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer"
}
```

**Expected Response** (201):
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
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

---

## 🎯 Next Steps

1. ✅ Select environment: **User Service - Local**
2. ✅ Update all URLs to use `{{base_url}}`
3. ✅ Verify server is running
4. ✅ Send Register request
5. ✅ Should get 201 response

---

## 📞 Still Having Issues?

1. **Check server logs**: `mvn spring-boot:run` output
2. **Verify environment**: Top-right should show "User Service - Local"
3. **Check URL**: Should be `{{base_url}}/users/register`
4. **Verify JSON**: Use online JSON validator
5. **Check headers**: Content-Type should be `application/json`

---

**Status**: Follow these steps and it will work!

**Last Updated**: May 1, 2026
