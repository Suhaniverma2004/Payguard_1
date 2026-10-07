import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[0]))

from main import build_features, score, RiskRequest


def test_feature_shape_is_consistent():
    features = build_features(amount=2500, hour=14, velocity=2)
    assert features.shape == (1, 4)
    assert features.dtype.kind == "f"


def test_hour_wraps_to_valid_range():
    normal = build_features(2500, 2, 1)
    wrapped = build_features(2500, 26, 1)
    assert normal.tolist() == pytest.approx(wrapped.tolist())


def test_score_returns_bounded_anomaly():
    result = score(RiskRequest(amount=2500, hour=14, velocity=2))
    assert 0.0 <= result["anomalyScore"] <= 1.0
    assert result["model"] == "isolation-forest-v1.1"


def test_extreme_amount_is_more_suspicious_than_typical_amount():
    normal = score(RiskRequest(amount=1500, hour=14, velocity=1))
    extreme = score(RiskRequest(amount=150000, hour=14, velocity=1))

    assert extreme["anomalyScore"] >= normal["anomalyScore"]
