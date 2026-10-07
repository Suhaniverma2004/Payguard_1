package com.payguard.fraud;

import com.payguard.event.TransactionEvent;
import com.payguard.model.RiskAssessment;
import com.payguard.repository.RiskAssessmentRepository;
import com.payguard.repository.TransactionRepository;
import com.payguard.model.Transaction;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.*;
import java.util.*;

@Service
public class FraudRiskService {
    private final StringRedisTemplate redis; private final RiskAssessmentRepository risks; private final TransactionRepository transactions; private final RestClient ml;
    public FraudRiskService(StringRedisTemplate redis,RiskAssessmentRepository risks,TransactionRepository transactions,RestClient.Builder builder){this.redis=redis;this.risks=risks;this.transactions=transactions;this.ml=builder.baseUrl(System.getenv().getOrDefault("ML_SERVICE_URL","http://localhost:8000")).build();}

    @KafkaListener(topics="transactions",groupId="payguard-risk-engine")
    public void assess(TransactionEvent tx){
        if(risks.findByTransactionId(tx.transactionId()).isPresent()) return;
        int ruleScore=0; List<String> reasons=new ArrayList<>();
        if(tx.amount()>=50000){ruleScore+=35;reasons.add("high-value transaction");}
        if(tx.amount()>=100000){ruleScore+=15;reasons.add("very high transaction amount");}
        int hour=tx.transactionTime().atZone(ZoneOffset.UTC).getHour();
        if(hour<5 || hour>=23){ruleScore+=20;reasons.add("unusual transaction hour");}
        if(tx.deviceId()==null || tx.deviceId().isBlank()){ruleScore+=10;reasons.add("missing device fingerprint");}
        if(tx.location()==null || tx.location().isBlank()){ruleScore+=5;reasons.add("missing location");}
        int velocity=(int)transactions.countByUserIdAndCreatedAtAfter(tx.userId(),Instant.now().minusSeconds(300));
        double anomaly=0.0;
        try{var result=ml.post().uri("/score").body(Map.of("amount",tx.amount(),"hour",hour,"velocity",velocity)).retrieve().body(MLResponse.class); if(result!=null) anomaly=result.anomalyScore();}catch(Exception ignored){}
        int combined=(int)Math.round(ruleScore*0.7+anomaly*100*0.3); combined=Math.max(0,Math.min(100,combined));
        String level=combined>=70?"HIGH":combined>=40?"MEDIUM":"LOW"; String decision=combined>=70?"BLOCK":combined>=40?"REVIEW":"APPROVE";
        RiskAssessment r=new RiskAssessment(); r.setTransactionId(tx.transactionId());r.setRuleScore(ruleScore);r.setAnomalyScore(anomaly);r.setRiskScore(combined);r.setRiskLevel(level);r.setDecision(decision);r.setReasons(String.join(", ",reasons));r.setEvaluatedAt(Instant.now());risks.save(r);
        transactions.findByTransactionId(tx.transactionId()).ifPresent(t->{t.setStatus(decision); transactions.save(t);});
        var hash="risk:"+tx.transactionId();redis.opsForHash().put(hash,"riskScore",String.valueOf(combined));redis.opsForHash().put(hash,"riskLevel",level);redis.opsForHash().put(hash,"decision",decision);redis.opsForHash().put(hash,"anomalyScore",String.valueOf(anomaly));
    }
    public record MLResponse(double anomalyScore,String model){}
}
