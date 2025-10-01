package com.backend.constante.controllers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.constante.dto.HabitDTO;
import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.model.Habit;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.UserRepository;
import com.backend.constante.utils.Frequency;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HabitControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    private final String EMAIL = "example@gmail.com";
    private final String PASSWORD = "pssword321";
    private final String JWT_INVALID = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSJ9.wrongsignature";
    private Habit savedHabit;



    @BeforeEach
    void setUp(){

        habitRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setUsername("username");
        
        userRepository.save(user);
        
        User user2 = new User();
        user2.setEmail("metal@gmail.com");
        user2.setPassword(passwordEncoder.encode("PASSWORD"));

        userRepository.save(user2);

        Habit habit = new Habit();
        habit.setName("Play guitar");
        habit.setUser(user);

        savedHabit = habitRepository.save(habit);

        Habit habit2 = new Habit();
        habit2.setName("Reading");
        habit2.setUser(user);

        habitRepository.save(habit2);
    }

    @Test
    void createHabitThenReturnDTO(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        HabitDTO newHabit = new HabitDTO();
        newHabit.setName("Play guitar");
        newHabit.setFrequency(Frequency.daily);

        ResponseEntity<HabitDTO> response = testRestTemplate.exchange(
            "/habits",
            HttpMethod.POST,
            new HttpEntity<>(newHabit, headers),
            HabitDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Play guitar");
    }

    @Test
    void createHabitButUserNotFound(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        HabitDTO newHabit = new HabitDTO();
        newHabit.setName("Play guitar"); 

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits",
            HttpMethod.POST,
            new HttpEntity<>(newHabit, headers),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void findHabitsByUser(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<List<HabitDTO>> response = testRestTemplate.exchange(
            "/habits", 
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<HabitDTO>>(){}
            );
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Play guitar");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Reading");

    }

    @Test
    void findHabitsByUserButReturnEmptyList(){
        LoginRequest request = new LoginRequest("metal@gmail.com", "PASSWORD");

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();  

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<List<HabitDTO>> response = testRestTemplate.exchange(
            "/habits",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<HabitDTO>>(){}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void findHabitsByUserButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void findHabitByIdReturnHabitDTO(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<HabitDTO> response = testRestTemplate.exchange(
            "/habits/" + savedHabit.getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            HabitDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Play guitar");
    }

    @Test
    void findHabitByIdButHabitNotFound(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/3",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findHabitsByIdButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateHabitSuccessfullyReturnString(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HabitDTO habit = new HabitDTO();
        habit.setName("Play electric guitar");

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/" + savedHabit.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(habit, headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Habit has been updated correctly.");
    }

    @Test
    void updateHabitButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        HabitDTO habit = new HabitDTO();
        habit.setName("Play electric guitar");


        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/1",
            HttpMethod.PUT,
            new HttpEntity<>(habit, headers),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    
    @Test
    void updateHabitButReturnNOTFOUND(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HabitDTO habit = new HabitDTO();
        habit.setName("Play electric guitar");

        HttpHeaders headers  = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/4",
            HttpMethod.PUT,
            new HttpEntity<>(habit, headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteHabitSuccessfullyReturnString(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/" + savedHabit.getId(),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Habit has been deleted correctly.");
    }

    @Test
    void deleteHabitButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/1",
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

        @Test
    void deleteHabitButReturnNOTFOUND(){
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        ResponseEntity<UserDTO> loginResponse = testRestTemplate.postForEntity(
            "/auth/login",
            request,
            UserDTO.class);
        String token = loginResponse.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        ResponseEntity<String> response = testRestTemplate.exchange(
            "/habits/4",
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
