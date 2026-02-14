# CashGrid — Commercial Cash Payment Platform

A production-grade backend for a Turkish banking/fintech cash payment system built with **Spring Boot 4** and **Java 21**.

---

## Tech Stack

| Layer            | Technology                                |
| ---------------- | ----------------------------------------- |
| Language         | Java 21                                   |
| Framework        | Spring Boot 4.0.2                         |
| Database         | PostgreSQL 18                             |
| Migrations       | Flyway                                    |
| Auth             | JWT (jjwt 0.13.0) + Spring Security       |
| Validation       | Jakarta Validation + Custom IBAN (Mod-97) |
| Mapping          | MapStruct 1.6.3                           |
| API Docs         | springdoc-openapi 3.0.1 (Swagger UI)      |
| Rate Limiting    | Bucket4j 8.16.1                           |
| Build            | Gradle 9.3.1 (Kotlin DSL)                 |
| Code Style       | Spotless + Google Java Format (AOSP)      |
| Testing          | JUnit 5 + Testcontainers 1.21.4           |
| Containerization | Docker (multi-stage build)                |
| CI               | GitHub Actions                            |

---

## Architecture

```
com.erenalyoruk.cashgrid
├── auth/          # JWT authentication, login, register, token refresh
├── account/       # Account CRUD, IBAN validation (TR Mod-97)
├── payment/       # Payment workflow with maker-checker approval
├── limit/         # Per-role, per-currency single & daily limits
├── audit/         # Immutable audit log for all payment events
└── common/        # Shared: exception handling, filters, DTOs
```

### Key Features

- **JWT Authentication** — Access + refresh tokens, role-based authorization (`MAKER`, `CHECKER`, `ADMIN`)
- **Maker-Checker Payments** — Maker creates payment, Checker approves/rejects. State machine: `PENDING → APPROVED/REJECTED`, `APPROVED → COMPLETED/FAILED`
- **IBAN Validation** — Turkish IBAN format with Mod-97 check digit verification
- **Pessimistic Locking** — `SELECT FOR UPDATE` on balance transfers to prevent race conditions
- **Idempotency** — Duplicate payment detection via reference number
- **Rate Limiting** — Per-IP and per-token rate limits using token bucket algorithm
- **Correlation ID** — Request tracing via `X-Correlation-Id` header on every request/response
- **Audit Trail** — Every payment state change is recorded with actor, action, timestamp, and correlation ID
- **Soft Delete** — Accounts use soft delete (`deleted` flag)
- **Pagination** — Consistent `PageResponse<T>` wrapper for all list endpoints

---

## Prerequisites

- **Java 21** (or Docker)
- **Docker & Docker Compose** (for PostgreSQL and containerized deployment)

---

## Quick Start

### Option 1: Docker Compose (full stack)

```bash
docker compose up --build
```

This starts PostgreSQL and the backend. The API is available at `http://localhost:8080`.

### Option 2: Local Development

1. Start PostgreSQL only:

```bash
docker compose up postgres
```

2. Run the backend with the `dev` profile:

```bash
./gradlew :backend:bootRun
```

The backend starts on port **8080** with the `dev` profile by default.

---

## API Documentation

Once the application is running, visit:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## API Overview

### Auth

| Method | Endpoint                | Description           | Access        |
| ------ | ----------------------- | --------------------- | ------------- |
| POST   | `/api/v1/auth/register` | Register a new user   | Public        |
| POST   | `/api/v1/auth/login`    | Login and get tokens  | Public        |
| POST   | `/api/v1/auth/refresh`  | Refresh access token  | Public        |
| GET    | `/api/v1/auth/me`       | Get current user info | Authenticated |

### Accounts

| Method | Endpoint                | Description               | Access        |
| ------ | ----------------------- | ------------------------- | ------------- |
| POST   | `/api/v1/accounts`      | Create an account         | ADMIN         |
| GET    | `/api/v1/accounts`      | List accounts (paginated) | Authenticated |
| GET    | `/api/v1/accounts/{id}` | Get account by ID         | Authenticated |
| PUT    | `/api/v1/accounts/{id}` | Update account            | ADMIN         |
| DELETE | `/api/v1/accounts/{id}` | Soft delete account       | ADMIN         |

### Payments

| Method | Endpoint                        | Description               | Access        |
| ------ | ------------------------------- | ------------------------- | ------------- |
| POST   | `/api/v1/payments`              | Create a payment          | MAKER         |
| GET    | `/api/v1/payments`              | List payments (paginated) | Authenticated |
| GET    | `/api/v1/payments/{id}`         | Get payment by ID         | Authenticated |
| POST   | `/api/v1/payments/{id}/approve` | Approve a payment         | CHECKER       |
| POST   | `/api/v1/payments/{id}/reject`  | Reject a payment          | CHECKER       |

### Limits

| Method | Endpoint              | Description    | Access |
| ------ | --------------------- | -------------- | ------ |
| GET    | `/api/v1/limits`      | List limits    | ADMIN  |
| PUT    | `/api/v1/limits/{id}` | Update a limit | ADMIN  |
| POST   | `/api/v1/limits`      | Create a limit | ADMIN  |

### Audit Logs

| Method | Endpoint                  | Description                 | Access |
| ------ | ------------------------- | --------------------------- | ------ |
| GET    | `/api/v1/audit-logs`      | List audit logs (paginated) | ADMIN  |
| GET    | `/api/v1/audit-logs/{id}` | Get audit log by ID         | ADMIN  |

---

## Example Flow

```bash
# 1. Register a MAKER user
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"maker1","password":"Pass1234!","role":"MAKER"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"maker1","password":"Pass1234!"}' | jq -r '.accessToken')

# 3. Create a payment (requires MAKER role + existing accounts)
curl -s -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "sourceAccountId": 1,
    "targetAccountId": 2,
    "amount": 1000.00,
    "currency": "TRY",
    "description": "Invoice payment"
  }'

# 4. Login as CHECKER and approve
CHECKER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"checker1","password":"Pass1234!"}' | jq -r '.accessToken')

curl -s -X POST http://localhost:8080/api/v1/payments/1/approve \
  -H "Authorization: Bearer $CHECKER_TOKEN"
```

---

## Testing

All 35 integration tests use **Testcontainers** (spins up a real PostgreSQL container automatically — Docker must be running):

```bash
./gradlew :backend:test
```

View HTML test report:

```
backend/build/reports/tests/test/index.html
```

### Test Suites

| Suite                   | Tests | Description                                       |
| ----------------------- | ----- | ------------------------------------------------- |
| AuthIntegrationTest     | 8     | Register, login, refresh, /me, validation         |
| AccountIntegrationTest  | 5     | CRUD, IBAN validation, soft delete                |
| PaymentIntegrationTest  | 8     | Create, approve, reject, idempotency, limits      |
| LimitIntegrationTest    | 3     | CRUD, limit enforcement                           |
| AuditLogIntegrationTest | 5     | Audit log listing, filtering, payment audit trail |
| CorrelationIdTest       | 2     | X-Correlation-Id header propagation               |
| RateLimitTest           | 3     | Per-IP and per-token rate limiting                |
| CashGridApplicationTest | 1     | Application context loads                         |

---

## Code Formatting

The project enforces **Google Java Format (AOSP)** via Spotless:

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

Formatting is checked automatically in CI and as part of `./gradlew check`.

---

## CI / CD

GitHub Actions runs on every push/PR to `main`:

1. **Format** — `spotlessCheck`
2. **Test** — Full integration test suite with Testcontainers
3. **Build** — Produces bootJar artifact

See [.github/workflows/ci.yml](.github/workflows/ci.yml) for details.

---

## Project Structure

```
cash-grid/
├── .github/workflows/ci.yml        # GitHub Actions CI pipeline
├── backend/
│   ├── Dockerfile                   # Multi-stage Docker build
│   ├── build.gradle.kts             # Backend dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/erenalyoruk/cashgrid/
│       │   │   ├── CashGridApplication.java
│       │   │   ├── auth/            # JWT auth module
│       │   │   ├── account/         # Account CRUD module
│       │   │   ├── payment/         # Payment workflow module
│       │   │   ├── limit/           # Transaction limits module
│       │   │   ├── audit/           # Audit logging module
│       │   │   └── common/          # Shared components
│       │   └── resources/
│       │       ├── application.yaml
│       │       ├── application-{dev,test,prod}.yaml
│       │       └── db/migration/    # Flyway V1–V5
│       └── test/                    # Integration tests
├── docker-compose.yml               # PostgreSQL + Backend
├── build.gradle.kts                 # Root build config + Spotless
├── settings.gradle.kts
└── gradle/                          # Gradle 9.3.1 wrapper
```

---

## Configuration

### Spring Profiles

| Profile         | Purpose           | Database                   |
| --------------- | ----------------- | -------------------------- |
| `dev` (default) | Local development | localhost:5432             |
| `test`          | Integration tests | Testcontainers (automatic) |
| `prod`          | Production        | Environment variables      |

### Environment Variables (prod)

| Variable                 | Description                                            |
| ------------------------ | ------------------------------------------------------ |
| `DATABASE_URL`           | JDBC URL (e.g. `jdbc:postgresql://host:5432/cashgrid`) |
| `DATABASE_USERNAME`      | Database username                                      |
| `DATABASE_PASSWORD`      | Database password                                      |
| `JWT_SECRET`             | JWT signing secret (min 32 characters)                 |
| `JWT_ACCESS_EXPIRATION`  | Access token TTL in ms (default: 900000 = 15 min)      |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL in ms (default: 604800000 = 7 days)  |

---

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

---

## License

This project is for educational and demonstration purposes.
