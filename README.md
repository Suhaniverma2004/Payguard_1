# PAYGUARD

**Real-time payment fraud detection and risk engine** built as an event-driven software system.

PAYGUARD accepts payment transactions through a secured Spring Boot API, persists them through a transactional outbox, publishes events to Kafka, evaluates deterministic fraud rules plus a Python anomaly model, persists the resulting decision in PostgreSQL, and caches hot risk data in Redis. A React command center provides authenticated monitoring and transaction simulation.

## Architecture

```text
React Dashboard
      |
      | JWT-secured REST
      v
Spring Boot API ---- PostgreSQL
      |                 |
      |                 +---- Transaction + Risk Audit
      |
      +---- Transactional Outbox
                    |
                    v
                Apache Kafka
                    |
                    v
              Fraud Risk Engine
                |          |
                |          +---- FastAPI / Isolation Forest
                |
                +---- Rule scoring + velocity signals
                |
                +---- PostgreSQL risk assessment
                +---- Redis risk cache

Kafka processing failures -> retry / dead-letter handling
```

## Engineering features

- JWT authentication with BCrypt password hashing.
- Role-aware security boundary with an ADMIN-only metrics endpoint.
- User-isolated transaction and risk access.
- Idempotent transaction ingestion through the `Idempotency-Key` header.
- Transactional outbox for atomic transaction + event persistence.
- Kafka event-driven risk processing with reliable producer/consumer settings.
- Retry handling and a dead-letter topic for failed transaction events.
- Hybrid risk score combining deterministic transaction rules and ML anomaly scoring.
- Five-minute per-user transaction velocity signal.
- PostgreSQL persistence for transactions and auditable risk assessments.
- Versioned Flyway database migrations with Hibernate schema validation.
- Redis cache for low-latency risk lookups.
- Correlation IDs, health probes, metrics and structured application logging.
- React monitoring dashboard with live polling and transaction simulation.
- Production-style Docker images with health checks and non-root backend/ML containers.
- Nginx-served production React dashboard.
- GitHub Actions CI for Java tests, frontend build, ML tests and Docker validation.

## Stack

- **Backend:** Java 21, Spring Boot, Spring Security, REST APIs, JPA, Flyway
- **Streaming:** Apache Kafka
- **Data:** PostgreSQL, Redis
- **ML:** Python, FastAPI, scikit-learn, Isolation Forest
- **Frontend:** React, Vite, Recharts, Nginx
- **DevOps:** Docker, Docker Compose, GitHub Actions

## Local deployment

Prerequisites: Docker Desktop with Compose.

From the repository root:

```bash
cp .env.example .env
```

Set a real local password and JWT secret in `.env`, then:

```bash
docker compose up --build -d
```

Services:

- Dashboard: `http://localhost:5173`
- API: `http://localhost:8080`
- ML service docs: `http://localhost:8000/docs`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`

Check the stack:

```bash
docker compose ps
docker compose logs -f backend
```

Stop it with:

```bash
docker compose down
```

The PostgreSQL data volume is preserved by default. Use `docker compose down -v` only when you intentionally want to remove the local database.

## API flow

### 1. Create an account

```bash
curl -X POST http://localhost:8080/api/v1/auth/signup   -H "Content-Type: application/json"   -d '{"username":"demo","password":"demo123"}'
```

Use the returned JWT as `Authorization: Bearer <token>` for protected endpoints.

### 2. Submit a transaction

Every transaction request should carry a unique idempotency key.

```bash
curl -X POST http://localhost:8080/api/v1/transactions   -H "Authorization: Bearer <token>"   -H "Idempotency-Key: demo-txn-001"   -H "Content-Type: application/json"   -d '{"userId":"USR-1001","amount":85000,"currency":"INR","merchantId":"MER-10","merchantCategory":"ELECTRONICS","location":"BENGALURU","deviceId":"DEV-99","transactionTime":"2026-10-07T16:00:00Z"}'
```

The API persists the transaction and emits a Kafka event. The risk engine consumes the event asynchronously.

### 3. Read risk assessments

```bash
curl http://localhost:8080/api/v1/risk   -H "Authorization: Bearer <token>"
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

## Production-readiness scope

PAYGUARD now has versioned persistence, application security, observability, health checks, reproducible container builds, a production-style frontend image, CI validation, and a documented local deployment path.

The repository intentionally remains a **single-node demonstration/deployment architecture**. A real production rollout would additionally require infrastructure-level TLS, external secret management, managed PostgreSQL/Redis/Kafka or a multi-broker Kafka cluster, horizontal scaling, load balancing, backups, disaster recovery, and a properly trained/calibrated fraud model.

## Project status

The core event-driven payment risk pipeline is implemented and CI-validated. The ML service remains a research/demo anomaly model trained on synthetic data; it should not be represented as a production fraud model.

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
- [x] User isolation and idempotency hardening
- [x] Transactional outbox
- [x] Kafka retry + dead-letter handling
- [x] React monitoring dashboard and simulator
- [x] Versioned Flyway database migrations
- [x] Observability and health probes
- [x] Dockerized local deployment
- [x] Production-style frontend container
- [x] GitHub Actions CI/CD validation
- [ ] Multi-node cloud infrastructure
- [ ] Load testing and performance benchmarks
- [ ] Calibrated fraud model trained on a real or responsibly sourced dataset
