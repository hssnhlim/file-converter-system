package com.crumoria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.crumoria.dto.JwtAuthResponse;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.LoginDto;
import com.crumoria.dto.UserDto;
import com.crumoria.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/email and password")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
        JwtAuthResponse response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterDto registerDto) {
        UserDto user = authService.registerUser(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}