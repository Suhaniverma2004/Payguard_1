package com.payguard.fraud;

import com.payguard.service.TransactionService.TransactionEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.ZoneOffset;

@Service
public class FraudRiskService {
    private final StringRedisTemplate redis;
    public FraudRiskService(StringRedisTemplate redis) { this.redis = redis; }

    @KafkaListener(topics="transactions", groupId="payguard-risk-engine")
    public void assess(TransactionEvent tx) {
        int score = 0;
        if (tx.amount() >= 50000) score += 35;
        if (tx.amount() >= 100000) score += 15;
        int hour = tx.transactionTime().atZone(ZoneOffset.UTC).getHour();
        if (hour < 5 || hour >= 23) score += 20;
        if (tx.deviceId() == null || tx.deviceId().isBlank()) score += 10;
        if (tx.location() == null || tx.location().isBlank()) score += 5;
        String level = score >= 70 ? "HIGH" : score >= 40 ? "MEDIUM" : "LOW";
        String decision = score >= 70 ? "BLOCK" : score >= 40 ? "REVIEW" : "APPROVE";
        redis.opsForHash().put("risk:" + tx.transactionId(), "riskScore", String.valueOf(score));
        redis.opsForHash().put("risk:" + tx.transactionId(), "riskLevel", level);
        redis.opsForHash().put("risk:" + tx.transactionId(), "decision", decision);
    }
}
