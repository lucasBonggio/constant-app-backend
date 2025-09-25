package com.backend.constante.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.RecordDTO;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.model.Habit;
import com.backend.constante.model.Record;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.RecordRepository;
import com.backend.constante.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecordControllerTests {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String EMAIL = "example@gmail.com";
    private final String PASSWORD = "password123";
    private final String JWT_INVALID = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSJ9.wrongsignature";
    private String TOKEN;
    private Habit savedHabit;
    private User savedUser;

    @BeforeEach
    void setUp(){
        habitRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(EMAIL);
        savedUser = userRepository.save(user);

        Habit habit = new Habit();
        habit.setName("Play electric guitar");
        habit.setUser(user);

        savedHabit = habitRepository.save(habit);
        
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        ResponseEntity<UserDTO> response = testRestTemplate.postForEntity("/auth/login", request, UserDTO.class);

        TOKEN = response.getBody().getToken();

        Record record = new Record();
        record.setCompleted(true);
        record.setDate(LocalDate.now());
        record.setHabit(savedHabit);
        record.setUser(user);

        recordRepository.save(record);
        
        Record record1 = new Record();
        record1.setCompleted(true);
        record1.setDate(LocalDate.now().minusDays(1));
        record1.setHabit(savedHabit);
        record1.setUser(user);

        recordRepository.save(record1);
    }

    @Test
    void saveRecordAndReturnDTO(){
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
    
        ResponseEntity<RecordDTO> response = testRestTemplate.exchange(
            "/records/" +  savedHabit.getId(),
            HttpMethod.POST,
            new HttpEntity<>(headers),
            RecordDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getHabitId()).isEqualTo(savedHabit.getId());
        assertThat(response.getBody().getCompleted()).isTrue();
    }

    @Test
    void saveRecordButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<Map> response = testRestTemplate.exchange(
            "/records/" + savedHabit.getId(),
            HttpMethod.POST,
            new HttpEntity<>(headers),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Invalid or missing token");
    }

    @Test
    void savedRecordButReturnNOTFOUND(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);

        ResponseEntity<Map> response = testRestTemplate.exchange(
            "/records/2",
            HttpMethod.POST,
            new HttpEntity<>(headers),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Habit with id: '2' not found.");
    }

    @Test
    void findRecordByHabitIdReturnPagedModel() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);

        ResponseEntity<String> response = testRestTemplate.exchange(
            "/records/" + savedHabit.getId() + "?page=0&size=10",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());
        System.out.println("CONTENIDO: " + root); 
        JsonNode records = root.get("_embedded").get("recordDTOList");

        assertThat(records.isArray()).isTrue();
        assertThat(records.size()).isEqualTo(2);
        assertThat(records.get(0).get("completed").isBoolean()).isTrue();
        assertThat(records.get(1).get("completed").isBoolean()).isTrue();
    }

    @Test
    void findRecordByHabitButReturnUNAUTHORIZED(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<Map> response = testRestTemplate.exchange(
            "/records/1?page=0&size=10",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Invalid or missing token");
    }

    @Test
    void findRecordByDateReturnListRecordDTO() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);

        ResponseEntity<List<RecordDTO>> response = testRestTemplate.exchange(
            "/records?date=2025-09-18",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<RecordDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(2);

        assertThat(response.getBody().get(0).getCompleted()).isTrue();
        assertThat(response.getBody().get(1).getCompleted()).isTrue();
    }

    @Test
    void findRecordByDateButReturnUNAUTHORIZED(){
                HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JWT_INVALID);

        ResponseEntity<Map> response = testRestTemplate.exchange(
            "/records?date=2025-09-18",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Invalid or missing token");
    }
}
