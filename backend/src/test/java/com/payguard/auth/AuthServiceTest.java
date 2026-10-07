package com.payguard.auth;

import com.payguard.dto.AuthRequest;
import com.payguard.dto.AuthResponse;
import com.payguard.model.AppUser;
import com.payguard.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void signupHashesPasswordAndReturnsJwt() {
        AppUserRepository users = mock(AppUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);

        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode("StrongPass123")).thenReturn("bcrypt-hash");

        AppUser saved = new AppUser("alice", "bcrypt-hash", "USER");
        when(users.save(any(AppUser.class))).thenReturn(saved);
        when(jwt.generate("alice", "USER")).thenReturn("jwt-token");

        AuthService service = new AuthService(users, encoder, jwt);

        AuthResponse response = service.signup(
                new AuthRequest("alice", "StrongPass123")
        );

        assertEquals("alice", response.username());
        assertEquals("USER", response.role());
        assertEquals("jwt-token", response.token());

        verify(encoder).encode("StrongPass123");
        verify(users).save(any(AppUser.class));
        verify(jwt).generate("alice", "USER");
    }

    @Test
    void duplicateUsernameIsRejected() {
        AppUserRepository users = mock(AppUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);

        when(users.existsByUsername("alice")).thenReturn(true);

        AuthService service = new AuthService(users, encoder, jwt);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.signup(new AuthRequest("alice", "StrongPass123"))
        );

        assertEquals("Username already exists", exception.getMessage());
        verifyNoInteractions(encoder, jwt);
        verify(users, never()).save(any());
    }

    @Test
    void invalidPasswordIsRejected() {
        AppUserRepository users = mock(AppUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);

        AppUser user = new AppUser("alice", "stored-hash", "USER");

        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches("WrongPass123", "stored-hash")).thenReturn(false);

        AuthService service = new AuthService(users, encoder, jwt);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.login(new AuthRequest("alice", "WrongPass123"))
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwt, never()).generate(anyString(), anyString());
    }
}
