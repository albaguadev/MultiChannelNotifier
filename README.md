# MultiChannel Notifier

A Spring Boot REST API that dispatches notifications across multiple channels (Email, SMS, WhatsApp), built as a practical demonstration of the **Strategy** and **Factory** design patterns in a clean, extensible architecture.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
- [Validation Rules](#validation-rules)
- [Error Handling](#error-handling)
- [Requirements](#requirements)
- [Running the Application](#running-the-application)
- [Running the Tests](#running-the-tests)
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
        ▼
NotificationStrategyFactory   ← resolves strategy by NotificationType (O(1) map lookup)
        │
        ├──▶ EmailStrategy     ← validates email format, sends via Email
        ├──▶ SmsStrategy       ← validates ES phone number (libphonenumber), sends via SMS
        └──▶ WhatsAppStrategy  ← validates E.164 phone number (libphonenumber), sends via WhatsApp
```

All exceptions are intercepted by `GlobalExceptionHandler` and returned as structured JSON responses.

---

## Design Patterns

**Strategy Pattern**
Each delivery channel implements the `NotificationStrategy` interface, which defines three methods: `send()`, `validate()`, and `getType()`. The service layer is completely decoupled from the concrete implementations.

**Factory Pattern**
`NotificationStrategyFactory` is a Spring-managed component that receives all `NotificationStrategy` beans via constructor injection at startup and stores them in an immutable `Map<NotificationType, NotificationStrategy>`. Strategy resolution is O(1) and requires no conditional logic.

---

## Project Structure

```
src/main/java/com/notifier/
├── MultiChannelNotifierApplication.java   ← entry point
├── controller/
│   └── NotificationController.java        ← POST /api/v1/notifications
├── service/
│   └── NotificationService.java           ← orchestration and pre-validation
├── factory/
│   └── NotificationStrategyFactory.java   ← Spring-managed strategy resolver
├── strategy/
│   ├── NotificationStrategy.java          ← common interface
│   ├── EmailStrategy.java                 ← email delivery + format validation
│   ├── SmsStrategy.java                   ← SMS delivery + ES phone validation
│   └── WhatsAppStrategy.java              ← WhatsApp delivery + E.164 validation
├── dto/
│   └── NotificationRequest.java           ← request payload with Bean Validation
├── model/
│   └── NotificationType.java              ← enum: EMAIL, SMS, WHATSAPP
└── exception/
    ├── GlobalExceptionHandler.java        ← @RestControllerAdvice
    ├── InvalidNotificationException.java  ← domain-level validation failure
    └── ErrorMessage.java                  ← standardized error response (record)
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
| Endpoint not found                | 404         |
| Unexpected server error           | 500         |

---

## Requirements

- Java 21 or higher
- Maven 3.8 or higher
- Git

---

## Running the Application

```bash
mvn spring-boot:run
```

The server starts on port `8081` by default. This can be changed in `src/main/resources/application.properties`.

---

## Running the Tests

```bash
mvn test
```

The test suite uses **JUnit 5** and **Spring REST Docs**. Each test generates documentation snippets under `target/generated-snippets/`, which are then used to build the API documentation HTML.

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
