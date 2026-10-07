package com.payguard.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,Object>> badRequest(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"error",e.getMessage()));
    }
}
