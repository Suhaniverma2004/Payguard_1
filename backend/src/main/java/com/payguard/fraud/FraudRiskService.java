package com.payguard.fraud;

import com.payguard.event.TransactionEvent;
import com.payguard.model.RiskAssessment;
import com.payguard.repository.RiskAssessmentRepository;
import com.payguard.repository.TransactionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FraudRiskService {

    private final StringRedisTemplate redis;
    private final RiskAssessmentRepository risks;
    private final TransactionRepository transactions;
    private final RestClient ml;

    public FraudRiskService(
            StringRedisTemplate redis,
            RiskAssessmentRepository risks,
            TransactionRepository transactions,
            RestClient.Builder builder
    ) {
        this.redis = redis;
        this.risks = risks;
        this.transactions = transactions;
        this.ml = builder
                .baseUrl(System.getenv().getOrDefault(
                        "ML_SERVICE_URL",
                        "http://localhost:8000"
                ))
                .build();
    }

    @Transactional
    @KafkaListener(
            topics = "transactions",
            groupId = "payguard-risk-engine"
    )
    public void assess(TransactionEvent tx) {

        if (risks.findByTransactionId(tx.transactionId()).isPresent()) {
            return;
        }

        int ruleScore = 0;
        List<String> reasons = new ArrayList<>();

        if (tx.amount() >= 50000) {
            ruleScore += 35;
            reasons.add("high-value transaction");
        }

        if (tx.amount() >= 100000) {
            ruleScore += 15;
            reasons.add("very high transaction amount");
        }

        int hour = tx.transactionTime()
                .atZone(ZoneOffset.UTC)
                .getHour();

        if (hour < 5 || hour >= 23) {
            ruleScore += 20;
            reasons.add("unusual transaction hour");
        }

        if (tx.deviceId() == null || tx.deviceId().isBlank()) {
            ruleScore += 10;
            reasons.add("missing device fingerprint");
        }

        if (tx.location() == null || tx.location().isBlank()) {
            ruleScore += 5;
            reasons.add("missing location");
        }

        int velocity = (int) transactions
                .countByUserIdAndCreatedAtAfter(
                        tx.userId(),
                        Instant.now().minusSeconds(300)
                );

        if (velocity > 5) {
            ruleScore += 15;
            reasons.add("high transaction velocity");
        }

        double anomaly = 0.0;

        try {
            var result = ml.post()
                    .uri("/score")
                    .body(Map.of(
                            "amount", tx.amount(),
                            "hour", hour,
                            "velocity", velocity
                    ))
                    .retrieve()
                    .body(MLResponse.class);

            if (result != null) {
                anomaly = result.anomalyScore();
            }
        } catch (Exception ignored) {
            reasons.add("ML service unavailable; rules-only assessment");
        }

        if (anomaly >= 0.75) {
            reasons.add("high ML anomaly signal");
        }

        int combined = (int) Math.round(
                ruleScore * 0.7 + anomaly * 100 * 0.3
        );

        combined = Math.max(0, Math.min(100, combined));

        String level =
                combined >= 70 ? "HIGH" :
                combined >= 40 ? "MEDIUM" :
                "LOW";

        String decision =
                combined >= 70 ? "BLOCK" :
                combined >= 40 ? "REVIEW" :
                "APPROVE";

        RiskAssessment assessment = new RiskAssessment();

        assessment.setTransactionId(tx.transactionId());
        assessment.setRuleScore(ruleScore);
        assessment.setAnomalyScore(anomaly);
        assessment.setRiskScore(combined);
        assessment.setRiskLevel(level);
        assessment.setDecision(decision);
        assessment.setReasons(String.join(", ", reasons));
        assessment.setEvaluatedAt(Instant.now());

        risks.save(assessment);

        transactions.findByTransactionId(tx.transactionId())
                .ifPresent(transaction -> {
                    transaction.setStatus(decision);
                    transactions.save(transaction);
                });

        String hash = "risk:" + tx.transactionId();

        try {
            redis.opsForHash().put(hash, "riskScore", String.valueOf(combined));
            redis.opsForHash().put(hash, "riskLevel", level);
            redis.opsForHash().put(hash, "decision", decision);
            redis.opsForHash().put(hash, "anomalyScore", String.valueOf(anomaly));
        } catch (Exception ignored) {
            // Redis is a cache; PostgreSQL remains the source of truth.
        }
    }

    public record MLResponse(double anomalyScore, String model) {
    }
}
