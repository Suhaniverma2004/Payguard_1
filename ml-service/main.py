from math import exp, log1p, pi, sin, cos

import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel
from sklearn.ensemble import IsolationForest

app = FastAPI(
    title="PAYGUARD ML Risk Service",
    version="1.1.0",
)

# Prototype training data is synthetic. The distribution is closer to
# payment traffic than the previous uniform 10-10,000 distribution.
rng = np.random.default_rng(42)

normal_amounts = np.clip(
    rng.lognormal(mean=7.6, sigma=1.15, size=6000),
    10,
    250_000,
)

normal_velocity = np.clip(
    rng.poisson(lam=2.5, size=6000),
    0,
    20,
)

normal_hours = rng.integers(0, 24, size=6000)


def build_features(amount: float, hour: int, velocity: int) -> np.ndarray:
    """Build the same feature representation for training and scoring."""
    safe_amount = max(float(amount), 1.0)
    safe_hour = int(hour) % 24
    safe_velocity = max(int(velocity), 0)

    log_amount = log1p(safe_amount)
    velocity_feature = min(safe_velocity / 10.0, 2.0)

    angle = 2.0 * pi * safe_hour / 24.0
    hour_sin = sin(angle)
    hour_cos = cos(angle)

    return np.array(
        [[log_amount, velocity_feature, hour_sin, hour_cos]],
        dtype=float,
    )


training_features = np.column_stack(
    [
        np.log1p(normal_amounts),
        np.minimum(normal_velocity / 10.0, 2.0),
        np.sin(2.0 * pi * normal_hours / 24.0),
        np.cos(2.0 * pi * normal_hours / 24.0),
    ]
)

model = IsolationForest(
    n_estimators=150,
    contamination=0.08,
    random_state=42,
)
model.fit(training_features)


class RiskRequest(BaseModel):
    amount: float
    hour: int
    velocity: int = 0


@app.get("/health")
def health():
    return {
        "service": "ml-risk-service",
        "status": "UP",
        "model": "isolation-forest-v1.1",
    }


@app.post("/score")
def score(request: RiskRequest):
    features = build_features(
        request.amount,
        request.hour,
        request.velocity,
    )

    raw_score = float(model.decision_function(features)[0])

    # Higher decision_function values are more normal.
    # Convert the result to a bounded anomaly probability-like score.
    anomaly_score = 1.0 / (1.0 + exp(8.0 * raw_score))
    anomaly_score = max(0.0, min(1.0, anomaly_score))

    return {
        "anomalyScore": round(anomaly_score, 4),
        "model": "isolation-forest-v1.1",
    }
