package com.payguard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payguard.dto.TransactionRequest;
import com.payguard.dto.TransactionResponse;
import com.payguard.event.TransactionEvent;
import com.payguard.model.Transaction;
import com.payguard.outbox.OutboxEvent;
import com.payguard.outbox.OutboxEventRepository;
import com.payguard.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    public static final String TOPIC = "transactions";

    private final TransactionRepository repo;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public TransactionService(
            TransactionRepository repo,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager
    ) {
        this.repo = repo;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public TransactionResponse create(
            TransactionRequest request,
            String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        String normalizedKey = idempotencyKey.trim();

        Optional<Transaction> existing =
                repo.findByIdempotencyKey(normalizedKey);

        if (existing.isPresent()) {
            return toResponse(
                    existing.get(),
                    "Idempotent replay: existing transaction returned"
            );
        }

        try {
            return transactionTemplate.execute(status -> {
                String transactionId =
                        "TXN-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();

                Transaction transaction = new Transaction();
                transaction.setTransactionId(transactionId);
                transaction.setIdempotencyKey(normalizedKey);
                transaction.setUserId(request.userId());
                transaction.setAmount(request.amount());
                transaction.setCurrency(request.currency().toUpperCase());
                transaction.setMerchantId(request.merchantId());
                transaction.setMerchantCategory(request.merchantCategory());
                transaction.setLocation(request.location());
                transaction.setDeviceId(request.deviceId());
                transaction.setTransactionTime(request.transactionTime());
                transaction.setStatus("RECEIVED");
                transaction.setCreatedAt(Instant.now());

                repo.saveAndFlush(transaction);

                TransactionEvent event = new TransactionEvent(
                        transactionId,
                        request.userId(),
                        request.amount().doubleValue(),
                        request.currency().toUpperCase(),
                        request.merchantId(),
                        request.merchantCategory(),
                        request.location(),
                        request.deviceId(),
                        request.transactionTime()
                );

                String payload;
                try {
                    payload = objectMapper.writeValueAsString(event);
                } catch (JsonProcessingException exception) {
                    throw new IllegalStateException(
                            "Unable to serialize transaction event",
                            exception
                    );
                }

                outboxRepository.save(
                        new OutboxEvent(
                                "TRANSACTION_CREATED",
                                transactionId,
                                payload,
                                Instant.now()
                        )
                );

                return new TransactionResponse(
                        transactionId,
                        "RECEIVED",
                        "Transaction accepted for risk assessment"
                );
            });
        } catch (DataIntegrityViolationException exception) {
            return repo.findByIdempotencyKey(normalizedKey)
                    .map(transaction ->
                            toResponse(
                                    transaction,
                                    "Idempotent replay: existing transaction returned"
                            )
                    )
                    .orElseThrow(() -> exception);
        }
    }

    public List<Transaction> latestForUser(String userId) {
        return repo.findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Transaction> getForUser(
            String transactionId,
            String userId
    ) {
        return repo.findByTransactionIdAndUserId(transactionId, userId);
    }

    public long velocity(String userId, Instant now) {
        return repo.countByUserIdAndCreatedAtAfter(
                userId,
                now.minusSeconds(300)
        );
    }

    private TransactionResponse toResponse(
            Transaction transaction,
            String message
    ) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getStatus(),
                message
        );
    }
}
