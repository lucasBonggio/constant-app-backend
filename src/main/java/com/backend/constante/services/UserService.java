package com.backend.constante.services;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.constante.dto.CreateUserDTO;
import com.backend.constante.exception.BusinessRuleException;
import com.backend.constante.model.User;
import com.backend.constante.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(CreateUserDTO newUser){
        if(userRepository.findByEmail(newUser.getEmail()).isPresent()){
            throw new BusinessRuleException("The email has already been registered.");
        }

        User user = new User();
        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail()); 
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));

        userRepository.save(user);
    } 
    
    public boolean findByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    @Override 
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")) 
        );
    }
}
