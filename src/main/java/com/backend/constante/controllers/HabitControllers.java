package com.backend.constante.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.constante.dto.HabitDTO;
import com.backend.constante.services.HabitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/habits")
@RequiredArgsConstructor
public class HabitControllers {
    
    private final HabitService habitService;

    @PostMapping
    public ResponseEntity<HabitDTO> createHabit(@Valid @RequestBody HabitDTO habit, Authentication authentication){
        String email = authentication.getName();
        HabitDTO newHabit = habitService.createHabit(habit, email);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                            .body(newHabit);
    }

    @GetMapping
    public ResponseEntity<List<HabitDTO>> findByUser(Authentication authentication){
        String email = authentication.getName();
        List<HabitDTO> habits = habitService.findHabitsByUser(email);
        
        return ResponseEntity.ok(habits); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitDTO> findById(@PathVariable Long habitId, Authentication authentication){
        String email = authentication.getName();
        HabitDTO habit = habitService.findHabitById(habitId, email);

        return ResponseEntity.ok(habit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateHabit(@Valid @RequestBody HabitDTO habit,
                                                @PathVariable Long habitId,
                                                Authentication authentication){
        String email = authentication.getName();
        habitService.updateHabit(email, habitId, habit);

        return ResponseEntity.ok("Habit has been updated correctly.");
    }
    
    @DeleteMapping("/{habitId}")
    public ResponseEntity<String> deleteHabit(@PathVariable Long habitId,
                                            Authentication authentication){

        String email = authentication.getName();
        habitService.deleteHabit(email, habitId);
        
        return ResponseEntity.ok("Habit has been deleted correctly.");
    }
}
