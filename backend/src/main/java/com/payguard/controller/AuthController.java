package com.payguard.controller;

import com.payguard.auth.AuthService;
import com.payguard.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/signup") public ResponseEntity<AuthResponse> signup(@Valid @RequestBody AuthRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.signup(r));}
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody AuthRequest r){return service.login(r);}
}
