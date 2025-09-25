package com.backend.constante.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.backend.constante.config.JwtUtil;
import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.model.User;
import com.backend.constante.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServicesTests {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp(){
        loginRequest = new LoginRequest();
        loginRequest.setEmail("example@gmail.com");
        loginRequest.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setEmail("example@gmail.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void IShouldLogInAUser(){
        String token = "mocked.jwt.token";
        UserDTO expectedUser = new UserDTO();
        expectedUser.setUsername("username");
        expectedUser.setEmail("example@gmail.com");
        expectedUser.setToken(token);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn(token);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(expectedUser);

        UserDTO result = authService.loginUser(loginRequest);

        assertNotNull(result);
        assertEquals(expectedUser.getUsername(), result.getUsername());
        assertEquals(expectedUser.getEmail(), result.getEmail());
        assertEquals(expectedUser.getToken(), result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil).generateToken(user.getEmail());
        verify(modelMapper).map(user, UserDTO.class);
    }

    @Test
    void IShouldLoginAUserButTheCredentialsAreInvalid(){
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Invalid credentials"));             

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.loginUser(loginRequest));

        assertEquals("Invalid credentials", ex.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }
}
