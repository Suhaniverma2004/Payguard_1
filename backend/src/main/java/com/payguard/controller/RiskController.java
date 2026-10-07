package com.payguard.controller;

import com.payguard.dto.RiskAssessmentResponse;
import com.payguard.model.RiskAssessment;
import com.payguard.repository.RiskAssessmentRepository;
import com.payguard.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskAssessmentRepository riskRepository;
    private final TransactionRepository transactionRepository;

    public RiskController(
            RiskAssessmentRepository riskRepository,
            TransactionRepository transactionRepository
    ) {
        this.riskRepository = riskRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public List<RiskAssessmentResponse> latest(Authentication authentication) {
        return riskRepository
                .findLatestForUser(authentication.getName())
                .stream()
                .map(this::map)
                .toList();
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<RiskAssessmentResponse> get(
            @PathVariable String transactionId,
            Authentication authentication
    ) {
        boolean ownsTransaction = transactionRepository
                .findByTransactionIdAndUserId(
                        transactionId,
                        authentication.getName()
                )
                .isPresent();

        if (!ownsTransaction) {
            return ResponseEntity.notFound().build();
        }

        return riskRepository
                .findByTransactionId(transactionId)
                .map(x -> ResponseEntity.ok(map(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    private RiskAssessmentResponse map(RiskAssessment x) {
        return new RiskAssessmentResponse(
                x.getTransactionId(),
                x.getRuleScore(),
                x.getAnomalyScore(),
                x.getRiskScore(),
                x.getRiskLevel(),
                x.getDecision(),
                x.getReasons(),
                x.getEvaluatedAt()
        );
    }
}
