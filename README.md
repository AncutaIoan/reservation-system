# Reservation System

A high-concurrency reservation platform built with **Java 25** and **Spring Boot**.

The goal of this project is to go beyond a basic CRUD application and explore real backend engineering problems such as:

* Concurrent reservations
* Race conditions
* Database locking
* Transactions
* Idempotency
* Event-driven architecture
* Kafka
* Transactional Outbox
* Caching
* Observability
* Failure handling
* Load testing

The core requirement is simple:

> If 100 users attempt to reserve the same seat at the same time, exactly one reservation must succeed.

---

# Tech Stack

## Core

```text
Java 25
Spring Boot
Spring MVC
Maven
PostgreSQL
Spring Data JPA
Hibernate
Flyway
Docker Compose
```

## Testing

```text
JUnit
Mockito
AssertJ
Testcontainers
```

## Planned

```text
Kafka
Redis
Micrometer
Prometheus
Grafana
OpenTelemetry
Spring Security
```

---

# Architecture

The project starts as a **modular monolith**.

```text
reservation-system
│
├── event
├── seat
├── reservation
├── payment
├── notification
└── shared
```

The initial architecture is:

```text
Client
  ↓
Spring MVC Controller
  ↓
Application Service
  ↓
Domain Logic
  ↓
Spring Data JPA
  ↓
PostgreSQL
```

---

# Package Structure

```text
src/main/java/com/miu/reservations
│
├── event
│   ├── Event.java
│   ├── EventController.java
│   ├── EventService.java
│   └── EventRepository.java
│
├── seat
│   ├── Seat.java
│   ├── SeatController.java
│   ├── SeatService.java
│   └── SeatRepository.java
│
├── reservation
│   ├── Reservation.java
│   ├── ReservationController.java
│   ├── ReservationService.java
│   └── ReservationRepository.java
│
└── ReservationSystemApplication.java
```

The project uses **package-by-feature** rather than:

```text
controller/
service/
repository/
entity/
```

---

# Requirements

Install:

```text
Java 25
Docker
Docker Compose
Git
```

Verify Java:

```bash
java -version
javac -version
```

Expected:

```text
openjdk version "25..."
javac 25...
```

Verify Docker:

```bash
docker info
```

---

# PostgreSQL

PostgreSQL runs inside Docker.

## compose.yaml

```yaml
services:
  postgres:
    image: postgres:18

    container_name: reservation-postgres

    environment:
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: mypassword
      POSTGRES_DB: mydatabase

    ports:
      - "5432:5432"

    volumes:
      - postgres_data:/var/lib/postgresql

    healthcheck:
      test:
        [
          "CMD-SHELL",
          "pg_isready -U myuser -d mydatabase"
        ]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

Start PostgreSQL:

```bash
docker compose up -d
```

Check container status:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f postgres
```

Stop containers:

```bash
docker compose down
```

Delete containers and database volume:

```bash
docker compose down -v
```

> Warning: `-v` deletes the PostgreSQL data volume.

---

# Spring Database Configuration

Configure:

```text
src/main/resources/application.properties
```

with:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=mypassword

spring.jpa.hibernate.ddl-auto=validate

spring.flyway.enabled=true
```

Spring connects to:

```text
Spring Boot
    ↓
localhost:5432
    ↓
Docker
    ↓
PostgreSQL
```

---

# Database Migrations

The database schema is managed using **Flyway**.

Migration files should be placed under:

```text
src/main/resources/db/migration
```

Example:

```text
V1__create_event_table.sql
V2__create_seat_table.sql
V3__create_reservation_table.sql
```

Do not use:

```properties
spring.jpa.hibernate.ddl-auto=update
```

The schema should be controlled by Flyway.

Use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

so Hibernate validates that the entities match the database schema.

---

# Running the Application

Start PostgreSQL:

```bash
docker compose up -d
```

Then run the Spring application:

```bash
./mvnw spring-boot:run
```

Or run:

```java
ReservationSystemApplication.main()
```

directly from IntelliJ IDEA.

---

# Building the Project

Run tests:

```bash
./mvnw test
```

Build:

```bash
./mvnw clean package
```

Run packaged application:

```bash
java -jar target/reservation-system-*.jar
```

---

# Domain

The initial domain consists of:

```text
Event
Seat
Reservation
```

Later:

```text
Payment
User
Notification
```

---

# Reservation Flow

```text
User selects seat
       ↓
Reservation created
       ↓
Seat becomes HELD
       ↓
User completes payment
       ↓
Reservation becomes CONFIRMED
```

If payment does not succeed:

```text
HELD
 ↓
expires
 ↓
EXPIRED
```

---

# Reservation States

Initial states:

```java
public enum ReservationStatus {
    HELD,
    CONFIRMED,
    EXPIRED,
    CANCELLED
}
```

Lifecycle:

```text
AVAILABLE
    ↓
HELD
    ↓
CONFIRMED
```

or:

```text
HELD
 ↓
EXPIRED
```

---

# Initial API

## Create Event

```http
POST /events
```

## Add Seat

```http
POST /events/{eventId}/seats
```

## List Seats

```http
GET /events/{eventId}/seats
```

## List Available Seats

```http
GET /events/{eventId}/available-seats
```

## Reserve Seat

```http
POST /reservations
```

## Get Reservation

```http
GET /reservations/{reservationId}
```

## Confirm Reservation

```http
POST /reservations/{reservationId}/confirm
```

## Cancel Reservation

```http
DELETE /reservations/{reservationId}
```

---

# First Major Engineering Goal

The first serious goal of the project is:

> Prove that exactly one user can reserve a seat when many users attempt to reserve it concurrently.

Example:

```text
100 concurrent users
        ↓
      Seat A12
        ↓
   Reservation API
        ↓
    PostgreSQL
```

Expected result:

```text
100 attempts

1 success
99 conflicts
```

---

# Concurrency

The project will compare several approaches.

## Naive Approach

```text
SELECT seat
    ↓
check available
    ↓
INSERT reservation
```

This can create a race condition.

Two requests may both observe:

```text
AVAILABLE
```

before either transaction commits.

---

# Database Constraints

The database should enforce important invariants.

Application code alone should not be trusted to prevent invalid states.

Examples:

```text
unique seat reservation
valid foreign keys
valid reservation state
```

---

# Optimistic Locking

Experiment with a version field:

```java
@Version
private Long version;
```

Conceptually:

```text
Request A reads version 5
Request B reads version 5

Request A updates:
5 → 6

Request B tries to update version 5

Conflict
```

Study:

```text
retries
contention
performance
conflict handling
```

---

# Pessimistic Locking

Experiment with row-level database locks.

Conceptually:

```sql
SELECT *
FROM seat
WHERE id = ?
FOR UPDATE;
```

Study:

```text
lock contention
deadlocks
transaction duration
throughput
```

---

# Java 25 Concurrency

The project should also be used to learn Java 25 concurrency.

Example:

```java
try (var executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    // submit concurrent reservation attempts

}
```

Topics:

```text
virtual threads
thread safety
race conditions
locks
synchronization
executors
CompletableFuture
```

---

# Testing

## Unit Tests

Use:

```text
JUnit
Mockito
AssertJ
```

Examples:

```text
expired reservation cannot be confirmed

confirmed reservation cannot expire

invalid reservation state is rejected
```

---

# Integration Tests

Use **Testcontainers** with a real PostgreSQL instance.

Example goal:

```text
100 concurrent reservation attempts
          ↓
same PostgreSQL database
          ↓
exactly 1 succeeds
```

The important concurrency behavior should not be tested only with mocks.

---

# Payment

Later introduce a simulated payment service.

States:

```text
PENDING
   ↓
SUCCESS
```

or:

```text
PENDING
   ↓
FAILED
```

---

# Payment Idempotency

Clients may retry requests because of:

```text
network timeout
lost response
client retry
load balancer retry
```

Payments should support:

```http
Idempotency-Key: unique-key
```

Expected behavior:

```text
same key
+
same request
       ↓
```
