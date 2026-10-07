package com.payguard.controller;

import com.payguard.dto.TransactionRequest;
import com.payguard.dto.TransactionResponse;
import com.payguard.model.Transaction;
import com.payguard.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        if (!authentication.getName().equals(request.userId())) {
            throw new IllegalArgumentException(
                    "Transaction userId must match the authenticated user"
            );
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(service.create(request, key));
    }

    @GetMapping
    public List<Transaction> latest(Authentication authentication) {
        return service.latestForUser(authentication.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> get(
            @PathVariable String id,
            Authentication authentication
    ) {
        return service.getForUser(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "transaction-service",
                "status", "UP"
        );
    }
}
