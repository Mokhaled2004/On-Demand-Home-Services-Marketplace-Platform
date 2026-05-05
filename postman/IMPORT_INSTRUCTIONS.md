# Postman Collection Import Instructions

## Single Unified Collection

**File:** `postman/collections/Marketplace-API.postman_collection.json`

This collection contains **31 endpoints** organized by service:

### Structure:
```
Marketplace API
├── User Service (16 endpoints)
│   ├── User Management (5)
│   │   ├── Register User
│   │   ├── Login User
│   │   ├── Get User by ID
│   │   ├── Update User
│   │   └── Delete User
│   ├── Wallet Management (5)
│   │   ├── Get My Wallet Balance
│   │   ├── Add Funds
│   │   ├── Deduct Balance
│   │   ├── Refund Balance
│   │   └── Validate Balance
│   └── Admin Management (6)
│       ├── Get All Users
│       ├── Get All Transactions
│       ├── Get All Compensation Logs
│       ├── Get User Transactions
│       └── Get User Compensation Log
└── Service Catalog (15 endpoints)
    ├── Admin - Categories (5)
    │   ├── Create Category
    │   ├── Get All Categories
    │   ├── Get Category by ID
    │   ├── Update Category
    │   └── Delete Category
    ├── Provider - Offers (5)
    │   ├── Create Service Offer
    │   ├── Get My Offers
    │   ├── Get Offer by ID
    │   ├── Update Service Offer
    │   └── Deactivate Service Offer
    └── Customer - Browse Services (5)
        ├── Get All Active Services
        ├── Get Services by Category
        ├── Search Services
        ├── Get Service Details
        └── Check Service Availability
```

## How to Import

1. **Open Postman**
2. **Click "Import"** (top left)
3. **Select "Upload Files"**
4. **Choose:** `postman/collections/Marketplace-API.postman_collection.json`
5. **Click "Import"**

## Environment Variables

After importing, set these variables in Postman:

### Collection Variables (in Marketplace API collection):
- `user_service_url`: `http://localhost:8081`
- `catalog_service_url`: `http://localhost:8083/api/v1/catalog`
- `jwt_token`: Your JWT token from User Service login
- `admin_jwt_token`: Your admin JWT token from User Service login

## Testing Flow

1. **Register a user** (User Service → User Management → Register User)
2. **Login** (User Service → User Management → Login User) - Copy the JWT token
3. **Set `jwt_token` variable** with the token from step 2
4. **Test endpoints** in any order

## Notes

- All endpoints are organized by service name first
- Each service has its own folder with sub-folders for endpoint groups
- Request bodies are pre-filled with sample data
- All endpoints include proper descriptions and error codes
