CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT uk_app_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    merchant_id VARCHAR(255),
    merchant_category VARCHAR(255),
    location VARCHAR(255),
    device_id VARCHAR(255),
    transaction_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_transactions_transaction_id UNIQUE (transaction_id),
    CONSTRAINT uk_transactions_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE IF NOT EXISTS risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(255) NOT NULL,
    rule_score INTEGER NOT NULL,
    anomaly_score DOUBLE PRECISION NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    decision VARCHAR(16) NOT NULL,
    reasons VARCHAR(1000),
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_risk_assessments_transaction_id UNIQUE (transaction_id)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username
    ON app_users (username);
CREATE INDEX IF NOT EXISTS idx_transaction_user
    ON transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_status
    ON transactions (status);
CREATE INDEX IF NOT EXISTS idx_risk_transaction
    ON risk_assessments (transaction_id);
CREATE INDEX IF NOT EXISTS idx_risk_decision
    ON risk_assessments (decision);
CREATE INDEX IF NOT EXISTS idx_outbox_published
    ON outbox_events (published_at);
CREATE INDEX IF NOT EXISTS idx_outbox_created
    ON outbox_events (created_at);
