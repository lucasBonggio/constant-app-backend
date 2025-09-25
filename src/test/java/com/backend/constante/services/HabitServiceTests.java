package com.backend.constante.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.backend.constante.dto.HabitDTO;
import com.backend.constante.exception.ResourceNotFoundException;
import com.backend.constante.model.Habit;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class HabitServiceTests {
    @InjectMocks
    private HabitService habitService;

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Habit habit;
    private Habit habit2;
    private Habit habit3;
    private HabitDTO habitDTO;
    private HabitDTO habitDTO2;
    private HabitDTO habitDTO3;
    private Habit savedHabit;
    private HabitDTO updatedHabit;
    private String email;

    @BeforeEach
    void setUp(){
        email = "example@gmail.com";

        user = new User();
        user.setId(1L);
        user.setEmail("example@gmail.com");

        habit = new Habit();
        habit.setId(1L);
        habit.setName("Play guitar");
        habit.setUser(user);

        habit2 = new Habit();
        habit2.setName("Play LOL");
        habit2.setUser(user);

        habit3 = new Habit();
        habit3.setName("Play Minecraft");
        habit3.setUser(user);

        habitDTO2 = new HabitDTO();
        habitDTO2.setName("Play LOL");

        habitDTO3 = new HabitDTO();
        habitDTO3.setName("Play Minecraft");

        habitDTO = new HabitDTO();
        habitDTO.setName("Play guitar");
        
        savedHabit = new Habit();
        savedHabit.setName("Play guitar");
        savedHabit.setUser(user);

        updatedHabit = new HabitDTO();
        updatedHabit.setName("Play electric guitar");
    }

    @Test
    void IShouldCreateAHabit(){
        HabitDTO expectedHabit = new HabitDTO();
        expectedHabit.setName("Play guitar");

        when(modelMapper.map(habitDTO, Habit.class)).thenReturn(habit);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.save(habit)).thenReturn(savedHabit);
        when(modelMapper.map(savedHabit, HabitDTO.class)).thenReturn(habitDTO);

        HabitDTO result = habitService.createHabit(habitDTO, email);

        assertNotNull(result);
        assertEquals(expectedHabit.getName(), result.getName());
        verify(userRepository).findByEmail(email);
        verify(habitRepository).save(any(Habit.class));
    }
    
    @Test
    void IShouldCreateAHabitButUserNotFound(){

        when(modelMapper.map(habitDTO, Habit.class)).thenReturn(habit);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            habitService.createHabit(habitDTO, email);
        });
        
        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(modelMapper).map(habitDTO, Habit.class);
        verify(habitRepository, never()).save(any());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void IShouldFindHabitByUser(){
        List<Habit> habits = List.of(habit, habit2, habit3);
        List<HabitDTO> expectedDTOs = List.of(habitDTO, habitDTO2, habitDTO3);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findByUser(user)).thenReturn(habits);
        when(modelMapper.map(habit, HabitDTO.class)).thenReturn(habitDTO);
        when(modelMapper.map(habit2,HabitDTO.class)).thenReturn(habitDTO2);
        when(modelMapper.map(habit3,HabitDTO.class)).thenReturn(habitDTO3);

        List<HabitDTO> result = habitService.findHabitsByUser(email);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedDTOs, result);
        verify(userRepository).findByEmail(email);
        verify(habitRepository).findByUser(user);
        verify(modelMapper, times(3)).map(any(Habit.class), eq(HabitDTO.class));
    }

    @Test
    void IShouldFindHabitsByUserButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.findHabitsByUser(email));

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(userRepository).findByEmail(email);
        verify(habitRepository, never()).findByUser(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void IShouldFindHabitsByUserButEmptyList(){
        List<Habit> noHabits = List.of();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findByUser(user)).thenReturn(noHabits);

        List<HabitDTO> result = habitService.findHabitsByUser(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
        verify(habitRepository).findByUser(user);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void IShouldFindHabitById(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(habit));
        when(modelMapper.map(habit, HabitDTO.class)).thenReturn(habitDTO);

        HabitDTO result = habitService.findHabitById(1L, email);

        assertNotNull(result);
        assertEquals(habit.getName(), result.getName());
        verify(userRepository).findByEmail(email);
        verify(habitRepository).findByIdAndUser(1L, user);
        verify(modelMapper, times(1)).map(any(Habit.class), eq(HabitDTO.class));
    }

    @Test
    void IShouldFindHabitByIdButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.findHabitById(1L, email));

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(userRepository).findByEmail(email);
        verify(habitRepository, never()).findByUser(any());
        verify(modelMapper, times(0)).map(any(Habit.class), eq(HabitDTO.class));
    }

    @Test
    void IShouldUpdateHabit(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));




        habitService.updateHabit(email, 1L, updatedHabit);

        verify(habitRepository).save(argThat(savedHabit ->
            "Play electric guitar".equals(savedHabit.getName()) && 
            savedHabit.getId().equals(1L) &&
            savedHabit.getUser().getId().equals(1L)
        ));
    }

    @Test
    void IShouldUpdateHabitButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.updateHabit(email, 1L, updatedHabit));

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(userRepository).findByEmail(email);
        verify(habitRepository, never()).findById(1L);
        verify(habitRepository, never()).save(any());
    }

    @Test
    void IShouldUpdateHabitButHabitNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.updateHabit(email, 1L, habitDTO));

        assertEquals("Habit with id: '1' not found.", ex.getMessage());
        verify(userRepository).findByEmail(email);
        verify(habitRepository).findById(1L);
        verify(habitRepository, never()).save(any());
    }

    @Test
    void IShouldDeleteHabit(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        habitService.deleteHabit(email, 1L);

        verify(habitRepository).delete(habit);
    }

    @Test
    void IShouldDeleteHabitButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.deleteHabit(email, 1L));

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(habitRepository, never()).findById(1L);
        verify(habitRepository, never()).delete(any());
    }

    @Test
    void IShouldDeleteHabitButHabitNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> habitService.deleteHabit(email, 1L));

        assertEquals("Habit with id: '1' not found.", ex.getMessage());
    }

}
