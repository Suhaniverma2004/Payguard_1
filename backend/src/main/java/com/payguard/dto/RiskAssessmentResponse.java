package com.payguard.dto;

import java.time.Instant;

public record RiskAssessmentResponse(
        String transactionId,
        int ruleScore,
        double anomalyScore,
        int riskScore,
        String riskLevel,
        String decision,
        String reasons,
        Instant evaluatedAt
) {}
