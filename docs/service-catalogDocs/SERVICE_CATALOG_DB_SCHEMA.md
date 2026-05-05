# Service Catalog Database Schema - FINAL VERSION

## 📋 Complete SQL Schema

```sql
-- ============================================
-- SERVICE CATALOG DATABASE SCHEMA
-- ============================================

-- ============================================
-- ENUMS (Type Safety)
-- ============================================

-- Drop if exists (for re-running migrations)
DROP TYPE IF EXISTS service_offer_status_enum CASCADE;

-- Create enum type
CREATE TYPE service_offer_status_enum AS ENUM ('ACTIVE', 'INACTIVE');

-- ============================================
-- TABLE 1: SERVICE_CATEGORIES
-- ============================================
-- Stores service categories (Plumbing, Carpentry, Electrical, etc.)
-- Admin can add new categories

CREATE TABLE service_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create unique index with LOWER function for case-insensitive uniqueness
CREATE UNIQUE INDEX idx_categories_name_unique ON service_categories(LOWER(name));

-- ============================================
-- TABLE 2: SERVICE_OFFERS
-- ============================================
-- Stores service offers created by providers
-- Each offer is for a specific service at a specific price and date
-- provider_id is NOT a foreign key (different database - User Service)
-- category_id IS a foreign key (same database)

CREATE TABLE service_offers (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,  -- References users.id in USER-SERVICE (NOT FK)
    category_id BIGINT NOT NULL,  -- References service_categories.id (FK)
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(15, 2) NOT NULL,
    available_from TIMESTAMP NOT NULL,  -- Start of availability window
    available_to TIMESTAMP NOT NULL,    -- End of availability window
    status service_offer_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE CASCADE,
    CHECK (price > 0),  -- Ensure positive price
    CHECK (available_to > available_from)  -- Ensure valid time range
);

CREATE INDEX idx_offers_provider ON service_offers(provider_id);
CREATE INDEX idx_offers_category ON service_offers(category_id);
CREATE INDEX idx_offers_status ON service_offers(status);
CREATE INDEX idx_offers_available_from ON service_offers(available_from);
CREATE INDEX idx_offers_available_to ON service_offers(available_to);
CREATE INDEX idx_offers_created ON service_offers(created_at);
-- Composite index for common query: browse by category and status
CREATE INDEX idx_offers_category_status ON service_offers(category_id, status);

-- ============================================
-- SUMMARY
-- ============================================
/*
✅ SERVICE_CATEGORIES: Admin adds new service types
✅ SERVICE_OFFERS: Providers create offers with price and date
✅ provider_id: NOT a FK (links to USER-SERVICE via REST API)
✅ category_id: IS a FK (same database)
✅ status: ACTIVE or INACTIVE (no COMPLETED - that's in Booking Service)
✅ Indexes for query performance
*/
```

---

## 📊 Visual Relationships

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│              SERVICE CATALOG DATABASE                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│       SERVICE_CATEGORIES                 │
├──────────────────────────────────────────┤
│ id (PK)                                  │
│ name (UNIQUE)                            │
│ description                              │
│ created_at                               │
│ updated_at                               │
└──────────────────────────────────────────┘
         │
         │ (1-to-many)
         │
         ▼
┌──────────────────────────────────────────┐
│       SERVICE_OFFERS                     │
├──────────────────────────────────────────┤
│ id (PK)                                  │
│ provider_id (NOT FK - User Service)      │
│ category_id (FK) ◄──────────────────────┤
│ title                                    │
│ description                              │
│ price                                    │
│ available_date                           │
│ status (ACTIVE, INACTIVE)                │
│ created_at                               │
│ updated_at                               │
└──────────────────────────────────────────┘
```

---

## 🔑 Table Details

### SERVICE_CATEGORIES

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| name | VARCHAR(255) | UNIQUE, NOT NULL | Category name (Plumbing, Carpentry, etc.) |
| description | TEXT | NULL | Details about the category |
| created_at | TIMESTAMP | DEFAULT NOW() | When created |
| updated_at | TIMESTAMP | DEFAULT NOW() | When last updated |

**Indexes:**
- idx_categories_name (name)

---

### SERVICE_OFFERS

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| provider_id | BIGINT | NOT NULL | Provider ID (from USER-SERVICE, NOT FK) |
| category_id | BIGINT | NOT NULL, FK | Links to SERVICE_CATEGORIES |
| title | VARCHAR(255) | NOT NULL | Service name (e.g., "Pipe Repair") |
| description | TEXT | NULL | What's included in the service |
| price | DECIMAL(15,2) | NOT NULL, CHECK > 0 | Service cost |
| available_from | TIMESTAMP | NOT NULL | Start of availability window |
| available_to | TIMESTAMP | NOT NULL | End of availability window |
| status | service_offer_status_enum | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE or INACTIVE |
| created_at | TIMESTAMP | DEFAULT NOW() | When offer was created |
| updated_at | TIMESTAMP | DEFAULT NOW() | When offer was last updated |

**Indexes:**
- idx_offers_provider (provider_id)
- idx_offers_category (category_id)
- idx_offers_status (status)
- idx_offers_available_from (available_from)
- idx_offers_available_to (available_to)
- idx_offers_created (created_at)
- idx_offers_category_status (category_id, status) - Composite index for browsing

**Constraints:**
- CHECK (price > 0) - Ensure positive price
- CHECK (available_to > available_from) - Ensure valid time range

---

## 💡 Key Design Decisions

| Decision | Reason |
|----------|--------|
| provider_id NOT FK | Different database (User Service) - use REST API instead |
| category_id IS FK | Same database - can use foreign key |
| status: ACTIVE/INACTIVE only | COMPLETED status belongs in Booking Service |
| price CHECK constraint | Prevent negative or zero prices |
| available_from/available_to TIMESTAMP | Support time slots, not just dates |
| CHECK (available_to > available_from) | Ensure valid time ranges |
| UNIQUE (LOWER(name)) on categories | Case-insensitive uniqueness |
| Composite index (category_id, status) | Optimize common browse queries |
| Comprehensive indexes | Query performance for filtering |

---

## 🎓 Senior-Level Improvements Applied

### 1️⃣ Case-Insensitive Category Names
```sql
UNIQUE (LOWER(name))
```
✅ Prevents: "Plumbing" and "plumbing" being treated as different categories

### 2️⃣ Time Slot Support (Not Just Dates)
```sql
available_from TIMESTAMP
available_to TIMESTAMP
CHECK (available_to > available_from)
```
✅ Supports: Multiple time slots per day, realistic booking windows

### 3️⃣ Composite Index for Performance
```sql
CREATE INDEX idx_offers_category_status ON service_offers(category_id, status);
```
✅ Optimizes: Common query `WHERE category_id = ? AND status = 'ACTIVE'`

### 4️⃣ Ownership Validation (Application Level)
```java
// Always validate provider exists and has PROVIDER role
User provider = userService.getUser(offer.getProviderId());
if (provider == null || !provider.getRole().equals("PROVIDER")) {
    throw new UnauthorizedException("Invalid provider");
}
```
✅ Ensures: Data integrity across microservices

### 5️⃣ Soft Delete Pattern
```sql
status = 'INACTIVE'  -- Instead of DELETE
```
✅ Preserves: Historical data, audit trail

---

## 🔄 How It Works

### Admin: Add New Category
```sql
INSERT INTO service_categories (name, description)
VALUES ('Plumbing', 'Pipe repair and installation services');
-- Result: category_id = 1
```

### Provider: Create Service Offer
```sql
INSERT INTO service_offers 
(provider_id, category_id, title, description, price, available_from, available_to, status)
VALUES (5, 1, 'Pipe Repair', 'Fix leaking pipes', 80.00, 
        '2026-05-15 09:00:00', '2026-05-15 17:00:00', 'ACTIVE');
-- Result: offer_id = 100
-- provider_id = 5 (from USER-SERVICE)
-- category_id = 1 (Plumbing)
-- Available: May 15, 9 AM to 5 PM
```

### Customer: Browse Services by Category (with time range)
```sql
SELECT * FROM service_offers 
WHERE category_id = 1 
  AND status = 'ACTIVE'
  AND available_from >= NOW()
ORDER BY price ASC;
-- Returns: All active plumbing services available in the future, sorted by price
```

### Provider: View All Their Active Offers
```sql
SELECT * FROM service_offers 
WHERE provider_id = 5 AND status = 'ACTIVE'
ORDER BY created_at DESC;
-- Returns: All offers created by provider 5
```

### Provider: Update Offer (Change Price or Availability Window)
```sql
UPDATE service_offers 
SET price = 90.00, 
    available_from = '2026-05-20 10:00:00',
    available_to = '2026-05-20 18:00:00',
    updated_at = NOW()
WHERE id = 100;
```

### Provider: Deactivate Offer
```sql
UPDATE service_offers 
SET status = 'INACTIVE', updated_at = NOW()
WHERE id = 100;
```

---

## ✅ Supports All Requirements

✅ **Admin:** Add new service category  
✅ **Provider:** Create service offer with price and available date  
✅ **Provider:** View all active service offers  
✅ **Provider:** Update service offer pricing and availability  
✅ **Provider:** View completed services (via Booking Service REST call)  
✅ **Customer:** Browse services by category  
✅ **Customer:** Check service availability  

---

## 📝 Microservices Integration

**Service Catalog DB** (This Database)
- Stores categories and offers
- Independent database

**User Service DB** (Different Database)
- Stores user/provider info
- Called via REST API when needed

**Booking Service DB** (Different Database - Not Yet Created)
- Stores bookings and completed services
- Will track which offers were booked

**How They Connect:**
```
Customer browses services:
1. Call Catalog Service: GET /services?category=1
2. Returns: SERVICE_OFFERS with provider_id
3. If customer wants provider details:
   Call User Service: GET /users/{provider_id}
4. If customer wants to book:
   Call Booking Service: POST /bookings
```

---

## 🎓 This is the Final Service Catalog DB!

Ready to:
1. ✅ Draw ERD on Miro?
2. ✅ Create SQL migration files?
3. ✅ Create JPA Entity classes?
4. ✅ Move to Booking Service DB design?

What's next?


---

## 🎤 Viva Talking Points (Elite Level)

### 1. Microservices Isolation
**Q: Why isn't provider_id a foreign key?**
```
A: "In microservices architecture, each service owns its database. 
   provider_id references the User Service database, which is separate. 
   We use REST API calls to validate provider existence, not database constraints.
   This ensures loose coupling and independent scalability."
```

### 2. Time Slot Design
**Q: Why available_from and available_to instead of just available_date?**
```
A: "Real-world services need time slots, not just dates. 
   A plumber might be available 9 AM-5 PM on May 15.
   This design supports multiple bookings per day and realistic scheduling.
   The CHECK constraint ensures available_to > available_from for data integrity."
```

### 3. Case-Insensitive Categories
**Q: Why UNIQUE (LOWER(name))?**
```
A: "Prevents duplicate categories due to case differences. 
   'Plumbing' and 'plumbing' are the same category.
   This ensures data consistency and prevents user confusion."
```

### 4. Composite Index
**Q: Why idx_offers_category_status?**
```
A: "The most common query is browsing services by category and status.
   A composite index on (category_id, status) optimizes this query.
   It's more efficient than separate indexes for this specific access pattern."
```

### 5. Soft Delete Pattern
**Q: Why use status = 'INACTIVE' instead of DELETE?**
```
A: "Soft deletes preserve historical data and audit trails.
   If we hard delete, we lose information about what offers existed.
   This is important for analytics, debugging, and compliance."
```

### 6. No COMPLETED Status
**Q: Why doesn't SERVICE_OFFERS have a COMPLETED status?**
```
A: "COMPLETED status belongs in the Booking Service, not Catalog Service.
   Each service has a single responsibility. 
   Catalog manages available services; Booking tracks which ones were booked.
   This separation keeps the schema focused and maintainable."
```

### 7. Ownership Validation
**Q: How do you ensure only valid providers can create offers?**
```
A: "We validate at the application level via REST API:
   1. Extract provider_id from JWT token
   2. Call User Service: GET /users/{provider_id}
   3. Verify role = 'PROVIDER'
   4. Only then allow offer creation
   This ensures data integrity across microservices."
```

---

## 🏆 This Schema is Production-Ready!

✅ Microservices-correct (no cross-DB FKs)  
✅ Time slot support (realistic scheduling)  
✅ Case-insensitive categories (data consistency)  
✅ Optimized indexes (query performance)  
✅ Soft delete pattern (audit trail)  
✅ Ownership validation (security)  
✅ Focused responsibility (single concern)  

**Elite-level design for your assignment!** 🔥
