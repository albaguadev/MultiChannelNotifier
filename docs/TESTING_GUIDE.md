# Testing Guide - JPA Persistence

This guide will help you manually test the persistence implementation with JPA, H2, and PostgreSQL.

---

## 📋 Prerequisites

- Java 21 installed
- Maven installed
- Docker and Docker Compose installed (for PostgreSQL testing)
- HTTP client (curl, Postman, or similar)

---

## 🧪 Scenario 1: Testing with H2 (Development)

### 1.1 Start the application with `dev` profile

**Option 1: With explicit profile (recommended for clarity)**
```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

**Option 2: Without profile (uses default configuration = H2 file-based)**
```bash
mvn spring-boot:run
```

**⚠️ IMPORTANT:** Verify in the logs that you're using H2 file-based:
```
Database: jdbc:h2:file:./data/notifier-dev (H2 2.2)
```

If you see `jdbc:h2:mem:` it means you're using H2 in-memory and **data will NOT persist**.

**What to expect:**
- Application starts on port `8081`
- Directory `./data/` is created with the H2 database
- Flyway executes the migration `V1__create_notification_records_table.sql`
- Logs show: `H2 console available at '/h2-console'`

### 1.2 Send notifications (create records)

**Send EMAIL notification:**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Test message","subject":"Test subject"}'
```

**Send SMS notification:**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"SMS","recipient":"+34600123456","message":"Test SMS message"}'
```

**Send WHATSAPP notification:**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"WHATSAPP","recipient":"+34600123456","message":"Test WhatsApp message"}'
```

**What to expect:**
- HTTP 200 OK response
- Logs show: `Executing [EMAIL/SMS/WHATSAPP] Delivery to: ...`
- Records are saved in the H2 database

### 1.3 Query saved notifications

**Get all notifications:**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method GET
```

**Filter by type (EMAIL):**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?type=EMAIL" -Method GET
```

**Filter by status (SENT):**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?status=SENT" -Method GET
```

**Filter by date range:**
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?from=2026-05-01T00:00:00Z&to=2026-05-01T23:59:59Z" -Method GET
```

**What to expect:**
- HTTP 200 OK response with JSON array of notifications
- Each notification includes: `id`, `type`, `recipient`, `message`, `subject`, `status`, `errorMessage`, `timestamp`
- Notifications are ordered by timestamp descending (most recent first)

### 1.4 Inspect the H2 database

1. Open browser at: `http://localhost:8081/h2-console`
2. Configure the connection:
   - **JDBC URL:** `jdbc:h2:file:./data/notifier-dev`
   - **User Name:** `sa`
   - **Password:** (leave empty)
3. Click "Connect"
4. Execute SQL queries:

```sql
-- View all notifications
SELECT * FROM notification_records ORDER BY timestamp DESC;

-- Count notifications by type
SELECT type, COUNT(*) as total FROM notification_records GROUP BY type;

-- View sent notifications
SELECT * FROM notification_records WHERE status = 'SENT';

-- View table structure
SHOW COLUMNS FROM notification_records;

-- View created indexes
SHOW INDEXES FROM notification_records;
```

**What to expect:**
- Table `notification_records` exists with all columns
- Indexes `idx_notification_type`, `idx_notification_status`, `idx_notification_timestamp` are created
- Table `flyway_schema_history` shows V1 migration applied
- Data matches the sent notifications

### 1.5 Verify persistence between restarts

1. Stop the application (Ctrl+C)
2. Start again: `mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"`
3. Query notifications: 
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method GET
```

**What to expect:**
- Previously sent notifications are still available
- Migration is not executed again (Flyway detects it's already applied)

---

## 🐘 Scenario 2: Testing with PostgreSQL (Production)

### 2.1 Start PostgreSQL with Docker Compose

```bash
docker-compose up -d
```

**What to expect:**
- PostgreSQL container starts on port `5432`
- Database `notifier_db` created
- User `notifier_user` with password `notifier_pass`

**Verify PostgreSQL is running:**
```bash
docker-compose ps
```

### 2.2 Start the application with `prod` profile

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=prod"
```

**What to expect:**
- Application starts on port `8081`
- Connects to PostgreSQL at `localhost:5432`
- Flyway executes the migration on PostgreSQL
- Logs show: `Database: jdbc:postgresql://localhost:5432/notifier_db (PostgreSQL ...)`

### 2.3 Send and query notifications

Repeat the same PowerShell commands from **Scenario 1.2** and **1.3**.

**What to expect:**
- Same behavior as with H2
- Data is saved in PostgreSQL

### 2.4 Inspect the PostgreSQL database

**Option A: Using psql from Docker**
```bash
docker exec -it multichannel-notifier-postgres psql -U notifier_user -d notifier_db
```

**Option B: Using external PostgreSQL client**
- Host: `localhost`
- Port: `5432`
- Database: `notifier_db`
- User: `notifier_user`
- Password: `notifier_pass`

**SQL queries:**
```sql
-- View all notifications
SELECT * FROM notification_records ORDER BY timestamp DESC;

-- View table structure
\d notification_records

-- View indexes
\di

-- View Flyway migration history
SELECT * FROM flyway_schema_history;
```

**What to expect:**
- Same structure as H2
- Data persisted correctly
- Indexes created

### 2.5 Stop PostgreSQL

```bash
docker-compose down
```

To also remove data:
```bash
docker-compose down -v
```

---

## 🧪 Scenario 3: Automated Tests

### 3.1 Run unit tests

```bash
mvn test
```

**What to expect:**
- 13 tests pass successfully
- Uses H2 in-memory (`test` profile)
- Flyway executes migrations in memory
- `PersistencePort` is mocked in controller tests

### 3.2 Verify test coverage

```bash
mvn clean test
```

**Included tests:**
- ✅ Send notification without optional fields
- ✅ Send notification with optional fields
- ✅ Validation of required fields (type, recipient, message)
- ✅ Email format validation
- ✅ Handling JSON with incorrect keys
- ✅ Handling endpoints not found (404)

---

## 🔍 Scenario 4: Correctness Properties Verification

These are the properties defined in the spec that you should verify manually:

### P1: Save idempotence
```powershell
# Send the same notification twice
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Test"}'

# Query all notifications
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method GET
```
**Verify:** Each send generates a unique record (different IDs and timestamps)

### P2: Data preservation
```powershell
# Send notification with subject
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Test","subject":"Subject Test"}'

# Query and verify subject was saved
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method GET
```
**Verify:** The `subject` field appears in the response with the correct value

### P3: Chronological order
```powershell
# Send 3 notifications with delay
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test1@example.com","message":"First"}'

Start-Sleep -Seconds 1

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test2@example.com","message":"Second"}'

Start-Sleep -Seconds 1

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test3@example.com","message":"Third"}'

# Query all
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method GET
```
**Verify:** Notifications appear ordered from most recent to oldest (Third, Second, First)

### P4: Filter by type
```powershell
# Send notifications of different types
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Email test"}'

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"SMS","recipient":"+34600123456","message":"SMS test"}'

# Filter only EMAIL
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?type=EMAIL" -Method GET
```
**Verify:** Only EMAIL type notifications appear

### P5: Filter by status
```powershell
# All notifications should have status=SENT (successful simulation)
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?status=SENT" -Method GET
```
**Verify:** All notifications have `status: "SENT"`

### P6: Filter by date range
```powershell
# Get current timestamp in ISO 8601 format
# Example: 2026-05-01T14:00:00Z

# Send notification
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Test"}'

# Filter by range that includes the notification
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications?from=2026-05-01T00:00:00Z&to=2026-05-01T23:59:59Z" -Method GET
```
**Verify:** The notification appears in the result

### P7: Infrastructure failure isolation
```powershell
# Stop the database while the app is running
docker-compose down

# Try to send notification
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/notifications" -Method POST -ContentType "application/json" -Body '{"type":"EMAIL","recipient":"test@example.com","message":"Test"}'
```
**Verify:** 
- HTTP 503 Service Unavailable response
- Error message indicates persistence problem
- Application doesn't crash

---

## 📊 Verification Checklist

### Basic Functionality
- [ ] Application starts with `dev` profile (H2)
- [ ] Application starts with `prod` profile (PostgreSQL)
- [ ] Can send EMAIL notifications
- [ ] Can send SMS notifications
- [ ] Can send WHATSAPP notifications
- [ ] Can query all notifications
- [ ] Can filter notifications by type
- [ ] Can filter notifications by status
- [ ] Can filter notifications by date range

### Persistence
- [ ] Data persists between restarts (H2 file-based)
- [ ] Data persists in PostgreSQL
- [ ] Flyway executes migrations correctly
- [ ] H2 Console works correctly
- [ ] Indexes are created correctly

### Validation
- [ ] Required fields are validated (type, recipient, message)
- [ ] Email format is validated
- [ ] Optional fields work (subject can be null)
- [ ] Errors return HTTP 400 with descriptive message

### Tests
- [ ] All unit tests pass (13/13)
- [ ] Tests use H2 in-memory
- [ ] Tests mock PersistencePort correctly

### Correctness Properties
- [ ] P1: Save idempotence
- [ ] P2: Data preservation
- [ ] P3: Chronological order
- [ ] P4: Filter by type
- [ ] P5: Filter by status
- [ ] P6: Filter by date range
- [ ] P7: Failure isolation

---

## 🐛 Troubleshooting

### Error: "Table not found"
**Cause:** Flyway didn't execute the migration
**Solution:** 
```bash
# Verify migration file exists
ls src/main/resources/db/migration/

# Restart the application
mvn clean spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

### Error: "Connection refused" (PostgreSQL)
**Cause:** PostgreSQL is not running
**Solution:**
```bash
docker-compose up -d
docker-compose ps  # Verify it's running
```

### Error: "Port 8081 already in use"
**Cause:** Another instance of the application is running
**Solution:**
```powershell
# PowerShell - Find process using port 8081
Get-NetTCPConnection -LocalPort 8081 | Select-Object -Property OwningProcess

# Kill the process (replace <PID> with the process number)
Stop-Process -Id <PID> -Force
```

### Data doesn't persist in H2
**Cause:** Using H2 in-memory instead of file-based
**Solution:** 
1. Verify in the startup logs that you see:
   ```
   Database: jdbc:h2:file:./data/notifier-dev (H2 2.2)
   ```
2. If you see `jdbc:h2:mem:`, you're using in-memory. Make sure to:
   - Start with dev profile: `mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"`
   - Or verify that `application.properties` has the correct datasource configuration
3. Restart the application and verify the logs again

---

## 📝 Additional Notes

- **Available profiles:**
  - `dev`: H2 file-based in `./data/notifier-dev`
  - `test`: H2 in-memory (used by tests)
  - `prod`: PostgreSQL at `localhost:5432`

- **H2 data directory:** `./data/` (ignored in git)

- **Application port:** `8081`

- **H2 Console:** `http://localhost:8081/h2-console` (only in `dev` profile)

- **PostgreSQL Docker:** 
  - Port: `5432`
  - Database: `notifier_db`
  - User: `notifier_user`
  - Password: `notifier_pass`

---

Ready to test! 🚀
