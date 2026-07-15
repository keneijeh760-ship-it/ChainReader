# VectorOS Fleet - Implementation Plan

> This document serves as the implementation guide for the Fleet Management Service of the VectorOS platform. All implementation decisions should follow this specification.

---

# Project Overview

## What is VectorOS?

VectorOS is a modular robotics software platform designed to manage autonomous warehouse robots.

The platform consists of independent microservices:

- Fleet Management Service (this repository)
- Robot Service
- Vision Service
- Dashboard
- Platform Integration

The Fleet service is the brain of the system.

It is responsible for:

- Robot registration
- Fleet monitoring
- Task management
- Telemetry ingestion
- Alert management
- Robot assignment
- Communication with other services

The Fleet service DOES NOT control robot movement directly.

Instead, it coordinates robots through APIs and message queues.

---

# Tech Stack

Language
- Java 21

Framework
- Spring Boot

Database
- PostgreSQL

ORM
- Spring Data JPA

Migration
- Flyway

Messaging
- MQTT

API
- REST

Documentation
- OpenAPI (Swagger)

Build Tool
- Maven

Deployment
- Docker

Authentication
- JWT (Future Sprint)

Testing
- JUnit
- Testcontainers (Future Sprint)

---

# Engineering Principles

The implementation should follow these principles:

- Clean Architecture
- SOLID Principles
- Single Responsibility Principle
- Dependency Injection
- Constructor Injection
- DTO Pattern
- Repository Pattern
- Layered Architecture

Business logic must never exist inside controllers.

Controllers should only:

- Validate requests
- Call services
- Return responses

All business logic belongs inside the Service layer.

Repositories should only communicate with the database.

---

# High-Level Architecture

Client

↓

REST API

↓

Controllers

↓

Services

↓

Repositories

↓

PostgreSQL

---

Other services communicate with Fleet through:

- MQTT
- REST APIs

---

# Folder Structure

src/main/java/com/vectoros/fleet

config/

controller/

dto/

entity/

exception/

mapper/

mqtt/

repository/

security/

service/

websocket/

resources/

db/migration/

---

# Initial Domain Models

## Robot

Represents an autonomous robot connected to the fleet.

Properties

- id
- name
- status
- batteryLevel
- currentX
- currentY
- lastSeen
- createdAt
- updatedAt

---

## Task

Represents a warehouse task assigned to a robot.

Properties

- id
- pickupLocation
- dropoffLocation
- priority
- status
- assignedRobot
- createdAt
- completedAt

---

## Telemetry

Represents periodic data sent by a robot.

Properties

- id
- robotId
- battery
- speed
- x
- y
- timestamp

---

## Alert

Represents warnings or failures.

Properties

- id
- robotId
- severity
- type
- message
- timestamp

---

# REST API

## Robot

POST /robots

Registers a new robot.

---

GET /robots

Returns every robot.

---

GET /robots/{id}

Returns one robot.

---

PATCH /robots/{id}

Updates robot information.

---

## Tasks

POST /tasks

Creates a warehouse task.

---

GET /tasks

Returns every task.

---

GET /tasks/{id}

Returns one task.

---

PATCH /tasks/{id}

Updates task status.

---

## Telemetry

POST /telemetry

Receives telemetry from Robot Service.

---

GET /telemetry/{robotId}

Returns robot telemetry history.

---

## Alerts

POST /alerts

Creates a system alert.

---

GET /alerts

Returns alerts.

---

# Sprint 1 Goal

The goal of Sprint 1 is NOT to implement every feature.

The goal is to create a production-quality project foundation.

Deliverables:

- Spring Boot project created
- PostgreSQL connected
- Dockerized
- Flyway configured
- Health endpoint
- Folder structure completed
- Base entities
- Repository interfaces
- Basic README

No authentication.

No MQTT.

No scheduling.

No WebSockets.

No business logic.

Those belong to future sprints.

---

# Development Order

1.

Bootstrap Spring Boot

↓

2.

Configure PostgreSQL

↓

3.

Configure Docker

↓

4.

Configure Flyway

↓

5.

Create Entities

↓

6.

Create Repository Layer

↓

7.

Create DTOs

↓

8.

Create Service Layer

↓

9.

Create Controllers

↓

10.

Swagger Documentation

---

# Coding Standards

Use constructor injection.

Never use field injection.

Prefer immutable DTOs where possible.

Keep methods small.

Each class should have one responsibility.

Avoid static utility classes unless necessary.

Every public endpoint should have validation.

Every service method should have JavaDoc.

Use meaningful variable names.

Avoid abbreviations.

---

# Definition of Done

A feature is considered complete when:

- Code compiles
- Tests pass
- API documented
- Docker works
- Flyway migration exists
- No duplicated code
- Clean architecture maintained
- README updated

---

# Long-Term Vision

Future sprints will introduce:

- JWT Authentication
- MQTT Broker
- WebSockets
- Robot Assignment Engine
- Route Planning
- Event Sourcing
- Analytics
- Metrics
- Monitoring
- Prometheus
- Grafana
- Kubernetes Deployment

These features should not influence Sprint 1 implementation.

The focus is on building a solid engineering foundation.

---

# Instructions for AI Coding Assistants

When generating code:

- Follow Spring Boot best practices.
- Use Java 21 features where appropriate.
- Keep architecture modular.
- Prefer readability over clever code.
- Generate production-quality code.
- Do not introduce unnecessary complexity.
- Follow REST conventions.
- Do not skip validation.
- Ensure code is testable.
- Explain architectural decisions when introducing new patterns.