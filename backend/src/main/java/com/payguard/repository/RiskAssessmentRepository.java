package com.payguard.repository;

import com.payguard.model.RiskAssessment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {

    Optional<RiskAssessment> findByTransactionId(String transactionId);

    List<RiskAssessment> findTop50ByOrderByEvaluatedAtDesc();

    @Query("""
            select r
            from RiskAssessment r
            join Transaction t on t.transactionId = r.transactionId
            where t.userId = :userId
            order by r.evaluatedAt desc
            """)
    List<RiskAssessment> findLatestForUser(
            @Param("userId") String userId,
            Pageable pageable
    );
}
