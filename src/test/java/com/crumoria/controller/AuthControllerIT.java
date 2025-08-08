package com.crumoria.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crumoria.service.AuthService;
import com.crumoria.security.JwtTokenProvider;
import com.crumoria.security.JwtAuthenticationFilter;

@WebMvcTest(controllers = AuthController.class,
            excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
            })
@org.springframework.boot.autoconfigure.ImportAutoConfiguration(ValidationAutoConfiguration.class)
@Import({JwtTokenProvider.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIT {
    
    @Autowired private MockMvc mvc;
    @MockitoBean private AuthService service;

    @Test
    void login_missingUsername_returns400() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body contains invalid fields"))
                .andExpect(jsonPath("$.details.usernameOrEmail").value("Username or email is required"));
    }
}
