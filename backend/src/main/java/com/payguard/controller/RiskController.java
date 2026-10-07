package com.payguard.controller;

import com.payguard.dto.RiskAssessmentResponse;
import com.payguard.model.RiskAssessment;
import com.payguard.repository.RiskAssessmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/risk")
public class RiskController {
 private final RiskAssessmentRepository repo;
 public RiskController(RiskAssessmentRepository repo){this.repo=repo;}
 @GetMapping public List<RiskAssessmentResponse> latest(){return repo.findTop50ByOrderByEvaluatedAtDesc().stream().map(this::map).toList();}
 @GetMapping("/{transactionId}") public ResponseEntity<RiskAssessmentResponse> get(@PathVariable String transactionId){return repo.findByTransactionId(transactionId).map(x->ResponseEntity.ok(map(x))).orElse(ResponseEntity.notFound().build());}
 private RiskAssessmentResponse map(RiskAssessment x){return new RiskAssessmentResponse(x.getTransactionId(),x.getRuleScore(),x.getAnomalyScore(),x.getRiskScore(),x.getRiskLevel(),x.getDecision(),x.getReasons(),x.getEvaluatedAt());}
}
