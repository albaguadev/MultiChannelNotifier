# Notification System - Design Patterns Implementation

A Java-based notification system designed to demonstrate the implementation of behavioral and creational design patterns in a decoupled architecture.

## Overview

The primary goal of this project is to manage multiple notification channels (Email, SMS, WhatsApp) while adhering to the Open/Closed Principle. By using a combination of Strategy and Factory patterns, the system can be extended with new delivery methods without modifying existing execution logic.

## Design Patterns

* **Strategy Pattern:** Encapsulates the specific logic for each notification provider. Each strategy implements a common interface, allowing the context to remain agnostic of the underlying implementation.
* **Factory Pattern:** Centralizes strategy resolution. All beans implementing `NotificationStrategy` are automatically discovered by Spring and registered in a map at startup, enabling O(1) retrieval by `NotificationType`.

## Project Structure

The source code is organized following standard Java package conventions:

* `com.notifier.strategy` — Interface definitions and concrete provider implementations (Email, SMS, WhatsApp).
* `com.notifier.factory` — Factory logic responsible for strategy resolution.
* `com.notifier.service` — Business logic orchestration between the controller and the strategy layer.
* `com.notifier.controller` — REST API entry point.
* `com.notifier.dto` — Data Transfer Objects with validation constraints.
* `com.notifier.model` — Core enumerations (`NotificationType`).
* `com.notifier.exception` — Centralized exception handling and error response structures.

## Key Technical Features

* **Open/Closed Principle:** Adding a new provider only requires creating a new class implementing `NotificationStrategy`. No existing code needs to be modified.
* **Type Safety:** `NotificationType` enum is used for provider selection, eliminating string-based errors.
* **Spring-managed Factory:** Strategy instances are injected by Spring and stored in an immutable map, ensuring thread-safe and efficient resolution at runtime.
* **Centralized Error Handling:** A `@RestControllerAdvice` component intercepts all exceptions and returns consistent JSON error responses.

## Requirements

* Java 21 or higher
* Maven 3.8 or higher
* Git for version control

## Running the Application

```bash
mvn spring-boot:run
```

The server starts on port `8081` by default (configurable in `application.properties`).

## Running the Tests

```bash
mvn test
```

Tests are written with JUnit 5 and Spring REST Docs. Running them also generates API documentation snippets under `target/generated-snippets/`.

## Documentation

This project uses **Javadoc** for technical documentation and **Spring REST Docs** for API documentation.

To generate the Javadoc report, run:

```bash
mvn javadoc:javadoc
```
