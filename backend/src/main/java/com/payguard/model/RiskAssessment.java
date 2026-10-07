package com.payguard.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments", indexes = {
        @Index(name = "idx_risk_transaction", columnList = "transactionId"),
        @Index(name = "idx_risk_decision", columnList = "decision")
})
public class RiskAssessment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true) private String transactionId;
    @Column(nullable = false) private int ruleScore;
    @Column(nullable = false) private double anomalyScore;
    @Column(nullable = false) private int riskScore;
    @Column(nullable = false, length = 16) private String riskLevel;
    @Column(nullable = false, length = 16) private String decision;
    @Column(length = 1000) private String reasons;
    @Column(nullable = false) private Instant evaluatedAt;

    public UUID getId(){return id;}
    public String getTransactionId(){return transactionId;} public void setTransactionId(String v){transactionId=v;}
    public int getRuleScore(){return ruleScore;} public void setRuleScore(int v){ruleScore=v;}
    public double getAnomalyScore(){return anomalyScore;} public void setAnomalyScore(double v){anomalyScore=v;}
    public int getRiskScore(){return riskScore;} public void setRiskScore(int v){riskScore=v;}
    public String getRiskLevel(){return riskLevel;} public void setRiskLevel(String v){riskLevel=v;}
    public String getDecision(){return decision;} public void setDecision(String v){decision=v;}
    public String getReasons(){return reasons;} public void setReasons(String v){reasons=v;}
    public Instant getEvaluatedAt(){return evaluatedAt;} public void setEvaluatedAt(Instant v){evaluatedAt=v;}
}
