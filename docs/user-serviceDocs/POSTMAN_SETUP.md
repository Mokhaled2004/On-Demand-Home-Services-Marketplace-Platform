# Postman Collection Setup Guide

## Quick Start

### 1. Import Files into Postman

**Collection:**
- File: `postman/collections/User-Service.postman_collection.json`
- Contains: All 10 API endpoints with pre-configured requests

**Environment:**
- File: `postman/environments/User-Service-Local.postman_environment.json`
- Contains: Base URL, JWT token variable, and test data

### 2. Select Environment

In Postman top-right dropdown, select: **User Service - Local**

### 3. Start Server

```bash
cd user-service
mvn spring-boot:run -DskipTests
```

Server runs on: `http://localhost:8081`

## API Endpoints Overview

### User Management (5 endpoints)
1. ✅ **POST** `/users/register` - Create new user account
2. ✅ **POST** `/users/login` - Login and get JWT token
3. ✅ **GET** `/users/{userId}` - Get user details
4. ✅ **PUT** `/users/{userId}` - Update user profile
5. ✅ **DELETE** `/users/{userId}` - Delete user account

### Wallet Management (5 endpoints)
1. ✅ **GET** `/wallet/{userId}` - Get wallet balance
2. ✅ **POST** `/wallet/add-funds` - Add funds to wallet
3. ✅ **POST** `/wallet/deduct` - Deduct balance for booking
4. ✅ **POST** `/wallet/refund` - Refund balance for cancellation
5. ✅ **POST** `/wallet/validate` - Check if sufficient balance

## Testing Checklist

- [ ] Register user
- [ ] Login and copy JWT token
- [ ] Get user details
- [ ] Add funds (100.00)
- [ ] Check wallet balance
- [ ] Deduct balance (25.50)
- [ ] Validate balance (50.00)
- [ ] Refund balance (25.50)
- [ ] Update user profile
- [ ] Delete user

## Key Features

✅ **Pre-configured Requests** - All endpoints ready to use
✅ **Sample Data** - Example payloads included
✅ **Environment Variables** - Reusable values across requests
✅ **JWT Authentication** - Token management built-in
✅ **Error Handling** - Common error scenarios documented
✅ **Complete Documentation** - Detailed README included

## Files Created

```
postman/
├── collections/
│   └── User-Service.postman_collection.json    (10 endpoints)
├── environments/
│   └── User-Service-Local.postman_environment.json
├── README.md                                    (Detailed guide)
└── globals/
    └── workspace.postman_globals.json          (Existing)
```

## Next Steps

1. Import collection and environment into Postman
2. Select the environment
3. Start the User Service server
4. Run the test workflow from the README
5. Verify all endpoints work correctly

## Support

Refer to `postman/README.md` for:
- Detailed endpoint documentation
- Request/response examples
- Troubleshooting guide
- Error handling reference
