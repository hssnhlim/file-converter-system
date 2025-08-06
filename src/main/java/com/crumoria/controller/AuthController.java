package com.crumoria.controller;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.crumoria.dto.JwtAuthResponse;
import com.crumoria.dto.LoginDto;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.UserDto;
import com.crumoria.entity.Role;
import com.crumoria.entity.User;
import com.crumoria.repository.UserRepository;
import com.crumoria.service.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        
        String token = authService.login(loginDto);

        // Fetch the user details to include roles in the response
        User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setUsername(user.getUsername());
        jwtAuthResponse.setRoles(
            user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet())
        );

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterDto registerDto) {
        
        UserDto response = authService.registerUser(registerDto);

        return ResponseEntity.ok(response);
    }
}
