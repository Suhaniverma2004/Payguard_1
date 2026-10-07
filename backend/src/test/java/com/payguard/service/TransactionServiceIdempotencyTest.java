package com.payguard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payguard.dto.TransactionRequest;
import com.payguard.model.Transaction;
import com.payguard.outbox.OutboxEventRepository;
import com.payguard.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceIdempotencyTest {

    private TransactionService service(
            TransactionRepository repository,
            OutboxEventRepository outbox,
            PlatformTransactionManager transactionManager
    ) {
        return new TransactionService(
                repository,
                outbox,
                new ObjectMapper(),
                transactionManager
        );
    }

    private TransactionRequest request(String userId) {
        return new TransactionRequest(
                userId,
                new BigDecimal("100.00"),
                "USD",
                "merchant",
                "retail",
                "Bangalore",
                "device-1",
                Instant.now()
        );
    }

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

        TransactionService service = service(repository, outbox, transactionManager);

        var response = service.create(request("user1"), "  same-key  ");

        assertEquals("TXN-USER1", response.transactionId());
        assertEquals("RECEIVED", response.status());

        verify(repository).findByIdempotencyKeyAndUserId("same-key", "user1");
        verify(repository, never()).findByIdempotencyKey(anyString());
        verifyNoInteractions(outbox);
    }

    @Test
    void sameKeyForAnotherUserDoesNotUseGlobalReplayLookup() {
        TransactionRepository repository = mock(TransactionRepository.class);
        OutboxEventRepository outbox = mock(OutboxEventRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

        when(repository.findByIdempotencyKeyAndUserId("shared-key", "user2"))
                .thenReturn(Optional.empty());

        TransactionService service = service(repository, outbox, transactionManager);

        assertDoesNotThrow(() -> {
            try {
                service.create(request("user2"), "shared-key");
            } catch (RuntimeException ignored) {
                // The mocked transaction infrastructure cannot complete persistence;
                // the assertion below verifies the lookup boundary that matters here.
            }
        });

        verify(repository).findByIdempotencyKeyAndUserId("shared-key", "user2");
        verify(repository, never()).findByIdempotencyKey("shared-key");
    }

    @Test
    void latestTransactionsAreRequestedOnlyForTheAuthenticatedUser() {
        TransactionRepository repository = mock(TransactionRepository.class);
        OutboxEventRepository outbox = mock(OutboxEventRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

        TransactionService service = service(repository, outbox, transactionManager);

        service.latestForUser("user2");

        verify(repository).findTop50ByUserIdOrderByCreatedAtDesc("user2");
        verify(repository, never()).findTop50ByOrderByCreatedAtDesc();
    }
}
