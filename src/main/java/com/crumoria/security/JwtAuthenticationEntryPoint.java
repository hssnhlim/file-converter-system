package com.crumoria.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * This method is invoked when an exception is thrown due to an unauthenticated user trying to access a secured REST endpoint.
     * It sends a 401 Unauthorized response with the exception message.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }

}
