package com.payguard.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
public record TransactionRequest(
    @NotBlank String userId,
    @NotNull @Positive BigDecimal amount,
    @NotBlank @Size(min=3,max=3) String currency,
    String merchantId, String merchantCategory, String location, String deviceId,
    @NotNull Instant transactionTime
) {}
