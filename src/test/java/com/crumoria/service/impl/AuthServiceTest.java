package com.crumoria.service.impl;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.crumoria.entity.Role;
import com.crumoria.entity.User;
import com.crumoria.exception.BusinessException;
import com.crumoria.repository.UserRepository;
import com.crumoria.security.JwtTokenProvider;
import com.crumoria.service.PasswordPolicyService;
import com.crumoria.exception.ErrorCode;
import com.crumoria.dto.auth.JwtAuthResponse;
import com.crumoria.dto.auth.LoginDto;
import com.crumoria.dto.auth.RegisterDto;
import com.crumoria.dto.auth.UserDto;

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

    @Test
    void registerUser_happyPath_returnsUserDto() {
        RegisterDto dto = new RegisterDto("alice", "alice@x.com", "Testing123!!*?");
        User saved = User.builder()
                         .id(UUID.randomUUID())
                         .username("alice")
                         .email("alice@x.com")
                         .password("encoded")
                         .roles(Set.of(Role.USER))
                         .createdAt(LocalDateTime.now())
                         .updatedAt(LocalDateTime.now())
                         .build();

        org.mockito.Mockito.doNothing().when(policy).assertStrongPassword("Testing123!!*?");
        when(repo.existsByUsername("alice")).thenReturn(false);
        when(repo.existsByEmail("alice@x.com")).thenReturn(false);
        when(encoder.encode("Testing123!!*?")).thenReturn("encoded");
        when(repo.save(any(User.class))).thenReturn(saved);

        UserDto result = service.registerUser(dto);

        assertEquals("alice", result.getUsername());
        assertEquals("alice@x.com", result.getEmail());
    }

    @Test
    void registerUser_whenPasswordTooWeak_throws() {
        doThrow(new BusinessException(ErrorCode.WEAK_PASSWORD, "Password too weak"))
            .when(policy).assertStrongPassword("weak");

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> service.registerUser(new RegisterDto("bob", "bob@x.com", "weak")));

        assertEquals("Password too weak", ex.getMessage());
    }

    @Test
    void login_happyPath_returnsJwt() {
        LoginDto dto = new LoginDto("alice", "password");
        Authentication auth = mock(Authentication.class);

        User user = User.builder()
                        .username("alice")
                        .email("alice@x.com")
                        .roles(Set.of(Role.USER))
                        .build();

        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtProvider.generateToken(auth)).thenReturn("jwt-token");
        when(repo.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user));

        JwtAuthResponse response = service.login(dto);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("alice", response.getUsername());
        assertEquals(Set.of("USER"), response.getRoles());
    }

    @Test
    void login_whenUserNotFound_throws() {
        LoginDto dto = new LoginDto("ghost", "pw");
        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(repo.findByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> service.login(dto));

        assertEquals("User not found", ex.getMessage());
    }
}