package com.crumoria.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.crumoria.entity.Role;
import com.crumoria.entity.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryIT {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("mypg_devev")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void pgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired
    UserRepository repo;

    @Test
    void existsByEmail_returnsTrue_whenDuplicate() {
        repo.save(User.builder()
                .username("alice")
                .email("alice@x.com")
                .password("Testing123?*!!g123?*!!")
                .roles(Set.of(Role.USER))
                .build());

        assertTrue(repo.existsByEmail("alice@x.com"));
    }
}
