# VectorOS - Project Context

> This document provides the overall context for the VectorOS platform. Every service within the VectorOS ecosystem should follow the architectural principles and long-term vision described here.

---

# What is VectorOS?

VectorOS is a modular robotics software platform for managing autonomous warehouse robots.

The project is inspired by modern warehouse automation systems used by companies such as Amazon Robotics, Ocado Technology, ABB, KUKA, Siemens, Bosch, and other industrial automation companies.

Rather than controlling a single robot, VectorOS manages an entire fleet of autonomous robots operating inside a warehouse.

The platform is designed using a microservice architecture so that each component can evolve independently.

This project is intended as a portfolio project demonstrating modern software engineering practices for robotics software systems.

---

# Project Objectives

VectorOS demonstrates:

- Modern software engineering
- Distributed systems
- Robotics software architecture
- Backend engineering
- Computer vision
- Robot communication
- System integration
- Clean architecture
- Scalable microservices

The emphasis is on software engineering rather than hardware.

---

# System Overview

The complete platform consists of five independent repositories.

```
                +----------------------+
                |     Dashboard        |
                | React + TypeScript   |
                +----------+-----------+
                           |
                    REST / WebSocket
                           |
                           v
               +------------------------+
               |     Fleet Service      |
               | Spring Boot + Java     |
               +-----------+------------+
                           |
          +----------------+----------------+
          |                                 |
      MQTT / REST                       MQTT / REST
          |                                 |
          v                                 v
+---------------------+          +----------------------+
|   Robot Service     |          |   Vision Service     |
|      Modern C++     |          | Python + OpenCV      |
+---------------------+          +----------------------+
                           |
                           v
                 Warehouse Environment

```

---

# Repository Responsibilities

## 1. VectorOS Fleet

Technology

- Java
- Spring Boot
- PostgreSQL
- Docker

Responsibilities

- Register robots
- Manage robot state
- Store telemetry
- Manage warehouse tasks
- Robot assignment
- Alert management
- Fleet monitoring
- REST APIs
- WebSocket APIs
- MQTT communication

Fleet is the central coordinator of the platform.

Fleet never controls robot motors directly.

---

## 2. VectorOS Robot

Technology

- Modern C++20
- CMake
- Docker

Responsibilities

- Simulate robot behaviour
- Receive warehouse tasks
- Navigate environment
- Battery simulation
- Position updates
- Publish telemetry
- Receive commands
- Report robot status

Robot acts as the autonomous worker.

---

## 3. VectorOS Vision

Technology

- Python
- FastAPI
- OpenCV
- YOLO

Responsibilities

- Camera processing
- Obstacle detection
- Pallet detection
- Worker detection
- Alert generation
- Publish detection events

Vision acts as the perception system.

---

## 4. VectorOS Dashboard

Technology

- React
- TypeScript
- TailwindCSS

Responsibilities

- Fleet visualization
- Robot monitoring
- Task management
- Alerts
- Live telemetry
- Warehouse analytics

Dashboard is the operator interface.

---

## 5. VectorOS Platform

Technology

- Docker Compose

Responsibilities

- Infrastructure
- Service orchestration
- Local deployment
- Environment configuration
- Integration testing

Platform exists only to deploy and integrate every service.

---

# System Workflow

Example workflow.

Customer requests a warehouse operation.

↓

Fleet creates a warehouse task.

↓

Fleet assigns a robot.

↓

Robot receives assignment.

↓

Robot begins navigation.

↓

Robot continuously publishes telemetry.

↓

Vision detects an obstacle.

↓

Vision publishes an alert.

↓

Fleet processes alert.

↓

Fleet reroutes robot.

↓

Robot reaches destination.

↓

Fleet marks task completed.

↓

Dashboard updates in real time.

---

# Design Principles

Every service should follow these principles.

## Clean Architecture

Business logic should never depend on frameworks.

Frameworks should support the application rather than define it.

---

## Single Responsibility Principle

Each class should have one responsibility.

---

## Dependency Injection

Prefer constructor injection.

Avoid tightly coupled components.

---

## Separation of Concerns

Controllers

↓

Services

↓

Repositories

↓

Database

No layer should bypass another layer.

---

## Modularity

Every service must be independently deployable.

Services communicate only through well-defined APIs or messaging.

---

# Communication

Primary communication methods:

REST APIs

- CRUD operations
- Queries
- Commands

MQTT

- Robot telemetry
- Robot status
- Vision events
- Alerts

WebSockets

- Live dashboard updates

---

# Long-Term Roadmap

## Phase 1

Project foundation

- Project setup
- Docker
- PostgreSQL
- REST APIs

---

## Phase 2

Fleet Management

- Robot registration
- Task management
- Robot assignment

---

## Phase 3

Robot Simulation

- Navigation
- Battery
- Position
- Telemetry

---

## Phase 4

Computer Vision

- OpenCV
- YOLO
- Obstacle detection

---

## Phase 5

Dashboard

- Live monitoring
- Task control
- Analytics

---

## Phase 6

Integration

- Docker Compose
- Full platform deployment

---

## Phase 7

Advanced Features

- Authentication
- RBAC
- Route optimization
- Dynamic task assignment
- Robot scheduling
- Monitoring
- Prometheus
- Grafana
- Kubernetes

---

# Engineering Standards

The project should resemble production software rather than a university assignment.

Every repository should include:

- Comprehensive README
- Architecture documentation
- Docker support
- Unit tests where practical
- Consistent code style
- Clear commit history
- Meaningful GitHub issues
- Professional folder structure

---

# AI Coding Assistant Guidelines

When generating code:

- Follow production-quality software engineering practices.
- Prefer readability over clever implementations.
- Keep methods small and focused.
- Follow SOLID principles.
- Use appropriate design patterns only when justified.
- Do not generate placeholder or toy implementations unless requested.
- Maintain consistency with the architecture defined in this document.
- Consider future integration with the other VectorOS services.
- Explain architectural decisions when introducing new abstractions.

---

# Vision

VectorOS is not intended to be a simple robotics demo.

The objective is to build a realistic, modular robotics software platform that demonstrates how autonomous warehouse systems are engineered in industry.

Each repository should be capable of standing alone while also integrating seamlessly into the complete VectorOS ecosystem.