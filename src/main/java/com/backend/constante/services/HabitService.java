package com.backend.constante.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.backend.constante.dto.HabitDTO;
import com.backend.constante.exception.ResourceNotFoundException;
import com.backend.constante.model.Habit;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    /**
     * Creates a new habit associated with the authenticated user.
     *
     * The user must exist and be authenticated (email validated via JWT).
     * If the user or any required data is missing, a {@link ResourceNotFoundException} is thrown.
     *
     * @param habit DTO with habit data (name, frequency, etc.)
     * @param email of the authenticated user (from JWT)
     * @return saved habit as DTO.
     * @throws ResourceNotFoundException if user or context is invalid
     */
    public HabitDTO createHabit(HabitDTO habit, String email){
        Habit newHabit = modelMapper.map(habit, Habit.class);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        newHabit.setUser(user);
        Habit savedHabit = habitRepository.save(newHabit);

        return modelMapper.map(savedHabit, HabitDTO.class);
    }

    /**
    * Returns a list of mapped habits as DTOs.
    * This method searches for the account of the already authenticated user; if it's not found, a {@link ResourceNotFoundException} is thrown.
    * 
    * @param email retrieved from the user's token.
    * @return List of habits as DTOs.
    * @throws ResourceNotFoundException if user is invalid.
    */
    public List<HabitDTO> findHabitsByUser(String email){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        List<Habit> habits = habitRepository.findByUser(user);

        return habits.stream()
                    .map(habit -> modelMapper.map(habit, HabitDTO.class))
                    .collect(Collectors.toList());
    }
    
    /**
    * Searches for a habit by a specific ID.
    * This method searches for the authenticated user's data.
    * Then, with the user's data, we search for the habit in question.
    * If any of these items are not found, a {@link ResourceNotFoundException} is thrown.
    * 
    * @param idHabit the ID of the habit in question.
    * @param email retrieved from the user's token.
    * @return HabitDTO with the mapped information.
    * @throws ResourceNotFoundException if user or habit is invalid.
    */
    public HabitDTO findHabitById(Long idHabit, String email){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        Habit habit = habitRepository.findByIdAndUser(idHabit, user)
            .orElseThrow(() -> new ResourceNotFoundException("Habit", idHabit));
        
        return modelMapper.map(habit, HabitDTO.class);
    }

    /**
     * Performs a partial update on a habit (only non-null fields are updated).
     *
     * This method does NOT overwrite null values — useful for PATCH-style operations
     * where only specific fields are sent.
     *
     * @param email of the authenticated user.
     * @param idHabit target habit ID.
     * @param habit contains only the fields to update (others are ignored).
     * @throws ResourceNotFoundException if user or habit is invalid.
    */
    public void updateHabit(String email, Long idHabit, HabitDTO habit){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        Habit existingHabit = habitRepository.findByIdAndUser(idHabit, user)
            .orElseThrow(() -> new ResourceNotFoundException("Habit", idHabit));
            
        if(habit.getName() != null)existingHabit.setName(habit.getName());
        if(habit.getDescription() != null) existingHabit.setDescription(habit.getDescription());
        if(habit.getMadeSince() != null) existingHabit.setMadeSince(habit.getMadeSince());
        if(habit.getFrequency() != null) existingHabit.setFrequency(habit.getFrequency());
        if(habit.getReminderTime() != null) existingHabit.setReminderTime(habit.getReminderTime());

        habitRepository.save(existingHabit);
    }

    /**
     * Delete a habit using a specific ID.
     * The method first looks for user information.
     * Then, with the user's information, it searches for the habit.
     * If either search returns no results, a {@link ResourceNotFoundException} is thrown.
     *
     * @param email retrieved from the user's token.
     * @param idHabit the ID of the habit to search for.
     * @throws ResourceNotFoundException if user or habit is invalid.
     */
    public void deleteHabit(String email, Long idHabit){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        Habit habit = habitRepository.findByIdAndUser(idHabit, user)
            .orElseThrow(() -> new ResourceNotFoundException("Habit", idHabit));
        //Unlink the user before deletion to avoid cascading errors.
        habit.setUser(null);

        habitRepository.delete(habit);
    }
}
