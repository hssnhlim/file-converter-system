package com.crumoria.dto.auth;

import java.time.LocalDateTime;
import java.util.Set;

import com.crumoria.entity.Role;
import com.crumoria.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private final String username;
    private final String email;
    private final Set<Role> roles;
    private final LocalDateTime createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                      .username(user.getUsername())
                      .email(user.getEmail())
                      .roles(user.getRoles())
                      .createdAt(user.getCreatedAt())
                      .build();
    }
}
