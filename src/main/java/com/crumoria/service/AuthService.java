package com.crumoria.service;

import com.crumoria.dto.JwtAuthResponse;
import com.crumoria.dto.LoginDto;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.UserDto;

import jakarta.transaction.Transactional;

public interface AuthService {

    JwtAuthResponse login(LoginDto loginDto);
    
    @Transactional
    UserDto registerUser(RegisterDto registerDto);

    @Transactional
    UserDto registerAdmin(RegisterDto registerDto);
}
