package com.payguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotBlank
        @Size(max = 100)
        String userId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @Size(max = 100)
        String merchantId,

        @Size(max = 100)
        String merchantCategory,

        @Size(max = 150)
        String location,

        @Size(max = 200)
        String deviceId,

        @NotNull
        Instant transactionTime
) {
}
