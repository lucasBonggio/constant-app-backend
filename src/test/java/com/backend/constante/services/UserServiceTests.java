package com.backend.constante.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.constante.dto.CreateUserDTO;
import com.backend.constante.exception.BusinessRuleException;
import com.backend.constante.model.User;
import com.backend.constante.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    private CreateUserDTO newUser;
    @BeforeEach
    void setUp(){
        newUser = new CreateUserDTO();
        newUser.setEmail("example@gmail.com");
        newUser.setUsername("username");
        newUser.setPassword("password");
    }

    @Test
    void IShouldCreateAUser(){
        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(newUser.getPassword())).thenReturn("passwordEncrypted");

        userService.registerUser(newUser);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void IShouldCreateAUserButTheEmailExists(){
        User user = new User();
        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.of(user));

        BusinessRuleException ex= assertThrows(BusinessRuleException.class, () -> userService.registerUser(newUser));
        assertEquals("The email has already been registered.", ex.getMessage());
    }
}
