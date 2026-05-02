# User Service API - Postman Collection

Complete Postman collection for testing the User Service microservice API.

## Setup Instructions

### 1. Import Collection and Environment

1. Open Postman
2. Click **Import** button
3. Select `collections/User-Service.postman_collection.json`
4. Click **Import** button again
5. Select `environments/User-Service-Local.postman_environment.json`
6. Click **Import**

### 2. Select Environment

1. In Postman, click the environment dropdown (top-right)
2. Select **User Service - Local**

### 3. Start the Server

```bash
cd user-service
mvn spring-boot:run -DskipTests
```

Server will start on `http://localhost:8081`

## API Endpoints

### User Management

#### 1. Register User
- **Method**: POST
- **URL**: `http://localhost:8081/users/register`
- **Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER",
  "professionType": "Software Engineer"
}
```
- **Response**: User details with ID

#### 2. Login User
- **Method**: POST
- **URL**: `http://localhost:8081/users/login`
- **Body**:
```json
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```
- **Response**: JWT token (copy this to `jwt_token` variable)
- **Important**: After login, copy the token from response and update the `jwt_token` variable in Postman environment

#### 3. Get User by ID
- **Method**: GET
- **URL**: `http://localhost:8081/users/1`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Response**: User details

#### 4. Update User
- **Method**: PUT
- **URL**: `http://localhost:8081/users/1`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Body**:
```json
{
  "email": "john.updated@example.com",
  "professionType": "Senior Software Engineer"
}
```
- **Response**: Updated user details

#### 5. Delete User
- **Method**: DELETE
- **URL**: `http://localhost:8081/users/1`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Response**: 204 No Content

### Wallet Management

#### 1. Get Wallet Balance
- **Method**: GET
- **URL**: `http://localhost:8081/wallet/1`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Response**: Wallet details with current balance

#### 2. Add Funds
- **Method**: POST
- **URL**: `http://localhost:8081/wallet/add-funds`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Body**:
```json
{
  "userId": 1,
  "amount": 100.00
}
```
- **Response**: Updated wallet with new balance

#### 3. Deduct Balance
- **Method**: POST
- **URL**: `http://localhost:8081/wallet/deduct`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Body**:
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345"
}
```
- **Response**: Updated wallet with deducted balance
- **Note**: Idempotency key is auto-generated

#### 4. Refund Balance
- **Method**: POST
- **URL**: `http://localhost:8081/wallet/refund`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Body**:
```json
{
  "userId": 1,
  "amount": 25.50,
  "bookingId": "BOOKING-12345",
  "idempotencyKey": "REFUND-UUID-12345"
}
```
- **Response**: Updated wallet with refunded balance

#### 5. Validate Balance
- **Method**: POST
- **URL**: `http://localhost:8081/wallet/validate`
- **Headers**: `Authorization: Bearer {{jwt_token}}`
- **Body**:
```json
{
  "userId": 1,
  "amount": 50.00
}
```
- **Response**: Boolean (true if sufficient balance, false otherwise)

## Testing Workflow

### Complete Test Flow

1. **Register a new user**
   - Use the "Register User" request
   - Note the user ID from response

2. **Login**
   - Use the "Login User" request
   - Copy the JWT token from response
   - Paste it in Postman environment variable `jwt_token`

3. **Get User Details**
   - Use the "Get User by ID" request
   - Verify user information

4. **Add Funds to Wallet**
   - Use the "Add Funds" request
   - Add 100.00 to wallet

5. **Check Wallet Balance**
   - Use the "Get Wallet Balance" request
   - Verify balance is 100.00

6. **Deduct Balance**
   - Use the "Deduct Balance" request
   - Deduct 25.50 for booking

7. **Validate Balance**
   - Use the "Validate Balance" request
   - Check if 50.00 is available (should be true)

8. **Refund Balance**
   - Use the "Refund Balance" request
   - Refund 25.50

9. **Update User**
   - Use the "Update User" request
   - Update email and profession type

10. **Delete User** (optional)
    - Use the "Delete User" request
    - User account will be deleted

## Environment Variables

| Variable | Value | Description |
|----------|-------|-------------|
| `base_url` | http://localhost:8081 | Base URL for all requests |
| `jwt_token` | (empty) | JWT token from login - update after login |
| `user_id` | 1 | User ID for testing |
| `test_username` | john_doe | Test username |
| `test_password` | SecurePassword123! | Test password |
| `test_email` | john@example.com | Test email |
| `booking_id` | BOOKING-12345 | Test booking ID |

## Error Handling

### Common Errors

| Status | Error | Solution |
|--------|-------|----------|
| 401 | Unauthorized | JWT token expired or invalid - login again |
| 404 | Not Found | User or wallet not found - check user ID |
| 409 | Conflict | Username or email already exists - use different credentials |
| 400 | Bad Request | Invalid request body - check JSON format |
| 500 | Internal Server Error | Server error - check server logs |

## Notes

- All timestamps are in ISO 8601 format
- Amounts are in decimal format (e.g., 100.00)
- JWT tokens expire after 24 hours (configurable)
- Wallet is automatically created when user registers
- Idempotency keys prevent duplicate transactions
- All endpoints require JWT authentication except Register and Login

## Troubleshooting

### Server not starting
```bash
# Check if port 8081 is already in use
netstat -ano | findstr :8081

# Kill process using port 8081
taskkill /PID <PID> /F
```

### JWT token invalid
- Login again to get a new token
- Copy the token from the response
- Update the `jwt_token` environment variable

### Database connection error
- Ensure PostgreSQL is running
- Check database credentials in `application.yaml`
- Verify Neon connection string

## Support

For issues or questions, refer to:
- `docs/README.md` - Project documentation
- `IMPLEMENTATION_GUIDE.md` - Implementation details
- `SERVICE_COMMUNICATION_GUIDE.md` - Service communication patterns
