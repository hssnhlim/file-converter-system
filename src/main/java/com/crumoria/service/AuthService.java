package com.crumoria.service;

import com.crumoria.dto.auth.JwtAuthResponse;
import com.crumoria.dto.auth.LoginDto;
import com.crumoria.dto.auth.RegisterDto;
import com.crumoria.dto.auth.UserDto;

import jakarta.transaction.Transactional;

public interface AuthService {

    JwtAuthResponse login(LoginDto loginDto);
    
    @Transactional
    UserDto registerUser(RegisterDto registerDto);

    @Transactional
    UserDto registerAdmin(RegisterDto registerDto);
}
