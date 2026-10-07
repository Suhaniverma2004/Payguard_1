package com.payguard.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="transactions", indexes={@Index(name="idx_transaction_user", columnList="userId"), @Index(name="idx_transaction_status", columnList="status")})
public class Transaction {
    @Id @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;
    @Column(nullable=false, unique=true) private String transactionId;
    @Column(unique=true) private String idempotencyKey;
    @Column(nullable=false) private String userId;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal amount;
    @Column(nullable=false, length=3) private String currency;
    private String merchantId;
    private String merchantCategory;
    private String location;
    private String deviceId;
    @Column(nullable=false) private Instant transactionTime;
    @Column(nullable=false) private String status;
    @Column(nullable=false) private Instant createdAt;

    public Transaction() {}
    public UUID getId(){return id;} public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;} public String getTransactionId(){return transactionId;} public void setTransactionId(String v){transactionId=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;} public String getMerchantId(){return merchantId;} public void setMerchantId(String v){merchantId=v;}
    public String getMerchantCategory(){return merchantCategory;} public void setMerchantCategory(String v){merchantCategory=v;} public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getDeviceId(){return deviceId;} public void setDeviceId(String v){deviceId=v;} public Instant getTransactionTime(){return transactionTime;} public void setTransactionTime(Instant v){transactionTime=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
}
