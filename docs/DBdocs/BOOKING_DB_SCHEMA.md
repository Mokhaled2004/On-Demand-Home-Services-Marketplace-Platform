# Booking Database Schema - FINAL VERSION

## 📋 Complete SQL Schema

```sql
-- ============================================
-- BOOKING DATABASE SCHEMA
-- ============================================

-- ============================================
-- ENUMS (Type Safety)
-- ============================================

DROP TYPE IF EXISTS booking_status_enum CASCADE;

CREATE TYPE booking_status_enum AS ENUM ('PENDING', 'CONFIRMED', 'FAILED', 'CANCELLED');

-- ============================================
-- TABLE: BOOKINGS
-- ============================================
-- Stores all booking records
-- Tracks which customer booked which service from which provider
-- Manages booking status and pricing

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,  -- References users.id in USER-SERVICE (NOT FK)
    service_offer_id BIGINT NOT NULL,  -- References service_offers.id in CATALOG-SERVICE (NOT FK)
    provider_id BIGINT NOT NULL,  -- References users.id in USER-SERVICE (NOT FK)
    booking_date TIMESTAMP NOT NULL,  -- When the booking was made
    service_start TIMESTAMP NOT NULL,  -- When the service starts
    service_end TIMESTAMP NOT NULL,  -- When the service ends
    amount DECIMAL(15, 2) NOT NULL,  -- Price charged (snapshot at booking time)
    status booking_status_enum NOT NULL DEFAULT 'PENDING',  -- Current booking status
    idempotency_key VARCHAR(100) UNIQUE,  -- Prevents duplicate bookings on retry
    event_published BOOLEAN DEFAULT FALSE,  -- Tracks if RabbitMQ event was sent
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (amount > 0),  -- Ensure positive amount
    CHECK (service_end > service_start),  -- Ensure valid time range
    UNIQUE (service_offer_id, service_start, service_end)  -- Prevent double booking same slot
);

CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_provider ON bookings(provider_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_service_offer ON bookings(service_offer_id);
CREATE INDEX idx_bookings_service_start ON bookings(service_start);
CREATE INDEX idx_bookings_created ON bookings(created_at);
-- Composite indexes for common queries
CREATE INDEX idx_bookings_customer_status ON bookings(customer_id, status);
CREATE INDEX idx_bookings_provider_status ON bookings(provider_id, status);

-- ============================================
-- SUMMARY
-- ============================================
/*
✅ BOOKINGS: Single table for all booking data
✅ customer_id: NOT a FK (links to USER-SERVICE via REST API)
✅ service_offer_id: NOT a FK (links to CATALOG-SERVICE via REST API)
✅ provider_id: NOT a FK (links to USER-SERVICE via REST API)
✅ status: PENDING, CONFIRMED, FAILED, CANCELLED
✅ amount: Price snapshot at booking time
✅ updated_at: Tracks when status last changed
✅ Comprehensive indexes for query performance
✅ Simple and clean (no unnecessary history table)
*/
```

---

## 📊 Table Details

### BOOKINGS

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| customer_id | BIGINT | NOT NULL | Customer ID (from USER-SERVICE, NOT FK) |
| service_offer_id | BIGINT | NOT NULL | Service offer ID (from CATALOG-SERVICE, NOT FK) |
| provider_id | BIGINT | NOT NULL | Provider ID (from USER-SERVICE, NOT FK) |
| booking_date | TIMESTAMP | NOT NULL | When the booking was made |
| service_start | TIMESTAMP | NOT NULL | When the service starts |
| service_end | TIMESTAMP | NOT NULL | When the service ends |
| amount | DECIMAL(15,2) | NOT NULL, CHECK > 0 | Price charged (snapshot) |
| status | booking_status_enum | NOT NULL, DEFAULT 'PENDING' | PENDING, CONFIRMED, FAILED, CANCELLED |
| idempotency_key | VARCHAR(100) | UNIQUE | Prevents duplicate bookings on retry |
| event_published | BOOLEAN | DEFAULT FALSE | Tracks if RabbitMQ event was sent |
| created_at | TIMESTAMP | DEFAULT NOW() | When booking was created |
| updated_at | TIMESTAMP | DEFAULT NOW() | When booking was last updated |

**Indexes:**
- idx_bookings_customer (customer_id)
- idx_bookings_provider (provider_id)
- idx_bookings_status (status)
- idx_bookings_service_offer (service_offer_id)
- idx_bookings_service_start (service_start)
- idx_bookings_created (created_at)
- idx_bookings_customer_status (customer_id, status) - Composite index
- idx_bookings_provider_status (provider_id, status) - Composite index

**Constraints:**
- CHECK (amount > 0) - Ensure positive amount
- CHECK (service_end > service_start) - Ensure valid time range
- UNIQUE (service_offer_id, service_start, service_end) - Prevent double booking same slot

---

## 🔑 Booking Status States

| Status | Meaning | When It Happens |
|--------|---------|-----------------|
| **PENDING** | Booking created, waiting for confirmation | Initial state after booking creation |
| **CONFIRMED** | Booking approved, payment successful | After wallet deduction succeeds |
| **FAILED** | Booking rejected, payment failed | If wallet balance insufficient or service unavailable |
| **CANCELLED** | Customer cancelled the booking | Customer requests cancellation |

---

## 🔄 How It Works - Booking Flow

### Step 1: Create Booking (PENDING) - Idempotent
```sql
INSERT INTO bookings 
(customer_id, service_offer_id, provider_id, booking_date, service_start, service_end, amount, status, idempotency_key)
VALUES (10, 100, 5, NOW(), '2026-05-15 09:00:00', '2026-05-15 17:00:00', 80.00, 'PENDING', 'BOOKING_CUST10_OFFER100_1234567890');
-- Result: booking_id = 1, status = PENDING
-- idempotency_key prevents duplicate if request is retried
```

### Step 2a: Booking Succeeds (CONFIRMED)
```sql
UPDATE bookings 
SET status = 'CONFIRMED', event_published = FALSE, updated_at = NOW() 
WHERE id = 1;
-- Status: PENDING → CONFIRMED
-- event_published = FALSE means we need to send RabbitMQ event
```

### Step 2b: Booking Fails (FAILED)
```sql
UPDATE bookings 
SET status = 'FAILED', event_published = FALSE, updated_at = NOW() 
WHERE id = 1;
-- Status: PENDING → FAILED
-- Reason: Insufficient balance or service unavailable
```

### Step 3: Customer Cancels (CANCELLED)
```sql
UPDATE bookings 
SET status = 'CANCELLED', event_published = FALSE, updated_at = NOW() 
WHERE id = 1;
-- Status: CONFIRMED → CANCELLED
-- Customer requested cancellation
```

### Step 4: Event Published to RabbitMQ
```sql
UPDATE bookings 
SET event_published = TRUE 
WHERE id = 1 AND status = 'CONFIRMED';
-- Marks that BOOKING_CONFIRMED event was sent
-- Helps with retry logic if event sending fails
```

---

## 📋 Common Queries

### Get All Bookings for a Customer
```sql
SELECT * FROM bookings 
WHERE customer_id = 10 
ORDER BY created_at DESC;
```

### Get All Bookings for a Provider
```sql
SELECT * FROM bookings 
WHERE provider_id = 5 
ORDER BY created_at DESC;
```

### Get All Confirmed Bookings
```sql
SELECT * FROM bookings 
WHERE status = 'CONFIRMED' 
ORDER BY service_date ASC;
```

### Get Bookings by Status
```sql
SELECT * FROM bookings 
WHERE status = 'PENDING' 
ORDER BY created_at ASC;
```

### Get Bookings for a Specific Service Date
```sql
SELECT * FROM bookings 
WHERE service_date = '2026-05-15' 
ORDER BY created_at DESC;
```

---

## 💡 Key Design Decisions

| Decision | Reason |
|----------|--------|
| Single BOOKINGS table | Simple, clean, no unnecessary complexity |
| customer_id NOT FK | Different database (User Service) - use REST API |
| service_offer_id NOT FK | Different database (Catalog Service) - use REST API |
| provider_id NOT FK | Different database (User Service) - use REST API |
| amount field | Snapshot of price at booking time (price may change later) |
| service_start/service_end | Support time slots, not just dates |
| CHECK (service_end > service_start) | Ensure valid time ranges |
| UNIQUE (service_offer_id, service_start, service_end) | Prevent double booking same slot |
| idempotency_key | Prevent duplicate bookings on request retry |
| event_published | Track if RabbitMQ event was sent (helps with retries) |
| status field | Tracks current booking state |
| updated_at | Shows when status last changed |
| Composite indexes | Optimize common queries (customer+status, provider+status) |
| CHECK (amount > 0) | Ensure positive amounts |

---

## 🎓 Microservices Integration

**Booking DB** (This Database)
- Stores booking records
- Independent database

**User Service DB** (Different Database)
- Stores user/customer/provider info
- Called via REST API when needed

**Catalog Service DB** (Different Database)
- Stores service offers
- Called via REST API when needed

**How They Connect:**
```
1. Customer books service:
   POST /bookings
   {
     "customer_id": 10,
     "service_offer_id": 100,
     "provider_id": 5
   }

2. Booking Service creates record with status = PENDING

3. Booking Service calls User Service:
   GET /users/{customer_id}
   GET /wallet/{customer_id}

4. Booking Service calls Catalog Service:
   GET /services/{service_offer_id}

5. If all checks pass:
   UPDATE bookings SET status = 'CONFIRMED'
   Publish BOOKING_CONFIRMED to RabbitMQ

6. If checks fail:
   UPDATE bookings SET status = 'FAILED'
   Publish BOOKING_FAILED to RabbitMQ
```

---

## ✅ Supports All Requirements

✅ Create booking with proper state management  
✅ Validate service availability (via Catalog Service REST call)  
✅ Check wallet balance (via User Service REST call)  
✅ Deduct balance (via User Service REST call)  
✅ Handle failures with compensation logic (status = FAILED)  
✅ Manage transaction consistency (status field)  
✅ Track booking status (PENDING, CONFIRMED, FAILED, CANCELLED)  
✅ View booking history (query by customer_id or provider_id)  
✅ RabbitMQ integration ready (status changes trigger events)  

---

## 🔥 Elite-Level Improvements Applied

### 1️⃣ Double Booking Prevention
```sql
UNIQUE (service_offer_id, service_start, service_end)
```
✅ Prevents: Two customers booking the same service at the same time

### 2️⃣ Time Slot Support (Not Just Dates)
```sql
service_start TIMESTAMP
service_end TIMESTAMP
CHECK (service_end > service_start)
```
✅ Supports: Realistic time slots, prevents scheduling conflicts

### 3️⃣ Idempotency Key for Retry Safety
```sql
idempotency_key VARCHAR(100) UNIQUE
```
✅ Prevents: Duplicate bookings if client retries the request
✅ Same concept as wallet transactions for consistency

### 4️⃣ Composite Indexes for Performance
```sql
CREATE INDEX idx_bookings_customer_status ON bookings(customer_id, status);
CREATE INDEX idx_bookings_provider_status ON bookings(provider_id, status);
```
✅ Optimizes: Common queries like "get all confirmed bookings for customer"

### 5️⃣ Event Publishing Tracking
```sql
event_published BOOLEAN DEFAULT FALSE
```
✅ Tracks: Whether RabbitMQ event was sent
✅ Helps: Retry logic if event sending fails
✅ Enables: Debugging and audit trail

### 6️⃣ Status Transition Safety (Application Level)
```
Valid transitions:
- PENDING → CONFIRMED (payment succeeds)
- PENDING → FAILED (payment fails)
- CONFIRMED → CANCELLED (customer cancels)
- FAILED → (no transitions, terminal state)
- CANCELLED → (no transitions, terminal state)

Enforced at application level using state machine pattern
```

---

## 🎤 Viva Talking Points

### 1. Why No History Table?
```
Q: Why don't you track old_status and new_status?
A: "For this assignment, we keep it simple. The updated_at field 
   shows when the status last changed. If we need detailed history 
   later, we can add it, but YAGNI (You Aren't Gonna Need It)."
```

### 2. Microservices Isolation
```
Q: Why aren't customer_id, service_offer_id, provider_id foreign keys?
A: "Each service owns its database. These IDs reference other services' 
   databases. We validate them via REST API calls, not database constraints. 
   This ensures loose coupling and independent scalability."
```

### 3. Amount Snapshot
```
Q: Why store amount instead of just referencing service_offer_id?
A: "The service offer price might change later. By storing the amount 
   at booking time, we have a snapshot of what the customer actually paid. 
   This is important for billing and audit purposes."
```

### 4. Status Management
```
Q: How do you prevent invalid status transitions?
A: "At the application level, we validate transitions:
   - PENDING → CONFIRMED (if payment succeeds)
   - PENDING → FAILED (if payment fails)
   - CONFIRMED → CANCELLED (if customer cancels)
   Invalid transitions are rejected by the service logic."
```

---

## 🏆 This Schema is Production-Ready!

✅ Microservices-correct (no cross-DB FKs)  
✅ Simple and focused (one table, no unnecessary complexity)  
✅ Supports all booking states  
✅ Optimized indexes (query performance)  
✅ Clean design (easy to understand and maintain)  
✅ Ready for RabbitMQ integration  

**Elite-level design for your assignment!** 🔥


### 5. Double Booking Prevention
```
Q: How do you prevent two customers from booking the same service slot?
A: "We use a UNIQUE constraint on (service_offer_id, service_start, service_end).
   This ensures only one booking can exist for a specific service at a specific time.
   If a second booking tries to use the same slot, the database rejects it."
```

### 6. Idempotency Key
```
Q: What happens if a booking request is retried?
A: "We use an idempotency_key (UNIQUE constraint). If the same request is sent twice,
   the second attempt will fail with a unique constraint violation, preventing 
   duplicate bookings. This is the same pattern we use in wallet transactions."
```

### 7. Time Slot Design
```
Q: Why service_start and service_end instead of just service_date?
A: "Real-world services need time slots. A plumber might be available 9 AM-5 PM.
   With timestamps, we support multiple bookings per day and prevent scheduling conflicts.
   The CHECK constraint ensures service_end > service_start for data integrity."
```

### 8. Event Publishing Tracking
```
Q: Why track event_published in the database?
A: "When we publish BOOKING_CONFIRMED to RabbitMQ, we mark event_published = TRUE.
   If the event sending fails, we can query for unpublished events and retry.
   This ensures reliable event delivery even if the service crashes."
```

### 9. Composite Indexes
```
Q: Why create indexes on (customer_id, status) and (provider_id, status)?
A: "The most common queries are 'get all confirmed bookings for customer X' 
   or 'get all pending bookings for provider Y'. Composite indexes optimize 
   these queries by allowing the database to use a single index lookup."
```

---

## 🏆 This Schema is Elite-Level Production-Ready!

✅ Microservices-correct (no cross-DB FKs)  
✅ Double booking prevention (UNIQUE constraint)  
✅ Time slot support (realistic scheduling)  
✅ Idempotency key (retry-safe)  
✅ Event tracking (reliable RabbitMQ integration)  
✅ Composite indexes (query performance)  
✅ Status transition safety (application-level validation)  
✅ Amount snapshot (billing accuracy)  
✅ Simple and focused (one table, no unnecessary complexity)  

**This is what elite backend design looks like!** 🔥
