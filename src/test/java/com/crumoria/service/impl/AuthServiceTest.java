package com.crumoria.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.crumoria.dto.RegisterDto;
import com.crumoria.exception.BusinessException;
import com.crumoria.repository.UserRepository;
import com.crumoria.security.JwtTokenProvider;
import com.crumoria.service.PasswordPolicyService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock UserRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock PasswordPolicyService policy;
    @Mock AuthenticationManager authManager;
    @Mock JwtTokenProvider jwtProvider;

    @InjectMocks AuthServiceImpl service;

    @Test
    public void registerUser_whenEmailExists_throws() {
        when(repo.existsByEmail("a@b.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                     () -> service.registerUser(
                        new RegisterDto("bob", "a@b.com", "password")));
        
        assertEquals("Email already taken", ex.getMessage());
    }

    @Test
    public void registerUser_whenUsernameExists_throws() {
        when(repo.existsByUsername("bob")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                     () -> service.registerUser(
                        new RegisterDto("bob", "bob@x.com", "password")));

        assertEquals("Username already taken", ex.getMessage());
    }
}