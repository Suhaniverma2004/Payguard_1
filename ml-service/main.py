from fastapi import FastAPI
from pydantic import BaseModel
from sklearn.ensemble import IsolationForest
import numpy as np

app=FastAPI(title="PAYGUARD ML Risk Service", version="1.0.0")
model=IsolationForest(n_estimators=100, contamination=0.08, random_state=42)
rng=np.random.default_rng(42)
model.fit(np.column_stack([rng.uniform(10,10000,2000), rng.uniform(0,1,2000), rng.integers(0,24,2000)]))
class RiskRequest(BaseModel):
    amount: float
    hour: int
    velocity: int=0
@app.get("/health")
def health(): return {"service":"ml-risk-service","status":"UP"}
@app.post("/score")
def score(r: RiskRequest):
    x=np.array([[r.amount, min(r.velocity/10,1), r.hour]])
    raw=float(model.decision_function(x)[0])
    anomaly=max(0.0,min(1.0,0.5-raw))
    return {"anomalyScore":round(anomaly,4),"model":"isolation-forest"}
