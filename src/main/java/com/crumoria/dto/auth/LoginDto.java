package com.crumoria.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotBlank(message = "{login.username_or_email.not_blank}")
        @Size(max = 120, message = "{login.username_or_email.size}")
        String usernameOrEmail,

        @NotBlank(message = "{login.password.not_blank}")
        @Size(min = 8, max = 72, message = "{login.password.size}")
        String password
) {
    @JsonCreator
    public LoginDto(@JsonProperty("usernameOrEmail") String usernameOrEmail,
                    @JsonProperty("password")     String password) {
        this.usernameOrEmail = usernameOrEmail == null ? null : usernameOrEmail.trim();
        this.password        = password        == null ? null : password.trim();
    }
}