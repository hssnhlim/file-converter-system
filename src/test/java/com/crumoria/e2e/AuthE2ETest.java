package com.crumoria.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.crumoria.dto.JwtAuthResponse;
import com.crumoria.dto.LoginDto;
import com.crumoria.dto.RegisterDto;
import com.crumoria.dto.UserDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthE2ETest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("mypg_dev")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void pgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void registerThenLogin_flowWorks() {
        RegisterDto dto = new RegisterDto("bob", "bob@x.com", "Testing123?*!!");
        ResponseEntity<?> reg = rest.postForEntity("/api/v1/auth/register",
                dto, UserDto.class);

        assertEquals(HttpStatus.CREATED, reg.getStatusCode());

        LoginDto login = new LoginDto("bob@x.com", "Testing123?*!!");
        ResponseEntity<JwtAuthResponse> loginResp = rest.postForEntity("/api/v1/auth/login", login,
                JwtAuthResponse.class);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertNotNull(loginResp.getBody().getAccessToken());
    }
}
