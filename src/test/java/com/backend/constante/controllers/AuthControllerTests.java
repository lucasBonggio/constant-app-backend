package com.backend.constante.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.model.User;
import com.backend.constante.repositories.UserRepository;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private static final String EMAIL = "example@gmail.com";
    private static final String PASSWORD = "password123";

    private String loginAndGetToken() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        ResponseEntity<UserDTO> response = testRestTemplate.postForEntity("/auth/login", request, UserDTO.class);
        return response.getBody().getToken();
    }

    private User user;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();

        user = new User();
        user.setEmail(EMAIL);
        user.setUsername("username");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        userRepository.save(user);
    }

    @Test
    void loginUserThenReturnToken(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> response = testRestTemplate.postForEntity(
                                                            "/auth/login",
                                                            request,
                                                            UserDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("example@gmail.com");
    }

    @Test
    void loginUserButBadCredentials(){
        LoginRequest request = new LoginRequest("notFound@gmail.com", PASSWORD);

        ResponseEntity<String> response = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
