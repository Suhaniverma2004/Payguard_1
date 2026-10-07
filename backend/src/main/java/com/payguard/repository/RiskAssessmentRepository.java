package com.payguard.repository;

import com.payguard.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findByTransactionId(String transactionId);
    List<RiskAssessment> findTop50ByOrderByEvaluatedAtDesc();
}
