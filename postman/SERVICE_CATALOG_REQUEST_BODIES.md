# Service Catalog API - Request Bodies Reference

## Base URL
```
http://localhost:8083/api/v1/catalog
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer {jwt_token}
```

---

## ADMIN ENDPOINTS (ROLE_ADMIN)

### 1. Create Category
**POST** `/categories`

**Request Body:**
```json
{
  "name": "Home Cleaning",
  "description": "Professional home cleaning services including deep cleaning, regular maintenance, and specialized cleaning"
}
```

**Response:** 201 Created
```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    "id": 1,
    "name": "Home Cleaning",
    "description": "Professional home cleaning services...",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:00:00"
  }
}
```

---

### 2. Get All Categories
**GET** `/categories`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Home Cleaning",
      "description": "Professional home cleaning services...",
      "createdAt": "2026-05-04T12:00:00",
      "updatedAt": "2026-05-04T12:00:00"
    },
    {
      "id": 2,
      "name": "Plumbing",
      "description": "Professional plumbing services...",
      "createdAt": "2026-05-04T12:05:00",
      "updatedAt": "2026-05-04T12:05:00"
    }
  ]
}
```

---

### 3. Get Category by ID
**GET** `/categories/{categoryId}`

**Example:** `GET /categories/1`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": 1,
    "name": "Home Cleaning",
    "description": "Professional home cleaning services...",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:00:00"
  }
}
```

**Error Response:** 404 Not Found
```json
{
  "success": false,
  "message": "Category not found with id: 1",
  "data": null
}
```

---

### 4. Update Category
**PUT** `/categories/{categoryId}`

**Example:** `PUT /categories/1`

**Request Body:**
```json
{
  "name": "Home Cleaning Services",
  "description": "Professional home cleaning services including deep cleaning, regular maintenance, specialized cleaning, and post-construction cleanup"
}
```

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Category updated successfully",
  "data": {
    "id": 1,
    "name": "Home Cleaning Services",
    "description": "Professional home cleaning services...",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:10:00"
  }
}
```

**Error Response:** 409 Conflict (duplicate name)
```json
{
  "success": false,
  "message": "Category with name 'Home Cleaning Services' already exists",
  "data": null
}
```

---

### 5. Delete Category
**DELETE** `/categories/{categoryId}`

**Example:** `DELETE /categories/1`

**Response:** 204 No Content (empty body)

**Error Response:** 404 Not Found
```json
{
  "success": false,
  "message": "Category not found with id: 1",
  "data": null
}
```

---

## PROVIDER ENDPOINTS (ROLE_SERVICE_PROVIDER)

### 6. Create Service Offer
**POST** `/offers`

**Request Body:**
```json
{
  "categoryId": 1,
  "title": "Premium Home Deep Cleaning",
  "description": "Complete deep cleaning service including all rooms, carpets, and hard-to-reach areas. Includes eco-friendly products.",
  "price": 150.00,
  "availableFrom": "2026-05-10T09:00:00",
  "availableTo": "2026-05-10T17:00:00"
}
```

**Response:** 201 Created
```json
{
  "success": true,
  "message": "Offer created successfully",
  "data": {
    "id": 1,
    "providerId": 123,
    "categoryId": 1,
    "title": "Premium Home Deep Cleaning",
    "description": "Complete deep cleaning service...",
    "price": 150.00,
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T17:00:00",
    "status": "ACTIVE",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:00:00"
  }
}
```

**Error Response:** 400 Bad Request (invalid data)
```json
{
  "success": false,
  "message": "Price must be greater than zero",
  "data": null
}
```

---

### 7. Get My Offers
**GET** `/offers/my-offers`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Offers retrieved successfully",
  "data": [
    {
      "id": 1,
      "providerId": 123,
      "categoryId": 1,
      "title": "Premium Home Deep Cleaning",
      "description": "Complete deep cleaning service...",
      "price": 150.00,
      "availableFrom": "2026-05-10T09:00:00",
      "availableTo": "2026-05-10T17:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-05-04T12:00:00",
      "updatedAt": "2026-05-04T12:00:00"
    }
  ]
}
```

---

### 8. Get Offer by ID
**GET** `/offers/{offerId}`

**Example:** `GET /offers/1`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Offer retrieved successfully",
  "data": {
    "id": 1,
    "providerId": 123,
    "categoryId": 1,
    "title": "Premium Home Deep Cleaning",
    "description": "Complete deep cleaning service...",
    "price": 150.00,
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T17:00:00",
    "status": "ACTIVE",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:00:00"
  }
}
```

---

### 9. Update Service Offer
**PUT** `/offers/{offerId}`

**Example:** `PUT /offers/1`

**Request Body:**
```json
{
  "title": "Premium Home Deep Cleaning - Updated",
  "description": "Complete deep cleaning service including all rooms, carpets, and hard-to-reach areas. Includes eco-friendly products and post-cleaning inspection.",
  "price": 175.00,
  "availableFrom": "2026-05-10T09:00:00",
  "availableTo": "2026-05-10T18:00:00",
  "status": "ACTIVE"
}
```

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Offer updated successfully",
  "data": {
    "id": 1,
    "providerId": 123,
    "categoryId": 1,
    "title": "Premium Home Deep Cleaning - Updated",
    "description": "Complete deep cleaning service...",
    "price": 175.00,
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T18:00:00",
    "status": "ACTIVE",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:15:00"
  }
}
```

**Error Response:** 403 Forbidden (not owner)
```json
{
  "success": false,
  "message": "You are not authorized to update this offer",
  "data": null
}
```

---

### 10. Deactivate Service Offer
**DELETE** `/offers/{offerId}`

**Example:** `DELETE /offers/1`

**Response:** 204 No Content (empty body)

**Error Response:** 403 Forbidden (not owner)
```json
{
  "success": false,
  "message": "You are not authorized to delete this offer",
  "data": null
}
```

---

## CUSTOMER ENDPOINTS (Any Authenticated User)

### 11. Get All Active Services
**GET** `/services`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": 1,
      "providerId": 123,
      "categoryId": 1,
      "title": "Premium Home Deep Cleaning",
      "description": "Complete deep cleaning service...",
      "price": 150.00,
      "availableFrom": "2026-05-10T09:00:00",
      "availableTo": "2026-05-10T17:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-05-04T12:00:00",
      "updatedAt": "2026-05-04T12:00:00"
    }
  ]
}
```

---

### 12. Get Services by Category
**GET** `/services/category/{categoryId}`

**Example:** `GET /services/category/1`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": 1,
      "providerId": 123,
      "categoryId": 1,
      "title": "Premium Home Deep Cleaning",
      "description": "Complete deep cleaning service...",
      "price": 150.00,
      "availableFrom": "2026-05-10T09:00:00",
      "availableTo": "2026-05-10T17:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-05-04T12:00:00",
      "updatedAt": "2026-05-04T12:00:00"
    }
  ]
}
```

---

### 13. Search Services
**GET** `/services/search?keyword=cleaning&categoryId=1&minPrice=50&maxPrice=200`

**Query Parameters (all optional):**
- `keyword`: Search in title and description
- `categoryId`: Filter by category ID
- `minPrice`: Minimum price filter
- `maxPrice`: Maximum price filter

**Example Queries:**
```
GET /services/search?keyword=cleaning
GET /services/search?categoryId=1
GET /services/search?minPrice=50&maxPrice=200
GET /services/search?keyword=cleaning&categoryId=1&minPrice=50&maxPrice=200
```

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": 1,
      "providerId": 123,
      "categoryId": 1,
      "title": "Premium Home Deep Cleaning",
      "description": "Complete deep cleaning service...",
      "price": 150.00,
      "availableFrom": "2026-05-10T09:00:00",
      "availableTo": "2026-05-10T17:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-05-04T12:00:00",
      "updatedAt": "2026-05-04T12:00:00"
    }
  ]
}
```

---

### 14. Get Service Details
**GET** `/services/{offerId}`

**Example:** `GET /services/1`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Service retrieved successfully",
  "data": {
    "id": 1,
    "providerId": 123,
    "categoryId": 1,
    "title": "Premium Home Deep Cleaning",
    "description": "Complete deep cleaning service...",
    "price": 150.00,
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T17:00:00",
    "status": "ACTIVE",
    "createdAt": "2026-05-04T12:00:00",
    "updatedAt": "2026-05-04T12:00:00"
  }
}
```

**Error Response:** 404 Not Found (inactive or not found)
```json
{
  "success": false,
  "message": "Offer not found with id: 1",
  "data": null
}
```

---

### 15. Check Service Availability
**GET** `/services/{offerId}/availability`

**Example:** `GET /services/1/availability`

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Availability retrieved successfully",
  "data": {
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T17:00:00",
    "status": "ACTIVE",
    "isAvailable": true
  }
}
```

**Response (Not Available):** 200 OK
```json
{
  "success": true,
  "message": "Availability retrieved successfully",
  "data": {
    "availableFrom": "2026-05-10T09:00:00",
    "availableTo": "2026-05-10T17:00:00",
    "status": "ACTIVE",
    "isAvailable": false
  }
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid request data",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized - Invalid or missing JWT token",
  "data": null
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Forbidden - Insufficient permissions",
  "data": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null
}
```

### 409 Conflict
```json
{
  "success": false,
  "message": "Conflict - Resource already exists",
  "data": null
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error",
  "data": null
}
```

---

## Testing Tips

1. **Set JWT Token Variable:**
   - Get a valid JWT token from User Service
   - Set `{{jwt_token}}` variable in Postman with your token

2. **Set Base URL Variable:**
   - Default: `http://localhost:8083/api/v1/catalog`
   - Update `{{base_url}}` if running on different port

3. **Test Flow:**
   - Create categories first (Admin)
   - Create offers (Provider)
   - Browse and search services (Customer)
   - Check availability (Customer)

4. **Role-Based Access:**
   - Admin endpoints require `ROLE_ADMIN`
   - Provider endpoints require `ROLE_SERVICE_PROVIDER`
   - Customer endpoints require any authenticated user
