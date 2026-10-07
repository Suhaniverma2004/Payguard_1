package com.payguard.controller;

import com.payguard.model.RiskAssessment;
import com.payguard.repository.RiskAssessmentRepository;
import com.payguard.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskControllerTest {

    @Mock
    private RiskAssessmentRepository riskRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RiskController controller;

    @Test
    void getReturnsNotFoundWhenTransactionBelongsToAnotherUser() {
        var authentication =
                new UsernamePasswordAuthenticationToken("alice", null);

        when(transactionRepository.findByTransactionIdAndUserId(
                "TXN-123",
                "alice"
        )).thenReturn(Optional.empty());

        var response = controller.get("TXN-123", authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        verifyNoInteractions(riskRepository);
    }

    @Test
    void getReturnsAssessmentForTransactionOwnedByUser() {
        var authentication =
                new UsernamePasswordAuthenticationToken("alice", null);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setTransactionId("TXN-123");
        assessment.setRuleScore(20);
        assessment.setAnomalyScore(0.3);
        assessment.setRiskScore(25);
        assessment.setRiskLevel("LOW");
        assessment.setDecision("ALLOW");
        assessment.setReasons("No strong risk signals");
        assessment.setEvaluatedAt(java.time.Instant.now());

        when(transactionRepository.findByTransactionIdAndUserId(
                "TXN-123",
                "alice"
        )).thenReturn(Optional.of(new com.payguard.model.Transaction()));

        when(riskRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(assessment));

        var response = controller.get("TXN-123", authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().transactionId()).isEqualTo("TXN-123");
    }
}
