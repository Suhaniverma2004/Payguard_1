# PAYGUARD Architecture

## Core flow

1. Client submits a transaction to the Spring Boot API.
2. API validates and persists the transaction in PostgreSQL.
3. API publishes a transaction event to Kafka.
4. Risk processing consumes the event.
5. Rule-based and ML signals are combined into a risk decision.
6. Risk results are persisted and exposed to the dashboard.

## Engineering goals

- Low-latency transaction ingestion
- At-least-once event delivery with idempotent consumers
- Auditable fraud decisions
- Secure authentication and authorization
- Independent scaling of API, risk processing, and ML workloads
