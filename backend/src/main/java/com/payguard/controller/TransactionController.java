package com.payguard.controller;
import com.payguard.dto.*; import com.payguard.model.Transaction; import com.payguard.service.TransactionService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/transactions")
public class TransactionController {
 private final TransactionService service; public TransactionController(TransactionService service){this.service=service;}
 @PostMapping public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest r){return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.create(r));}
 @GetMapping public List<Transaction> latest(){return service.latest();}
 @GetMapping("/{id}") public ResponseEntity<Transaction> get(@PathVariable String id){return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("service","transaction-service","status","UP");}
}
