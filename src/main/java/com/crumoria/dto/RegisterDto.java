package com.crumoria.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern.Flag;

public record RegisterDto(
        @NotBlank(message = "{register.username.not_blank}")
        @Size(min = 3, max = 30, message = "{register.username.size}")
        String username,

        @Email(flags = Flag.CASE_INSENSITIVE, message = "{register.email.invalid}")
        @NotBlank(message = "{register.email.not_blank}")
        @Size(max = 254, message = "{register.email.size}")
        String email,

        @NotBlank(message = "{register.password.not_blank}")
        @Size(min = 8, max = 72, message = "{register.password.size}")
        String password
) {
    @JsonCreator
    public RegisterDto(@JsonProperty("username") String username,
                       @JsonProperty("email")    String email,
                       @JsonProperty("password") String password) {
        this.username = username == null ? null : username.trim();
        this.email    = email    == null ? null : email.trim().toLowerCase();
        this.password = password == null ? null : password.trim();
    }
}