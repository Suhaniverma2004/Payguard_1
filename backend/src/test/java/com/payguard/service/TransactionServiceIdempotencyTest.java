package com.payguard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payguard.model.Transaction;
import com.payguard.outbox.OutboxEventRepository;
import com.payguard.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TransactionServiceIdempotencyTest {

    @Test
    void idempotencyReplayIsScopedToTheAuthenticatedUser() {
        TransactionRepository repository = mock(TransactionRepository.class);
        OutboxEventRepository outbox = mock(OutboxEventRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN-USER1");
        transaction.setUserId("user1");
        transaction.setStatus("RECEIVED");

        when(repository.findByIdempotencyKeyAndUserId("same-key", "user1"))
                .thenReturn(Optional.of(transaction));

        TransactionService service = new TransactionService(
                repository, outbox, new ObjectMapper(), transactionManager
        );

        var request = new com.payguard.dto.TransactionRequest(
                "user1", new java.math.BigDecimal("100.00"), "USD",
                "merchant", "retail", "Bangalore", "device-1",
                java.time.Instant.now()
        );

        var response = service.create(request, "  same-key  ");

        assertEquals("TXN-USER1", response.transactionId());
        verify(repository).findByIdempotencyKeyAndUserId("same-key", "user1");
        verify(repository, never()).findByIdempotencyKey(anyString());
    }
}
