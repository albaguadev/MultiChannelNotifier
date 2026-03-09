# Notification System - Design Patterns Implementation

A Java-based notification system designed to demonstrate the implementation of behavioral and creational design patterns in a decoupled architecture.

## Overview

The primary goal of this project is to manage multiple notification channels (Email, SMS, WhatsApp) while adhering to the Open/Closed Principle. By using a combination of Strategy and Factory patterns, the system can be extended with new delivery methods without modifying existing execution logic.

## Design Patterns

* Strategy Pattern: Encapsulates the specific logic for each notification provider. Each strategy implements a common interface, allowing the context to remain agnostic of the underlying implementation.
* Factory Method: Centralizes object creation. The client code requests a strategy via an Enum, and the factory returns the appropriate instance, further reducing tight coupling.

## Project Structure

The source code is organized following standard Java package conventions:

* com.notifier.strategy: Interface definitions and concrete provider implementations.
* com.notifier.factory: Factory logic and the NotificationType enumeration.
* com.notifier.context: Context class that manages strategy execution.
* com.notifier: Application entry point.

## Key Technical Features

* Open/Closed Principle: Adding a new provider only requires creating a new strategy class and updating the factory mapping.
* Type Safety: Implementation of Java Enums to handle provider selection, eliminating string-based errors.
* Stateless Factory: A static factory implementation using modern Java switch expressions for efficient object retrieval.

## Requirements

* Java 21 or higher.
* Git for version control.

## Usage

1. Clone the repository.
2. Navigate to src/main/java/com/notifier/Main.java.
3. Run the main method to see the factory and strategy integration in action.