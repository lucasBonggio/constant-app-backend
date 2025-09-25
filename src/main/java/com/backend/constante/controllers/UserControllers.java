package com.backend.constante.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.constante.dto.CreateUserDTO;
import com.backend.constante.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserControllers {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody CreateUserDTO request){
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                                .body("User has been registered correctly. Now, please sign in.");
    }
}
