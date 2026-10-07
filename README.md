# PAYGUARD

**Real-time payment fraud detection and risk engine** built as an event-driven software system.

PAYGUARD accepts payment transactions through a secured Spring Boot API, publishes them to Kafka, evaluates deterministic fraud rules plus a Python anomaly model, persists the resulting decision in PostgreSQL, and caches hot risk data in Redis. A React command center provides authenticated monitoring and transaction simulation.

## Architecture

```text
React Dashboard
      |
      | JWT-secured REST
      v
Spring Boot API ---- PostgreSQL
      |
      | transaction event
      v
   Apache Kafka
      |
      v
Fraud Risk Engine
  |           |
  |           +---- FastAPI / Isolation Forest
  |
  +---- rule scoring + velocity signals
      |
      +---- PostgreSQL risk assessment
      +---- Redis risk cache

Kafka failures -> transactions.DLT
```

## Engineering features

- JWT authentication with BCrypt password hashing.
- Role-aware security boundary with an ADMIN-only metrics endpoint.
- Idempotent transaction ingestion through the `Idempotency-Key` header.
- Kafka event-driven risk processing with typed JSON serialization.
- Retry handling and a dead-letter topic for failed transaction events.
- Hybrid risk score combining deterministic transaction rules and ML anomaly scoring.
- Five-minute per-user transaction velocity signal.
- PostgreSQL persistence for transactions and auditable risk assessments.
- Redis cache for low-latency risk lookups.
- React monitoring dashboard with live polling and transaction simulator.
- GitHub Actions CI for Java tests, frontend build, and Python compilation.
- Docker Compose orchestration for local development.

## Stack

- **Backend:** Java 21, Spring Boot, Spring Security, REST APIs, JPA
- **Streaming:** Apache Kafka
- **Data:** PostgreSQL, Redis
- **ML:** Python, FastAPI, scikit-learn, Isolation Forest
- **Frontend:** React, Vite, Recharts
- **DevOps:** Docker, Docker Compose, GitHub Actions

## Local setup

Prerequisites: Docker Desktop with Compose, Git, Java 21, Maven 3.9+, Node.js 20+.

From the repository root:

```bash
docker compose up --build
```

Services:

- Dashboard: `http://localhost:5173`
- API: `http://localhost:8080`
- ML service docs: `http://localhost:8000/docs`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka: `localhost:9092` (broker is used by the containerized API)

## API flow

### 1. Create an account

```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```

Use the returned JWT as `Authorization: Bearer <token>` for protected endpoints.

### 2. Submit a transaction

Every transaction request should carry a unique idempotency key.

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: demo-txn-001" \
  -H "Content-Type: application/json" \
  -d '{"userId":"USR-1001","amount":85000,"currency":"INR","merchantId":"MER-10","merchantCategory":"ELECTRONICS","location":"BENGALURU","deviceId":"DEV-99","transactionTime":"2026-10-07T16:00:00Z"}'
```

The API persists the transaction and emits a Kafka event. The risk engine consumes the event asynchronously.

### 3. Read risk assessments

```bash
curl http://localhost:8080/api/v1/risk \
  -H "Authorization: Bearer <token>"
```

A decision is one of `APPROVE`, `REVIEW`, or `BLOCK`.

## Risk scoring

The current research/demo model intentionally uses transparent signals rather than claiming production fraud accuracy:

- High transaction value contributes rule points.
- Unusual UTC transaction hours contribute risk.
- Missing device/location signals contribute risk.
- Five-minute transaction velocity is sent to the ML service.
- Isolation Forest supplies an anomaly score.
- Final score = 70% rule score + 30% ML anomaly score, capped to 0–100.

Thresholds are configurable in code and are intended for experimentation, not real payment authorization.

## Project status

The repository is an actively developed engineering project. The core event pipeline, authentication, idempotency, hybrid risk scoring, persistence, caching, dashboard, retries/DLT, and CI workflow are implemented. Production cloud deployment, load testing, observability, and model calibration are still future hardening stages.

## Roadmap

- [x] Secured transaction REST API
- [x] PostgreSQL transaction persistence
- [x] Kafka transaction events
- [x] Rule-based risk engine
- [x] Python anomaly scoring service
- [x] Hybrid Java + ML risk decision
- [x] PostgreSQL risk-assessment audit trail
- [x] Redis risk-result cache
- [x] JWT authentication and role-aware authorization
- [x] Idempotency handling
- [x] Kafka retry + dead-letter topic
- [x] React monitoring dashboard and simulator
- [x] GitHub Actions CI
- [ ] Integration tests with Testcontainers
- [ ] Structured observability / metrics / tracing
- [ ] Load testing and performance benchmarks
- [ ] Production cloud deployment
- [ ] Calibrated fraud model trained on a real or responsibly sourced dataset
