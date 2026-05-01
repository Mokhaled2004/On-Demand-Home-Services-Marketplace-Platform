# Notification Database Schema - FINAL VERSION

## 📋 Complete SQL Schema

```sql
-- ============================================
-- NOTIFICATION DATABASE SCHEMA
-- ============================================

-- ============================================
-- ENUMS (Type Safety)
-- ============================================

DROP TYPE IF EXISTS notification_status_enum CASCADE;
DROP TYPE IF EXISTS notification_type_enum CASCADE;

CREATE TYPE notification_status_enum AS ENUM ('SENT', 'FAILED', 'PENDING');
CREATE TYPE notification_type_enum AS ENUM ('BOOKING_CONFIRMED', 'BOOKING_FAILED', 'BOOKING_CANCELLED');

-- ============================================
-- TABLE 1: NOTIFICATIONS
-- ============================================
-- Stores all notifications sent to users
-- Tracks which booking triggered the notification
-- Manages read/unread status

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,  -- References users.id in USER-SERVICE (NOT FK)
    booking_id BIGINT,  -- References bookings.id in BOOKING-SERVICE (NOT FK)
    type notification_type_enum NOT NULL,  -- BOOKING_CONFIRMED, BOOKING_FAILED, BOOKING_CANCELLED
    title VARCHAR(255) NOT NULL,  -- Short title (e.g., "Booking Confirmed")
    message TEXT NOT NULL,  -- Full message text
    read_status BOOLEAN DEFAULT FALSE,  -- Whether user has read it
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP  -- When user read it
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_booking ON notifications(booking_id);
CREATE INDEX idx_notifications_read_status ON notifications(read_status);
CREATE INDEX idx_notifications_created ON notifications(created_at);
-- Composite index for common query: get unread notifications for user
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_status);

-- ============================================
-- TABLE 2: NOTIFICATION_LOGS
-- ============================================
-- Tracks delivery status of each notification
-- Helps with retry logic if sending fails
-- Provides audit trail for notification delivery

CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,  -- References notifications.id (FK)
    status notification_status_enum NOT NULL,  -- SENT, FAILED, PENDING
    sent_at TIMESTAMP,  -- When it was sent
    error_message VARCHAR(255),  -- Error details if it failed
    retry_count INTEGER DEFAULT 0,  -- How many times we tried to send
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_logs_notification ON notification_logs(notification_id);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_created ON notification_logs(created_at);

-- ============================================
-- SUMMARY
-- ============================================
/*
✅ NOTIFICATIONS: Stores all notifications with booking context
✅ user_id: NOT a FK (links to USER-SERVICE via REST API)
✅ booking_id: NOT a FK (links to BOOKING-SERVICE via REST API)
✅ type: BOOKING_CONFIRMED, BOOKING_FAILED, BOOKING_CANCELLED
✅ read_status: Track if user has read the notification
✅ read_at: Timestamp when user read it
✅ NOTIFICATION_LOGS: Tracks delivery status and retries
✅ status: SENT, FAILED, PENDING
✅ retry_count: Track retry attempts
✅ Comprehensive indexes for query performance
*/
```

---

## 📊 Table Details

### NOTIFICATIONS

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| user_id | BIGINT | NOT NULL | User ID (from USER-SERVICE, NOT FK) |
| booking_id | BIGINT | NULL | Booking ID (from BOOKING-SERVICE, NOT FK) |
| type | notification_type_enum | NOT NULL | BOOKING_CONFIRMED, BOOKING_FAILED, BOOKING_CANCELLED |
| title | VARCHAR(255) | NOT NULL | Short notification title |
| message | TEXT | NOT NULL | Full notification message |
| read_status | BOOLEAN | DEFAULT FALSE | Whether user has read it |
| created_at | TIMESTAMP | DEFAULT NOW() | When notification was created |
| read_at | TIMESTAMP | NULL | When user read it |

**Indexes:**
- idx_notifications_user (user_id)
- idx_notifications_booking (booking_id) - Optimize "get notifications for booking X"
- idx_notifications_read_status (read_status)
- idx_notifications_created (created_at)
- idx_notifications_user_unread (user_id, read_status) - Composite index

---

### NOTIFICATION_LOGS

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| notification_id | BIGINT | NOT NULL, FK | Links to NOTIFICATIONS |
| status | notification_status_enum | NOT NULL | SENT, FAILED, PENDING |
| sent_at | TIMESTAMP | NULL | When notification was sent |
| error_message | VARCHAR(255) | NULL | Error details if failed |
| retry_count | INTEGER | DEFAULT 0 | Number of retry attempts |
| created_at | TIMESTAMP | DEFAULT NOW() | When log was created |

**Indexes:**
- idx_notification_logs_notification (notification_id)
- idx_notification_logs_status (status)
- idx_notification_logs_created (created_at)

**Constraints:**
- FOREIGN KEY (notification_id) → NOTIFICATIONS(id) ON DELETE CASCADE

---

## 🔄 How It Works - Notification Flow

### Step 1: Booking Service Publishes Event to RabbitMQ
```
Booking confirmed → Publish BOOKING_CONFIRMED event
{
  "booking_id": 123,
  "customer_id": 10,
  "provider_id": 5,
  "amount": 80.00
}
```

### Step 2: Notification Service Consumes Event
```
Receives BOOKING_CONFIRMED event
Creates 2 notifications:
1. For customer: "Your booking is confirmed!"
2. For provider: "You have a new booking!"
```

### Step 3: Create Notifications
```sql
INSERT INTO notifications 
(user_id, booking_id, type, title, message, read_status)
VALUES (10, 123, 'BOOKING_CONFIRMED', 'Booking Confirmed', 'Your booking for plumbing service is confirmed!', FALSE);

INSERT INTO notifications 
(user_id, booking_id, type, title, message, read_status)
VALUES (5, 123, 'BOOKING_CONFIRMED', 'New Booking', 'You have a new booking from customer!', FALSE);
```

### Step 4: Create Notification Logs (Track Delivery)
```sql
INSERT INTO notification_logs 
(notification_id, status, sent_at, retry_count)
VALUES (1, 'SENT', NOW(), 0);

INSERT INTO notification_logs 
(notification_id, status, sent_at, retry_count)
VALUES (2, 'SENT', NOW(), 0);
```

### Step 5: Customer Views Notifications
```sql
-- Get all unread notifications for customer
SELECT * FROM notifications 
WHERE user_id = 10 AND read_status = FALSE 
ORDER BY created_at DESC;
```

### Step 6: Customer Reads Notification
```sql
UPDATE notifications 
SET read_status = TRUE, read_at = NOW() 
WHERE id = 1;
```

### Step 7: Check Delivery Status
```sql
-- Check if notification was sent successfully
SELECT * FROM notification_logs 
WHERE notification_id = 1;
```

---

## 📋 Common Queries

### Get All Notifications for a User
```sql
SELECT * FROM notifications 
WHERE user_id = 10 
ORDER BY created_at DESC;
```

### Get Unread Notifications for a User
```sql
SELECT * FROM notifications 
WHERE user_id = 10 AND read_status = FALSE 
ORDER BY created_at DESC;
```

### Mark Notification as Read
```sql
UPDATE notifications 
SET read_status = TRUE, read_at = NOW() 
WHERE id = 1;
```

### Get Notifications for a Specific Booking
```sql
SELECT * FROM notifications 
WHERE booking_id = 123 
ORDER BY created_at DESC;
```

### Check Failed Notifications (For Retry)
```sql
SELECT n.*, nl.error_message, nl.retry_count
FROM notifications n
JOIN notification_logs nl ON n.id = nl.notification_id
WHERE nl.status = 'FAILED' AND nl.retry_count < 3
ORDER BY nl.created_at ASC;
```

---

## 💡 Key Design Decisions

| Decision | Reason |
|----------|--------|
| user_id NOT FK | Different database (User Service) - use REST API |
| booking_id NOT FK | Different database (Booking Service) - use REST API |
| booking_id nullable | Some notifications might not be booking-related |
| read_status BOOLEAN | Simple way to track read/unread |
| read_at TIMESTAMP | Track when user read the notification |
| NOTIFICATION_LOGS table | Track delivery status and retry attempts |
| retry_count field | Support retry logic for failed notifications |
| Composite index (user_id, read_status) | Optimize "get unread notifications" query |
| ON DELETE CASCADE | If notification deleted, logs are also deleted |

---

## 🎓 Microservices Integration

**Notification DB** (This Database)
- Stores notifications and delivery logs
- Independent database

**Booking Service** (Different Database)
- Publishes BOOKING_CONFIRMED/FAILED events to RabbitMQ
- Notification Service consumes these events

**User Service** (Different Database)
- Called via REST API to get user details if needed

**How They Connect:**
```
1. Booking Service creates booking
2. Booking succeeds → Publish BOOKING_CONFIRMED to RabbitMQ
3. Notification Service consumes event
4. Creates notifications for customer and provider
5. Stores in NOTIFICATIONS table
6. Tracks delivery in NOTIFICATION_LOGS table
7. Customer calls: GET /notifications/{userId}
8. Returns notifications from NOTIFICATIONS table
9. Customer calls: PUT /notifications/{notificationId}/read
10. Updates read_status and read_at
```

---

## ✅ Supports All Requirements

✅ Consume events from RabbitMQ (BOOKING_CONFIRMED, BOOKING_FAILED)  
✅ Send notifications via REST JSON endpoints  
✅ Store notification logs in DB  
✅ GET /notifications/{userId} - Get all notifications  
✅ GET /notifications/{userId}/unread - Get unread notifications  
✅ PUT /notifications/{notificationId}/read - Mark as read  
✅ Track delivery status (SENT, FAILED, PENDING)  
✅ Support retry logic (retry_count)  
✅ Link notifications to bookings (booking_id)  
✅ Audit trail (created_at, sent_at, read_at)  

---

## 🔥 Elite-Level Improvements Applied

### 1️⃣ Type Safety with ENUM
```sql
CREATE TYPE notification_type_enum AS ENUM (
    'BOOKING_CONFIRMED',
    'BOOKING_FAILED',
    'BOOKING_CANCELLED'
);
```
✅ Prevents: Typos, inconsistent values, messy analytics
✅ Ensures: Only valid notification types can be stored

### 2️⃣ Booking Index for Performance
```sql
CREATE INDEX idx_notifications_booking ON notifications(booking_id);
```
✅ Optimizes: Query "get all notifications for booking X"
✅ Prevents: Slow queries as data grows

### 3️⃣ Composite Index for Common Query
```sql
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_status);
```
✅ Optimizes: "Get unread notifications for user X"
✅ Single index lookup instead of multiple

---

## 🎤 Viva Talking Points

### 1. Why booking_id?
```
Q: Why store booking_id in notifications?
A: "It links the notification to the specific booking that triggered it.
   When a user clicks a notification, we can fetch the booking details.
   It also helps with debugging and tracing which booking caused issues."
```

### 2. Why NOTIFICATION_LOGS?
```
Q: Why have a separate logs table?
A: "It tracks delivery status and retry attempts. If sending fails,
   we can query failed notifications and retry them.
   It provides an audit trail of what happened with each notification."
```

### 3. Microservices Isolation
```
Q: Why aren't user_id and booking_id foreign keys?
A: "They reference different service databases. We validate them via
   REST API calls, not database constraints. This ensures loose coupling
   and independent scalability."
```

### 4. Read Status Tracking
```
Q: Why track read_status and read_at?
A: "read_status lets us show unread notification count to users.
   read_at tracks when they actually read it, useful for analytics
   and understanding user engagement."
```

### 5. Retry Logic
```
Q: How do you handle failed notifications?
A: "We track retry_count in NOTIFICATION_LOGS. If status = FAILED,
   we can query for failed notifications with retry_count < 3
   and retry sending them. This ensures reliable delivery."
```

---

## 🏆 This Schema is Production-Ready!

✅ Microservices-correct (no cross-DB FKs)  
✅ Booking context tracking (booking_id)  
✅ Read/unread status management  
✅ Delivery tracking (NOTIFICATION_LOGS)  
✅ Retry support (retry_count)  
✅ Optimized indexes (query performance)  
✅ Simple and focused (clean design)  
✅ Ready for RabbitMQ integration  

**Elite-level design for your assignment!** 🔥
