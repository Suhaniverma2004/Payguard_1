package com.payguard.controller;

import com.payguard.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/admin")
public class AdminController {
    private final TransactionRepository transactions; private final RiskAssessmentRepository risks;
    public AdminController(TransactionRepository transactions,RiskAssessmentRepository risks){this.transactions=transactions;this.risks=risks;}
    @GetMapping("/metrics") @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Object> metrics(){return Map.of("transactions",transactions.count(),"riskAssessments",risks.count());}
}
