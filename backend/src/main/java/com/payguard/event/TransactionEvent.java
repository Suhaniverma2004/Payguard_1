package com.payguard.event;

import java.time.Instant;

public record TransactionEvent(
        String transactionId,
        String userId,
        double amount,
        String currency,
        String merchantId,
        String merchantCategory,
        String location,
        String deviceId,
        Instant transactionTime
) {}
