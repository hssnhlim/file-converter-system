package com.crumoria.dto.auth;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtAuthResponse {

    private final String accessToken;
    private final String tokentype = "Bearer ";
    private final String username;
    private final Set<String> roles;
}
