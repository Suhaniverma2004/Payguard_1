package com.payguard.repository;
import com.payguard.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findTop50ByOrderByCreatedAtDesc();
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    long countByUserIdAndCreatedAtAfter(String userId, java.time.Instant after);
}
