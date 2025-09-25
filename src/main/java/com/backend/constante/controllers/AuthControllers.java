package com.backend.constante.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.constante.dto.LoginRequest;
import com.backend.constante.dto.UserDTO;
import com.backend.constante.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllers {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginRequest request){
        UserDTO user = authService.loginUser(request);

        return ResponseEntity.ok(user);
    }
}
