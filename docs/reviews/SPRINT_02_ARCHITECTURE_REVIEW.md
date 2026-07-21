# Sprint 02 Architecture Review
## Capability: Warehouse Task Management

Version: 1.0

Status: Approved for Development

Date:

---

# Objective

Sprint 2 introduces Warehouse Task Management.

Unlike Sprint 1, which established the Robot domain, this sprint introduces work that robots perform.

The goal is to model warehouse operations instead of simply storing data.

---

# Business Context

A warehouse receives requests to move inventory.

Examples:

- Pick pallet
- Deliver pallet
- Restock shelf
- Transport inventory
- Return empty container

The Fleet Service stores and coordinates these requests.

Robots execute them.

---

# Why This Sprint Exists

Without tasks:

- Robots have nothing to execute.
- Dashboard has nothing meaningful to display.
- Robot Service cannot demonstrate autonomous behaviour.
- MQTT communication has no business purpose.

Task Management becomes the central workflow of the system.

---

# System Impact

Modules affected:

- Robot Management
- Task Management

Future dependencies:

- MQTT
- Robot Service
- Dashboard
- Telemetry
- Scheduling Engine

No existing API contracts should be broken.

---

# Domain Relationships

Robot

1

↓

Many

↓

Tasks

Every task belongs to at most one robot.

A robot may have many historical tasks.

---

# Design Decisions

## Decision 1

Use JPA relationships instead of storing robotId manually.

Reason:

- Better object modelling
- Easier joins
- Cleaner repository methods

---

## Decision 2

Task numbers should be human readable.

Example

TASK-000001

Reason:

Warehouse operators should reference task numbers rather than database IDs.

---

## Decision 3

Task lifecycle is controlled by Fleet.

Robot Service reports progress only.

Reason:

Fleet owns orchestration.

Robots execute instructions.

---

## Decision 4

Business logic belongs inside services.

Controllers remain thin.

Repositories only access persistence.

---

# Task Lifecycle

NEW

↓

PENDING

↓

ASSIGNED

↓

IN_PROGRESS

↓

COMPLETED

or

FAILED

or

CANCELLED

Transitions should be validated.

Invalid state transitions should be rejected.

---

# Risks

Potential issue:

Deleting robots with assigned tasks.

Mitigation:

Disallow deletion if active tasks exist.

(Currently may be implemented as a placeholder.)

---

Potential issue:

Assigning offline robots.

Mitigation:

Future validation based on RobotStatus.

---

Potential issue:

Task reassignment.

Mitigation:

Keep assignment manual for MVP.

---

# Out of Scope

Automatic scheduling

Route optimisation

Battery-aware assignment

AI task planning

Warehouse zoning

Path planning

Obstacle avoidance

These belong to future sprints.

---

# Architecture Checklist

Before merging Sprint 2:

- Entity relationships are correct.
- DTOs are used.
- Controllers are thin.
- Services contain business logic.
- Validation exists.
- Flyway migration matches database design.
- OpenAPI documentation is updated.
- Tests pass.
- Docker build succeeds.

---

# Review Outcome

Status:

☐ Approved

☐ Changes Required

Review Notes:

____________________________________

____________________________________

____________________________________

____________________________________
