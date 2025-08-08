package com.crumoria.service.impl;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crumoria.dto.JwtAuthResponse;
import com.crumoria.dto.LoginDto;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.UserDto;
import com.crumoria.entity.Role;
import com.crumoria.entity.User;
import com.crumoria.exception.BusinessException;
import com.crumoria.exception.ErrorCode;
import com.crumoria.repository.UserRepository;
import com.crumoria.security.JwtTokenProvider;
import com.crumoria.service.AuthService;
import com.crumoria.service.PasswordPolicyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final AuthenticationManager authenticationManager;

    /* ---------- Public API ---------- */

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {

        log.info("Login attempt for {}", loginDto.usernameOrEmail());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDto.usernameOrEmail(), 
                loginDto.password())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByUsernameOrEmail(loginDto.usernameOrEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        return JwtAuthResponse.builder()
                .accessToken(token)
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }

    @Transactional
    @Override
    public UserDto registerUser(RegisterDto registerDto) {
        log.info("Registring regular user {}", registerDto.username());
        User user = createUser(registerDto, Set.of(Role.USER));
        return UserDto.from(user);
    }

    @Transactional
    @Override
    public UserDto registerAdmin(RegisterDto registerDto) {
        log.info("Registering admin {}", registerDto.username());
        User user = createUser(registerDto, Set.of(Role.ADMIN, Role.USER));
        return UserDto.from(user);
    }

    /* ---------- Private helpers ---------- */

    private User createUser(RegisterDto registerDto, Set<Role> roles) {
        passwordPolicyService.assertStrongPassword(registerDto.password());

        String email = registerDto.email().toLowerCase();

        ensureUniqueUsername(registerDto.username());
        ensureUniqueEmail(email);

        User user = User.builder()
                .username(registerDto.username())
                .email(email)
                .password(passwordEncoder.encode(registerDto.password()))
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private void ensureUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_TAKEN, "Username already taken");
        }
    }

    private void ensureUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already taken");
        }
    }

}
