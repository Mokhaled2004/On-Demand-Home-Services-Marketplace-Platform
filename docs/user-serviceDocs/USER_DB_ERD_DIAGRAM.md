# User Service Database - ERD Diagram

## 📊 Entity Relationship Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                          USER SERVICE DATABASE ERD                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘


                              ┌──────────────────────┐
                              │      USERS           │
                              ├──────────────────────┤
                              │ id (PK)              │
                              │ username (UNIQUE)    │
                              │ email (UNIQUE)       │
                              │ password_hash        │
                              │ role (ENUM)          │
                              │ profession_type      │
                              │ created_at           │
                              │ updated_at           │
                              └──────────────────────┘
                                      │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    │ (1-to-1)          │ (1-to-many)       │
                    │                   │                   │
                    ▼                   ▼                   ▼
        ┌──────────────────────┐  ┌──────────────────────┐
        │     WALLETS          │  │ COMPENSATION_LOG     │
        ├──────────────────────┤  ├──────────────────────┤
        │ id (PK)              │  │ id (PK)              │
        │ user_id (FK)         │  │ booking_id           │
        │ balance              │  │ user_id (FK)         │
        │ currency             │  │ transaction_id (FK)  │
        │ version              │  │ action (ENUM)        │
        │ created_at           │  │ amount               │
        │ updated_at           │  │ status (ENUM)        │
        │ CHECK (balance >= 0) │  │ reason               │
        │ UNIQUE(user_id)      │  │ created_at           │
        └──────────────────────┘  │ UNIQUE(booking_id,   │
                    │             │        action)       │
                    │ (1-to-many) └──────────────────────┘
                    │
                    ▼
        ┌──────────────────────────────────┐
        │   WALLET_TRANSACTIONS            │
        ├──────────────────────────────────┤
        │ id (PK)                          │
        │ wallet_id (FK)                   │
        │ transaction_type (ENUM)          │
        │ amount                           │
        │ description                      │
        │ reference_id (booking_id)        │
        │ status (ENUM)                    │
        │ idempotency_key (UNIQUE)         │
        │ created_at                       │
        │ metadata (JSONB)                 │
        │ CHECK (amount > 0)               │
        └──────────────────────────────────┘
```

---

## 🔗 Relationships Summary

| From | To | Type | Cardinality | Notes |
|------|-----|------|-------------|-------|
| USERS | WALLETS | Foreign Key | 1-to-1 | One wallet per user |
| USERS | COMPENSATION_LOG | Foreign Key | 1-to-many | User can have many compensation entries |
| WALLETS | WALLET_TRANSACTIONS | Foreign Key | 1-to-many | Wallet has many transactions |
| WALLET_TRANSACTIONS | COMPENSATION_LOG | Foreign Key | 1-to-many | Transaction can be referenced in compensation |

---

## 📋 Table Details for Miro

### Table 1: USERS
**Purpose**: Store user identity and role information

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| username | VARCHAR(255) | UNIQUE, NOT NULL | Login identifier |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Contact email |
| password_hash | VARCHAR(255) | NOT NULL | Hashed password |
| role | user_role_enum | NOT NULL | CUSTOMER, PROVIDER, ADMIN |
| profession_type | VARCHAR(100) | NULL | Only for PROVIDER role |
| created_at | TIMESTAMP | DEFAULT NOW() | Account creation time |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update time |

**Indexes**:
- idx_users_username (username)
- idx_users_email (email)
- idx_users_role (role)

---

### Table 2: WALLETS
**Purpose**: Store current balance for each user

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| user_id | BIGINT | UNIQUE, NOT NULL, FK | Links to USERS |
| balance | DECIMAL(15,2) | DEFAULT 0.00, CHECK >= 0 | Current balance |
| currency | VARCHAR(3) | DEFAULT 'USD' | Currency code |
| version | BIGINT | DEFAULT 0 | Optimistic locking |
| created_at | TIMESTAMP | DEFAULT NOW() | Wallet creation time |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update time |

**Indexes**:
- idx_wallets_user (user_id)

**Constraints**:
- CHECK (balance >= 0) - Prevent negative balance

---

### Table 3: WALLET_TRANSACTIONS
**Purpose**: Audit trail of all wallet movements

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| wallet_id | BIGINT | NOT NULL, FK | Links to WALLETS |
| transaction_type | transaction_type_enum | NOT NULL | DEBIT, CREDIT, REFUND |
| amount | DECIMAL(15,2) | NOT NULL, CHECK > 0 | Transaction amount |
| description | VARCHAR(255) | NULL | Human-readable description |
| reference_id | VARCHAR(100) | NULL | booking_id or order_id |
| status | transaction_status_enum | NOT NULL | PENDING, SUCCESS, FAILED |
| idempotency_key | VARCHAR(100) | UNIQUE | Prevents duplicate transactions |
| created_at | TIMESTAMP | DEFAULT NOW() | Transaction time |
| metadata | JSONB | NULL | Extra data (JSON) |

**Indexes**:
- idx_transactions_wallet (wallet_id)
- idx_transactions_status (status)
- idx_transactions_reference (reference_id)
- idx_transactions_idempotency (idempotency_key)
- idx_transactions_created (created_at)

**Constraints**:
- CHECK (amount > 0) - Ensure positive amounts

---

### Table 4: COMPENSATION_LOG
**Purpose**: Track deductions and refunds for rollback/audit

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment |
| booking_id | VARCHAR(100) | NOT NULL | External booking ID |
| user_id | BIGINT | NOT NULL, FK | Links to USERS |
| transaction_id | BIGINT | NULL, FK | Links to WALLET_TRANSACTIONS |
| action | compensation_action_enum | NOT NULL | DEDUCT, REFUND |
| amount | DECIMAL(15,2) | NOT NULL | Compensation amount |
| status | compensation_status_enum | NOT NULL | SUCCESS, FAILED, PENDING |
| reason | VARCHAR(255) | NULL | Why compensation occurred |
| created_at | TIMESTAMP | DEFAULT NOW() | Log creation time |

**Indexes**:
- idx_compensation_booking (booking_id)
- idx_compensation_user (user_id)
- idx_compensation_status (status)
- idx_compensation_created (created_at)

**Constraints**:
- UNIQUE (booking_id, action) - Prevent double deduction/refund

---

## 🎨 How to Draw in Miro

### Step 1: Create 4 Rectangles (Tables)
1. USERS
2. WALLETS
3. WALLET_TRANSACTIONS
4. COMPENSATION_LOG

### Step 2: Add Columns to Each Rectangle
For each table, list all columns with their types

### Step 3: Draw Relationships (Connectors)
- USERS → WALLETS (1-to-1, solid line)
- USERS → COMPENSATION_LOG (1-to-many, solid line)
- WALLETS → WALLET_TRANSACTIONS (1-to-many, solid line)
- WALLET_TRANSACTIONS → COMPENSATION_LOG (1-to-many, dashed line)

### Step 4: Add Labels
- Label each connector with cardinality (1-to-1, 1-to-many)
- Add FK labels on the foreign key columns

### Step 5: Add Color Coding (Optional)
- 🟦 Blue: Primary Keys (id)
- 🟩 Green: Foreign Keys (user_id, wallet_id, transaction_id)
- 🟨 Yellow: Constraints (UNIQUE, CHECK)
- 🟪 Purple: Indexes

---

## 📐 Miro Board Layout Suggestion

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  [USERS]                                                    │
│  ├─ id (PK)                                                 │
│  ├─ username (UNIQUE)                                       │
│  ├─ email (UNIQUE)                                          │
│  ├─ password_hash                                           │
│  ├─ role (ENUM)                                             │
│  ├─ profession_type                                         │
│  └─ timestamps                                              │
│       │                                                     │
│       ├──────────────────────────────────────────────────┐  │
│       │                                                  │  │
│       ▼ (1-to-1)                                    (1-to-many)
│  [WALLETS]                                      [COMPENSATION_LOG]
│  ├─ id (PK)                                     ├─ id (PK)
│  ├─ user_id (FK)                                ├─ booking_id
│  ├─ balance (CHECK >= 0)                        ├─ user_id (FK)
│  ├─ currency                                    ├─ transaction_id (FK)
│  ├─ version (optimistic lock)                   ├─ action (ENUM)
│  └─ timestamps                                  ├─ amount
│       │                                         ├─ status (ENUM)
│       │ (1-to-many)                             ├─ reason
│       ▼                                         └─ created_at
│  [WALLET_TRANSACTIONS]
│  ├─ id (PK)
│  ├─ wallet_id (FK)
│  ├─ transaction_type (ENUM)
│  ├─ amount (CHECK > 0)
│  ├─ description
│  ├─ reference_id (booking_id)
│  ├─ status (ENUM)
│  ├─ idempotency_key (UNIQUE)
│  ├─ metadata (JSONB)
│  └─ created_at
│
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Points to Highlight in Miro

1. **Optimistic Locking**: version column in WALLETS
2. **Idempotency**: idempotency_key in WALLET_TRANSACTIONS
3. **Data Integrity**: CHECK constraints on balance and amount
4. **Audit Trail**: WALLET_TRANSACTIONS + COMPENSATION_LOG
5. **Rollback Capability**: COMPENSATION_LOG tracks deductions and refunds
6. **Type Safety**: ENUMs for role, transaction_type, status, action

---

## 📝 Notes for Your Viva

- **Concurrency**: "We use optimistic locking (version column) to handle concurrent requests"
- **Idempotency**: "Idempotency key prevents duplicate charges if requests are retried"
- **Audit**: "COMPENSATION_LOG provides complete audit trail for admin visibility"
- **Rollback**: "If booking fails, we can easily refund by querying COMPENSATION_LOG"
- **Constraints**: "CHECK constraints ensure data integrity at database level"
