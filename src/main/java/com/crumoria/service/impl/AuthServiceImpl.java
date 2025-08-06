package com.crumoria.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crumoria.dto.LoginDto;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.UserDto;
import com.crumoria.entity.Role;
import com.crumoria.entity.User;
import com.crumoria.repository.UserRepository;
import com.crumoria.security.JwtTokenProvider;
import com.crumoria.service.AuthService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Override
    public String login(LoginDto loginDto) {

        log.info("Login attempt for {}", loginDto.getUsernameOrEmail());
        
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginDto.getUsernameOrEmail(), loginDto.getPassword()
            ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional
    @Override
    public UserDto registerUser(RegisterDto registerDto) {

        log.info("Registration attempt for username={}", registerDto.getUsername());
        
        // Check if username or email already exists
        Optional<User> existingUserEmail = userRepository.findByUsernameOrEmail(registerDto.getEmail());
        if (existingUserEmail.isPresent()) {
            log.warn("Email already taken: {}", registerDto.getEmail());
            throw new RuntimeException("Username or email already exists!");
        }

        Optional<User> existingUsername = userRepository.findByUsernameOrEmail(registerDto.getUsername());
        if (existingUsername.isPresent()) {
            log.warn("Username already taken: {}", registerDto.getUsername());
            throw new RuntimeException("Username or email already exists!");
        }

        // Create a new user
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign default role using enum
        user.setRoles(Collections.singleton(Role.USER));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(null);

        // Save the user
        user = userRepository.save(user);
        log.info("User registered with id={}", user.getId());

        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    @Override
    public String registerAdmin(RegisterDto registerDto) {
        
        // Check if username or email already exists
        Optional<User> existingUserEmail = userRepository.findByUsernameOrEmail(registerDto.getEmail());
        if (existingUserEmail.isPresent()) {
            throw new RuntimeException("Username or email already exists!");
        }

        Optional<User> existingUsername = userRepository.findByUsernameOrEmail(registerDto.getUsername());
        if (existingUsername.isPresent()) {
            throw new RuntimeException("Username or email already exists!");
        }

        // Create a new user
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign admin and user role using enum
        user.setRoles(new HashSet<>(Arrays.asList(Role.ADMIN, Role.USER)));

        // Save the user
        userRepository.save(user);

        return "Admin registered successfully!";
    }

}
