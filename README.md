# PAYGUARD

Real-time payment fraud detection and risk engine built as an event-driven software system.

## Architecture

`React Dashboard → Spring Boot Transaction API → Kafka → Fraud/Risk Processing → PostgreSQL + Redis`

A Python ML service provides anomaly scoring using Isolation Forest. The platform is designed around secure APIs, idempotent transaction processing, auditable decisions, and scalable event processing.

## Stack

- Java 21, Spring Boot, REST APIs
- Apache Kafka
- PostgreSQL
- Redis
- Python, FastAPI, scikit-learn
- React
- Docker / Docker Compose
- GitHub Actions

## Local setup

Prerequisites: Docker Desktop, Git, Java 21, Maven 3.9+, Node.js 20+.

```bash
docker compose up --build
```

API: http://localhost:8080
ML service: http://localhost:8000/docs

### Create a transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"userId":"USR-1001","amount":85000,"currency":"INR","merchantId":"MER-10","merchantCategory":"ELECTRONICS","location":"BENGALURU","deviceId":"DEV-99","transactionTime":"2026-10-07T16:00:00Z"}'
```

## Project status

Core transaction ingestion and event infrastructure are under active development. Fraud rules, scoring, security hardening, dashboard, test coverage, and production deployment are subsequent milestones.

Dashboard: http://localhost:5173

## Roadmap

- [x] Transaction REST API
- [x] PostgreSQL persistence
- [x] Kafka transaction events
- [x] Initial rule-based risk consumer
- [x] Redis risk-result cache
- [x] React monitoring dashboard
- [ ] JWT/RBAC security hardening
- [ ] ML score integration into the Java risk decision
- [ ] Idempotency and dead-letter handling
- [ ] Integration/load tests
- [ ] Production cloud deployment
