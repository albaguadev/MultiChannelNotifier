# MultiChannel Notifier

A Spring Boot REST API that dispatches notifications across multiple channels (Email, SMS, WhatsApp), built as a practical demonstration of the **Strategy** and **Factory** design patterns in a clean, extensible architecture.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
  - [Send Notification](#send-notification)
  - [Query Notifications](#query-notifications)
- [Validation Rules](#validation-rules)
- [Error Handling](#error-handling)
- [Requirements](#requirements)
- [Running the Application](#running-the-application)
- [Running the Tests](#running-the-tests)
- [Testing Guide](#testing-guide)
- [Documentation](#documentation)

---

## Overview

The system exposes a single REST endpoint that accepts a notification request and routes it to the appropriate delivery channel based on the `type` field. Each channel has its own validation logic and send behavior, all encapsulated behind a common interface.

The primary design goal is **extensibility without modification**: adding a new channel (e.g. Telegram, Slack) only requires creating a new class — no existing code needs to change.

---

## Architecture

```
POST /api/v1/notifications
        │
        ▼
NotificationController        ← validates request (@Valid)
        │
        ▼
NotificationService           ← orchestrates flow, guards against empty content
        │
        ├──▶ PersistencePort  ← saves notification record (Port & Adapter pattern)
        │    └──▶ JpaPersistenceAdapter ← JPA implementation (H2/PostgreSQL)
        │
        ▼
NotificationStrategyFactory   ← resolves strategy by NotificationType (O(1) map lookup)
        │
        ├──▶ EmailStrategy     ← validates email format, sends via Email
        ├──▶ SmsStrategy       ← validates ES phone number (libphonenumber), sends via SMS
        └──▶ WhatsAppStrategy  ← validates E.164 phone number (libphonenumber), sends via WhatsApp

GET /api/v1/notifications
        │
        ▼
QueryController               ← handles query requests
        │
        ▼
QueryService                  ← dispatches queries with filters
        │
        ▼
PersistencePort               ← retrieves notification records
```

All exceptions are intercepted by `GlobalExceptionHandler` and returned as structured JSON responses.

**Persistence Layer:**
- **Port & Adapter pattern** for technology-agnostic domain layer
- **JPA adapter** with support for H2 (dev/test) and PostgreSQL (prod)
- **Flyway migrations** for database schema versioning

---

## Design Patterns

**Strategy Pattern**
Each delivery channel implements the `NotificationStrategy` interface, which defines three methods: `send()`, `validate()`, and `getType()`. The service layer is completely decoupled from the concrete implementations.

**Factory Pattern**
`NotificationStrategyFactory` is a Spring-managed component that receives all `NotificationStrategy` beans via constructor injection at startup and stores them in an immutable `Map<NotificationType, NotificationStrategy>`. Strategy resolution is O(1) and requires no conditional logic.

**Port & Adapter Pattern (Hexagonal Architecture)**
The domain layer defines a `PersistencePort` interface that abstracts all persistence operations. The infrastructure layer provides a `JpaPersistenceAdapter` implementation. This allows the domain to remain technology-agnostic and makes it easy to swap persistence implementations without changing business logic.

---

## Project Structure

```
src/main/java/com/notifier/
├── MultiChannelNotifierApplication.java   ← entry point
├── controller/
│   ├── NotificationController.java        ← POST /api/v1/notifications
│   └── QueryController.java               ← GET /api/v1/notifications
├── service/
│   ├── NotificationService.java           ← orchestration and pre-validation
│   └── QueryService.java                  ← query dispatch with filters
├── port/
│   └── PersistencePort.java               ← persistence abstraction (hexagonal)
├── infrastructure/
│   └── persistence/
│       └── jpa/
│           ├── JpaPersistenceAdapter.java      ← JPA implementation of PersistencePort
│           ├── NotificationRecordEntity.java   ← JPA entity
│           └── JpaNotificationRecordRepository.java ← Spring Data repository
├── factory/
│   └── NotificationStrategyFactory.java   ← Spring-managed strategy resolver
├── strategy/
│   ├── NotificationStrategy.java          ← common interface
│   ├── EmailStrategy.java                 ← email delivery + format validation
│   ├── SmsStrategy.java                   ← SMS delivery + ES phone validation
│   └── WhatsAppStrategy.java              ← WhatsApp delivery + E.164 validation
├── dto/
│   ├── NotificationRequest.java           ← request payload with Bean Validation
│   └── NotificationRecordResponse.java    ← query response DTO
├── model/
│   ├── NotificationType.java              ← enum: EMAIL, SMS, WHATSAPP
│   ├── NotificationStatus.java            ← enum: SENT, FAILED
│   └── NotificationRecord.java            ← domain model for persistence
└── exception/
    ├── GlobalExceptionHandler.java        ← @RestControllerAdvice
    ├── InvalidNotificationException.java  ← domain-level validation failure
    ├── PersistenceException.java          ← infrastructure failure wrapper
    └── ErrorMessage.java                  ← standardized error response (record)

src/main/resources/
├── application.properties                 ← common configuration
├── application-dev.properties             ← H2 file-based (development)
├── application-test.properties            ← H2 in-memory (tests)
├── application-prod.properties            ← PostgreSQL (production)
└── db/migration/
    └── V1__create_notification_records_table.sql ← Flyway migration
```

---

## API Reference

### Send Notification

```
POST /api/v1/notifications
Content-Type: application/json
```

**Request body:**

| Field       | Type   | Required | Description                                      |
|-------------|--------|----------|--------------------------------------------------|
| `type`      | String | Yes      | Delivery channel: `EMAIL`, `SMS`, or `WHATSAPP`  |
| `recipient` | String | Yes      | Email address or phone number of the recipient   |
| `message`   | String | Yes      | Content of the notification                      |
| `subject`   | String | No       | Subject line — used by the Email channel only    |

**Example — Email:**
```json
{
  "type": "EMAIL",
  "recipient": "user@example.com",
  "message": "Your order has been confirmed.",
  "subject": "Order Confirmation"
}
```

**Example — SMS:**
```json
{
  "type": "SMS",
  "recipient": "+34612345678",
  "message": "Your verification code is 4821."
}
```

**Example — WhatsApp:**
```json
{
  "type": "WHATSAPP",
  "recipient": "+34612345678",
  "message": "Hello! Your appointment is confirmed for tomorrow at 10:00."
}
```

**Success response:**
```
HTTP 200 OK
"Notification processed successfully"
```

---

### Query Notifications

```
GET /api/v1/notifications
```

**Query parameters (all optional):**

| Parameter | Type   | Description                                      | Example                    |
|-----------|--------|--------------------------------------------------|----------------------------|
| `type`    | String | Filter by delivery channel: `EMAIL`, `SMS`, `WHATSAPP` | `?type=EMAIL`       |
| `status`  | String | Filter by status: `SENT`, `FAILED`               | `?status=SENT`             |
| `from`    | String | Filter by timestamp (ISO 8601, inclusive)        | `?from=2026-05-01T00:00:00Z` |
| `to`      | String | Filter by timestamp (ISO 8601, inclusive)        | `?to=2026-05-01T23:59:59Z`   |

**Examples:**

```bash
# Get all notifications
GET /api/v1/notifications

# Filter by type
GET /api/v1/notifications?type=EMAIL

# Filter by status
GET /api/v1/notifications?status=SENT

# Filter by date range
GET /api/v1/notifications?from=2026-05-01T00:00:00Z&to=2026-05-01T23:59:59Z

# Combine filters
GET /api/v1/notifications?type=EMAIL&status=SENT
```

**Success response:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "type": "EMAIL",
    "recipient": "user@example.com",
    "message": "Your order has been confirmed.",
    "subject": "Order Confirmation",
    "status": "SENT",
    "errorMessage": null,
    "timestamp": "2026-05-01T14:30:00Z"
  }
]
```

---

## Validation Rules

Each channel applies its own validation on top of the common Bean Validation constraints:

| Channel   | Recipient format                                      | Library used              |
|-----------|-------------------------------------------------------|---------------------------|
| EMAIL     | Valid email address (RFC compliant)                   | Hibernate Validator        |
| SMS       | Valid phone number for region `ES` (Spain)            | libphonenumber 8.13.31    |
| WHATSAPP  | Valid international phone number in E.164 format (`+`) | libphonenumber 8.13.31  |

---

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "status": 400,
  "timestamp": "2026-04-30T16:00:00",
  "message": "Invalid Email format: not-an-email",
  "description": "uri=/api/v1/notifications"
}
```

| Scenario                          | HTTP Status |
|-----------------------------------|-------------|
| Missing or null required fields   | 400         |
| Invalid enum value for `type`     | 400         |
| Invalid email / phone format      | 400         |
| Unknown JSON keys                 | 400         |
| Persistence failure               | 503         |
| Endpoint not found                | 404         |
| Unexpected server error           | 500         |

---

## Requirements

- Java 21 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (optional, for PostgreSQL)
- Git

---

## Running the Application

### Development (H2 file-based database)

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

The server starts on port `8081`. Data persists in `./data/notifier-dev` between restarts.

**H2 Console:** Available at `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:file:./data/notifier-dev`
- Username: `sa`
- Password: (leave empty)

### Production (PostgreSQL)

1. Start PostgreSQL with Docker Compose:
```bash
docker-compose up -d
```

2. Run the application:
```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=prod"
```

### Available Profiles

| Profile | Database        | Use Case                          |
|---------|-----------------|-----------------------------------|
| `dev`   | H2 file-based   | Local development with persistence |
| `test`  | H2 in-memory    | Automated tests                   |
| `prod`  | PostgreSQL      | Production deployment             |

**Note:** The port can be changed in `src/main/resources/application.properties`.

---

## Running the Tests

```bash
mvn test
```

The test suite uses **JUnit 5**, **Spring REST Docs**, and **jqwik** for property-based testing. Each test generates documentation snippets under `target/generated-snippets/`, which are then used to build the API documentation HTML.

Tests run with the `test` profile (H2 in-memory) and mock the `PersistencePort` for unit tests.

---

## Testing Guide

For detailed manual testing instructions, including:
- Testing with H2 and PostgreSQL
- Verifying persistence between restarts
- Testing query filters
- Verifying correctness properties

See: **[docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md)**

---

## Documentation

The project has two documentation outputs, both generated from the build:

### API Documentation (Spring REST Docs)

Generated from real integration tests — every example is guaranteed to match the actual API behavior.

```bash
mvn prepare-package
```

Output: `target/generated-docs/index.html`

### Javadoc

```bash
mvn javadoc:javadoc
```

Output: `target/site/apidocs/index.html`
