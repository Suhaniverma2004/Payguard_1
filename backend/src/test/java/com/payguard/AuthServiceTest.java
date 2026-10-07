package com.payguard;

import com.payguard.auth.AuthService;
import com.payguard.auth.JwtService;
import com.payguard.dto.AuthRequest;
import com.payguard.model.AppUser;
import com.payguard.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test
    void signupHashesPasswordAndReturnsJwt(){
        var users=mock(AppUserRepository.class); var encoder=new BCryptPasswordEncoder(); var jwt=mock(JwtService.class);
        when(users.existsByUsername("alice")).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(i->i.getArgument(0));
        when(jwt.generate("alice","USER")).thenReturn("token");
        var service=new AuthService(users,encoder,jwt);
        var result=service.signup(new AuthRequest("alice","password123"));
        assertEquals("token",result.token()); assertEquals("alice",result.username());
        verify(users).save(argThat(u -> u.getUsername().equals("alice") && !u.getPasswordHash().equals("password123")));
    }

    @Test
    void loginRejectsWrongPassword(){
        var users=mock(AppUserRepository.class); var encoder=new BCryptPasswordEncoder(); var jwt=mock(JwtService.class);
        when(users.findByUsername("alice")).thenReturn(Optional.of(new AppUser("alice",encoder.encode("correct"),"USER")));
        var service=new AuthService(users,encoder,jwt);
        assertThrows(IllegalArgumentException.class,()->service.login(new AuthRequest("alice","wrong")));
    }
}
