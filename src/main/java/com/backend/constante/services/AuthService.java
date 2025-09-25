package com.backend.constante.services;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.backend.constante.config.JwtUtil;
import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.model.User;
import com.backend.constante.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
    * Logs a user in by authenticating credentials and generating a JWT token.
    * This method delegates authentication to Spring Security via {@link AuthenticationManager}.
    * If the credentials are invalid, it throws an exception (e.g., {@link BadCredentialsException})
    * which is handled globally by a {@link GlobalExceptionHandler}.
    *
    * @param request with email and password.
    * @return UserDTO with user data and the JWT token.
    */
    public UserDTO loginUser(LoginRequest request){
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();

        String token = jwtUtil.generateToken(user.getEmail());

        UserDTO userMapped = modelMapper.map(user, UserDTO.class);
        userMapped.setToken(token);

        return userMapped;
    }
}
