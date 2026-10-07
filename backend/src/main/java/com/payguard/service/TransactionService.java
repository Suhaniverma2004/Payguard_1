package com.payguard.service;

import com.payguard.dto.*; import com.payguard.model.Transaction; import com.payguard.repository.TransactionRepository;
import org.springframework.kafka.core.KafkaTemplate; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.*;

@Service
public class TransactionService {
    public static final String TOPIC="transactions";
    private final TransactionRepository repo; private final KafkaTemplate<String, TransactionEvent> kafka;
    public TransactionService(TransactionRepository repo, KafkaTemplate<String, TransactionEvent> kafka){this.repo=repo;this.kafka=kafka;}
    @Transactional
    public TransactionResponse create(TransactionRequest r){
        String id="TXN-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
        Transaction t=new Transaction(); t.setTransactionId(id); t.setUserId(r.userId()); t.setAmount(r.amount()); t.setCurrency(r.currency().toUpperCase());
        t.setMerchantId(r.merchantId()); t.setMerchantCategory(r.merchantCategory()); t.setLocation(r.location()); t.setDeviceId(r.deviceId()); t.setTransactionTime(r.transactionTime()); t.setStatus("RECEIVED"); t.setCreatedAt(Instant.now());
        repo.save(t); kafka.send(TOPIC,id,new TransactionEvent(id,r.userId(),r.amount().doubleValue(),r.currency(),r.merchantId(),r.merchantCategory(),r.location(),r.deviceId(),r.transactionTime()));
        return new TransactionResponse(id,"RECEIVED","Transaction accepted for risk assessment");
    }
    public List<Transaction> latest(){return repo.findTop50ByOrderByCreatedAtDesc();}
    public Optional<Transaction> get(String id){return repo.findByTransactionId(id);}
    public record TransactionEvent(String transactionId,String userId,double amount,String currency,String merchantId,String merchantCategory,String location,String deviceId,Instant transactionTime) {}
}
