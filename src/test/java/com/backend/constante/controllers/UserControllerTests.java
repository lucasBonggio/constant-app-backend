package com.backend.constante.controllers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.backend.constante.dto.CreateUserDTO;
import com.backend.constante.repositories.UserRepository;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    @Test
    void registerUserThenReturnString(){
        CreateUserDTO newUser = new CreateUserDTO();
        newUser.setEmail("example@gmail.com");
        newUser.setPassword("password123");
        newUser.setUsername("username");
        
        ResponseEntity<String> response = testRestTemplate.postForEntity(
            "/user/register",
            newUser,
            String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo("User has been registered correctly. Now, please sign in.");
    }    

    @Test
    void registerUserButEmailRegistered(){
        //We create a user, and then the expected exception pops up.
        CreateUserDTO user = new CreateUserDTO();
        user.setEmail("example@gmail.com");
        user.setPassword("password321");
        user.setUsername("XD");

        ResponseEntity<String> firstUser = testRestTemplate.postForEntity(
            "/user/register",
            user,
            String.class
        );

        CreateUserDTO duplicateUser = new CreateUserDTO();
        duplicateUser.setEmail("example@gmail.com");
        duplicateUser.setPassword("password123");
        duplicateUser.setUsername("username");

        ResponseEntity<Map> response = testRestTemplate.postForEntity(
            "/user/register",
            duplicateUser,
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("The email has already been registered.");
    }
    
}
