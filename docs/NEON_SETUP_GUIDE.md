# Neon PostgreSQL Setup Guide for User Service

## Overview

Neon is a serverless PostgreSQL platform with:
- **Automatic Scaling**: Compute scales based on demand
- **Scale-to-Zero**: Development branches suspend after inactivity (saves costs)
- **Database Branching**: Create isolated copies for testing
- **Point-in-Time Recovery**: Restore to any point in time
- **Connection Pooling**: Built-in support for connection pooling

---

## Step 1: Create Neon Account

1. Go to: https://console.neon.tech
2. Sign up with GitHub, Google, or email
3. Verify email if needed
4. You're ready to create projects

---

## Step 2: Create Neon Project

1. **Click "New Project"** button
2. **Configure Project**:
   - **Project Name**: `marketplace-user-service`
   - **Database Name**: `neondb` (default, can change)
   - **PostgreSQL Version**: 16 (latest recommended)
   - **Region**: Choose closest to your location
     - US: `us-east-1`, `us-west-2`
     - EU: `eu-central-1`, `eu-west-1`
     - Asia: `ap-southeast-1`, `ap-northeast-1`
3. **Click "Create Project"**
4. Wait for project to initialize (usually 30 seconds)

---

## Step 3: Get Connection String

### Method 1: From Neon Console (Easiest)

1. Go to your project in Neon Console
2. Click **"Connection string"** button (top right)
3. Select **"PostgreSQL"** tab
4. Copy the full connection string
5. Format: `postgresql://[user]:[password]@[host]/[database]?sslmode=require`

### Method 2: Using Neon CLI

**Install Neon CLI:**
```bash
npm install -g neonctl
```

**Login to Neon:**
```bash
neonctl auth
# Opens browser for authentication
```

**Get Connection String:**
```bash
# List projects
neonctl projects list

# Get connection string for default branch
neonctl connection-string [project-id]

# Get connection string for specific branch
neonctl connection-string [project-id] --branch dev
```

---

## Step 4: Configure Spring Boot Application

### Update application.yml

```yaml
spring:
  application:
    name: USER-SERVICE
  
  datasource:
    # Replace with your Neon connection string
    url: jdbc:postgresql://[neon-host]:5432/[database]?sslmode=require
    username: [neon-user]
    password: [neon-password]
    driver-class-name: org.postgresql.Driver
    
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Using Environment Variables (Recommended)

**Create `.env` file:**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://[neon-host]:5432/[database]?sslmode=require
SPRING_DATASOURCE_USERNAME=[neon-user]
SPRING_DATASOURCE_PASSWORD=[neon-password]
```

**Reference in application.yml:**
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

**Add to .gitignore:**
```
.env
.env.local
```

---

## Step 5: Create Database Schema

### Option 1: Using Neon Console SQL Editor (Easiest)

1. Go to your Neon project
2. Click **"SQL Editor"** tab
3. Paste the following SQL:

```sql
-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    wallet_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create wallets table
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated BIGINT,
    transaction_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
```

4. Click **"Execute"** button
5. Tables are created!

### Option 2: Automatic with Spring Boot

1. Set in `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

2. Run your Spring Boot application:
```bash
mvn spring-boot:run
```

3. Tables are created automatically on first run
4. **Note**: Only for development! Use migrations for production.

### Option 3: Using Flyway or Liquibase (Production)

For production, use database migration tools:

**Add Flyway dependency to pom.xml:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**Create migration file:** `src/main/resources/db/migration/V1__Initial_Schema.sql`

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    wallet_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated BIGINT,
    transaction_count INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
```

---

## Step 6: Test Connection

### Test from Spring Boot

```bash
mvn spring-boot:run
```

**Look for these logs:**
```
Hibernate: create table users (...)
Hibernate: create table wallets (...)
Started UserServiceApplication in X seconds
```

### Test from Command Line

```bash
# Using psql (if installed)
psql "postgresql://[user]:[password]@[host]/[database]?sslmode=require"

# Then run:
SELECT * FROM users;
SELECT * FROM wallets;
```

### Test from Neon Console

1. Go to your project
2. Click **"SQL Editor"**
3. Run:
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
```

Should show: `users`, `wallets`

---

## Step 7: Create Development Branch (Optional)

For safe testing before production:

### Using Neon Console

1. Go to your project
2. Click **"Branches"** tab
3. Click **"Create branch"**
4. **Name**: `dev`
5. **Parent**: `main`
6. Click **"Create"**
7. Get connection string for dev branch

### Using Neon CLI

```bash
# Create dev branch
neonctl branches create --project-id [project-id] --name dev

# Get connection string for dev branch
neonctl connection-string [project-id] --branch dev
```

### Use Dev Branch for Testing

Update `.env`:
```properties
# For development
SPRING_DATASOURCE_URL=jdbc:postgresql://[dev-host]:5432/[database]?sslmode=require
```

---

## Connection String Format

### Full Format
```
postgresql://[user]:[password]@[host]:[port]/[database]?sslmode=require
```

### Example
```
postgresql://neondb_owner:AbCdEfGhIjKlMnOp@ep-cool-cloud-123456.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
```

### JDBC Format (for Spring Boot)
```
jdbc:postgresql://[host]:5432/[database]?sslmode=require
```

### Components
- **User**: `neondb_owner` (default)
- **Password**: Generated by Neon (shown once)
- **Host**: `ep-[name]-[region].aws.neon.tech`
- **Port**: `5432` (default)
- **Database**: `neondb` (default)
- **SSL Mode**: `require` (always for Neon)

---

## Important Configuration Notes

### 1. SSL Mode
Always use `sslmode=require` for Neon connections:
```yaml
url: jdbc:postgresql://[host]:5432/[database]?sslmode=require
```

### 2. Connection Pooling
Configure HikariCP for optimal performance:
```yaml
datasource:
  hikari:
    maximum-pool-size: 10      # Max connections
    minimum-idle: 2            # Min idle connections
    connection-timeout: 20000  # 20 seconds
    idle-timeout: 300000       # 5 minutes
    max-lifetime: 1200000      # 20 minutes
```

### 3. JPA Configuration
```yaml
jpa:
  hibernate:
    ddl-auto: update           # For development only!
  properties:
    hibernate:
      dialect: org.hibernate.dialect.PostgreSQLDialect
      format_sql: true
      jdbc:
        batch_size: 20
        fetch_size: 50
```

### 4. Logging
Enable SQL logging for debugging:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## Neon Features

### 1. Autoscaling
- Compute automatically scales based on load
- No manual intervention needed
- Scales down during low traffic

### 2. Scale-to-Zero
- Development branches suspend after inactivity
- Saves costs on unused databases
- Automatically resumes when accessed

### 3. Database Branching
- Create isolated copies of your database
- Perfect for testing schema changes
- Branch from any point in time

### 4. Point-in-Time Recovery
- Restore database to any point in time
- Useful for accidental data loss
- Available in Neon console

### 5. Connection Pooling
- Built-in connection pooling support
- Reduces connection overhead
- Improves performance

---

## Troubleshooting

### Error: "SSL connection error"
**Cause**: Missing or incorrect SSL mode
**Solution**:
```yaml
url: jdbc:postgresql://[host]:5432/[database]?sslmode=require
```

### Error: "Too many connections"
**Cause**: Connection pool exhausted
**Solution**: Increase pool size in application.yml
```yaml
hikari:
  maximum-pool-size: 20  # Increase from 10
```

### Error: "Authentication failed"
**Cause**: Wrong username or password
**Solution**:
1. Go to Neon Console
2. Click "Connection string"
3. Copy fresh connection string
4. Verify username and password

### Error: "Database does not exist"
**Cause**: Wrong database name in connection string
**Solution**:
1. Check database name in Neon Console
2. Default is `neondb`
3. Update connection string

### Error: "Connection timeout"
**Cause**: Network or firewall issue
**Solution**:
1. Check internet connection
2. Verify firewall allows outbound HTTPS
3. Try from different network

### Error: "Compute is suspended"
**Cause**: Development branch suspended after inactivity
**Solution**:
1. Go to Neon Console
2. Click on the branch
3. Compute will resume automatically
4. Wait 10-30 seconds

---

## Best Practices

### ✅ Do:
- Use `sslmode=require` for all connections
- Store connection strings in environment variables
- Use `.env` file for local development
- Create dev branches for testing
- Use connection pooling
- Monitor compute usage in Neon Console
- Use descriptive branch names
- Clean up unused branches

### ❌ Don't:
- Commit connection strings to Git
- Use `ddl-auto: create-drop` in production
- Hardcode credentials in code
- Use weak passwords
- Leave unused branches running
- Ignore SSL warnings
- Use `sslmode=disable` in production

---

## Next Steps

1. ✅ Create Neon project
2. ✅ Get connection string
3. ✅ Configure Spring Boot
4. ✅ Create database schema
5. ✅ Test connection
6. → Run User Service
7. → Register with Eureka
8. → Create other microservices

---

## Useful Links

- **Neon Console**: https://console.neon.tech
- **Neon Documentation**: https://neon.tech/docs
- **Neon CLI**: https://neon.tech/docs/reference/neon-cli
- **PostgreSQL Docs**: https://www.postgresql.org/docs/

