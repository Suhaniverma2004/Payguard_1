package com.payguard.auth;

import com.payguard.dto.*;
import com.payguard.model.AppUser;
import com.payguard.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    @Transactional
    public AuthResponse signup(AuthRequest r){
        if(users.existsByUsername(r.username())) throw new IllegalArgumentException("Username already exists");
        AppUser user=users.save(new AppUser(r.username(),encoder.encode(r.password()),"USER"));
        return new AuthResponse(jwt.generate(user.getUsername(),user.getRole()),user.getUsername(),user.getRole());
    }
    public AuthResponse login(AuthRequest r){
        AppUser user=users.findByUsername(r.username()).orElseThrow(()->new IllegalArgumentException("Invalid credentials"));
        if(!encoder.matches(r.password(),user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return new AuthResponse(jwt.generate(user.getUsername(),user.getRole()),user.getUsername(),user.getRole());
    }
}
