# APIdog Import Guide

## ✅ Yes, You Can Import This Collection into APIdog!

The Postman collection is **100% compatible** with APIdog. APIdog supports Postman collection format and can import it directly.

---

## 📥 How to Import into APIdog

### Method 1: Direct Import (Recommended)

1. **Open APIdog**
2. Click **Import** button (usually in top-left or menu)
3. Select **Postman Collection**
4. Choose file: `postman/collections/User-Service.postman_collection.json`
5. Click **Import**
6. Select environment: `postman/environments/User-Service-Local.postman_environment.json`
7. Click **Import Environment**

### Method 2: Via URL

1. Open APIdog
2. Click **Import**
3. Select **From URL**
4. Paste the collection file path or URL
5. Click **Import**

### Method 3: Drag and Drop

1. Open APIdog
2. Drag `User-Service.postman_collection.json` into APIdog window
3. Select **Import as Postman Collection**
4. Confirm import

---

## 🔧 APIdog Configuration

### 1. Set Base URL

In APIdog:
1. Go to **Environments**
2. Create new environment: **User Service - Local**
3. Add variable:
   - **Key**: `base_url`
   - **Value**: `http://localhost:8081`

### 2. Set JWT Token Variable

1. In same environment, add variable:
   - **Key**: `jwt_token`
   - **Value**: (empty - will be filled after login)

### 3. Import Environment File

1. Click **Environments**
2. Click **Import**
3. Select: `postman/environments/User-Service-Local.postman_environment.json`
4. Click **Import**

---

## 📋 Request Bodies Reference

All request bodies are documented in: `postman/request-bodies.json`

### Quick Copy-Paste Bodies

#### Register User
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer"
}
```

#### Login User
```json
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

#### Add Funds
```json
{
  "userId": 1,
  "amount": 100.00
}
```

#### Deduct Balance
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345"
}
```

#### Refund Balance
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345",
  "idempotencyKey": "REFUND-UUID-12345"
}
```

#### Update User
```json
{
  "email": "john.updated@example.com",
  "professionType": "Senior Software Engineer"
}
```

#### Validate Balance
```json
{
  "userId": 1,
  "amount": 50.00
}
```

---

## 🔐 JWT Token Management in APIdog

### After Login Request:

1. Send **Login** request
2. Copy token from response
3. In APIdog, go to **Environments**
4. Update `jwt_token` variable with copied token
5. All subsequent requests will use this token

### Or Use Pre-request Script:

In APIdog, you can add a pre-request script to automatically extract JWT:

```javascript
// After login request, extract token
const response = pm.response.json();
if (response.data && response.data.token) {
  pm.environment.set("jwt_token", response.data.token);
}
```

---

## 📡 Testing in APIdog

### 1. User Management Flow

1. **Register User** → Get user ID
2. **Login** → Get JWT token (copy to environment)
3. **Get User** → Verify user details
4. **Update User** → Change profile
5. **Delete User** → Remove account

### 2. Wallet Management Flow

1. **Add Funds** → Add 100.00
2. **Get Balance** → Verify 100.00
3. **Validate Balance** → Check if 50.00 available
4. **Deduct Balance** → Deduct 25.50
5. **Get Balance** → Verify 74.50
6. **Refund Balance** → Refund 25.50
7. **Get Balance** → Verify 100.00

---

## ✨ APIdog Features You Can Use

### Collections
- ✅ Organize endpoints by folders
- ✅ Add descriptions and documentation
- ✅ Version control integration
- ✅ Collaboration features

### Environments
- ✅ Multiple environments (Local, Dev, Prod)
- ✅ Variable management
- ✅ Environment switching
- ✅ Shared environments

### Testing
- ✅ Automated test scripts
- ✅ Response validation
- ✅ Performance testing
- ✅ Load testing

### Documentation
- ✅ Auto-generated API docs
- ✅ Mock server
- ✅ API sharing
- ✅ Team collaboration

---

## 🔄 Sync with Postman

If you update the collection in Postman:

1. Export from Postman: `File → Export → Collection`
2. Import into APIdog: `Import → Postman Collection`
3. Select the updated file
4. APIdog will merge/update the collection

---

## 📊 Comparison: Postman vs APIdog

| Feature | Postman | APIdog |
|---------|---------|--------|
| Import Postman Collections | ✅ | ✅ |
| JWT Management | ✅ | ✅ |
| Environment Variables | ✅ | ✅ |
| Pre-request Scripts | ✅ | ✅ |
| Response Validation | ✅ | ✅ |
| API Documentation | ✅ | ✅ |
| Mock Server | ✅ | ✅ |
| Team Collaboration | ✅ | ✅ |
| Performance Testing | ❌ | ✅ |
| Load Testing | ❌ | ✅ |

---

## 🚀 Quick Start with APIdog

1. **Import Collection**
   ```
   File → Import → Postman Collection
   Select: postman/collections/User-Service.postman_collection.json
   ```

2. **Import Environment**
   ```
   Environments → Import
   Select: postman/environments/User-Service-Local.postman_environment.json
   ```

3. **Select Environment**
   ```
   Top-right dropdown → User Service - Local
   ```

4. **Start Testing**
   ```
   Click any endpoint and send request
   ```

---

## 📝 Request Body Reference File

All request bodies are in: `postman/request-bodies.json`

This file contains:
- ✅ All 10 endpoint bodies
- ✅ Example responses
- ✅ Error responses
- ✅ Test scenarios
- ✅ Step-by-step workflows

---

## 🎯 Next Steps

1. ✅ Import collection into APIdog
2. ✅ Import environment into APIdog
3. ✅ Start the User Service server
4. ✅ Test all 10 endpoints
5. ✅ Use request bodies from `postman/request-bodies.json`

---

## 💡 Tips

- **Save JWT Token**: After login, copy token to environment variable
- **Use Variables**: Reference `{{base_url}}` and `{{jwt_token}}` in requests
- **Test Scenarios**: Follow the workflows in `postman/request-bodies.json`
- **Error Handling**: Check error responses for debugging
- **Documentation**: All endpoints documented in `postman/README.md`

---

## ❓ FAQ

**Q: Can I use APIdog instead of Postman?**
A: Yes! APIdog is fully compatible and has additional features like load testing.

**Q: Do I need to manually enter all request bodies?**
A: No! They're pre-configured in the collection. Just import and use.

**Q: How do I update the JWT token in APIdog?**
A: After login, copy the token and update the `jwt_token` environment variable.

**Q: Can I share the collection with my team?**
A: Yes! Export from APIdog and share the JSON file, or use APIdog's team collaboration features.

**Q: Are the request bodies in the collection?**
A: Yes! They're embedded in the collection. Also available separately in `postman/request-bodies.json`.

---

**Status**: ✅ Ready for APIdog Import

**Compatibility**: 100% Compatible with APIdog

**Last Updated**: May 1, 2026
