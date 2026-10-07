package com.payguard.repository;

import com.payguard.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByTransactionIdAndUserId(
            String transactionId,
            String userId
    );

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByIdempotencyKeyAndUserId(
            String idempotencyKey,
            String userId
    );

    List<Transaction> findTop50ByOrderByCreatedAtDesc();

    List<Transaction> findTop50ByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndCreatedAtAfter(
            String userId,
            Instant createdAt
    );
}
