package com.backend.constante.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.backend.constante.dto.RecordDTO;
import com.backend.constante.exception.ResourceNotFoundException;
import com.backend.constante.model.Habit;
import com.backend.constante.model.Record;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.RecordRepository;
import com.backend.constante.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class RecordServiceTests {
    @InjectMocks
    private RecordService recordService;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HabitRepository habitRepository;

    private User user;
    private Habit habit;
    private Record record;
    private Record record2;
    private Record savedRecord;
    private RecordDTO recordDTO;
    private RecordDTO recordDTO2;
    private String email;

    @BeforeEach
    void setUp(){
        email = "example@gmail.com";

        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername("username");

        habit = new Habit();
        habit.setName("Play guitar");
        habit.setUser(user);

        record = new Record();
        record.setId(1L);
        record.setUser(user);
        record.setHabit(habit);
        record.setCompleted(true);

        record2 = new Record();
        record2.setId(2L);
        record2.setUser(user);
        record2.setHabit(habit);
        record2.setCompleted(true);

        savedRecord = new Record();
        savedRecord.setId(1L);
        savedRecord.setUser(user);
        savedRecord.setCompleted(true);
        
        recordDTO = new RecordDTO();
        recordDTO.setCompleted(true);
        recordDTO.setHabitId(1L);

        recordDTO2 = new RecordDTO();
        recordDTO2.setCompleted(true);
        recordDTO2.setHabitId(2L);
    }
    
    @Test
    void IShouldSaveRecord(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);
        when(modelMapper.map(any(Record.class), eq(RecordDTO.class))).thenReturn(recordDTO);

        RecordDTO result = recordService.saveRecord(email, 1L);

        assertNotNull(result);
        assertEquals(result.getHabitId(), 1L);
    }

    @Test
    void IShouldSaveRecordButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            recordService.saveRecord(email, 1L);
        });

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void IShouldSaveRecordButHabitNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());


        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            recordService.saveRecord(email, 1L);
        });

        assertEquals("Habit with id: '1' not found.", ex.getMessage());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void IShouldFindRecordsByHabit(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<Record> recordsPage = new PageImpl<>(List.of(record, record2), pageable, 2);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(recordRepository.findByHabitAndUser(habit, user, pageable)).thenReturn(recordsPage);
        when(modelMapper.map(any(Record.class), eq(RecordDTO.class)))
        .thenAnswer(invocation -> {
            Record source = invocation.getArgument(0);
            if (source.getId().equals(1L)) {
                return recordDTO;
            } else if (source.getId().equals(2L)) {
                return recordDTO2;
            }
            return null;
        });
        Page<RecordDTO> records = recordService.findRecordsByHabit(1L, email, 0, 10);

        List<RecordDTO> results = records.getContent();

        assertNotNull(results);
        assertAll("results", 
            () -> assertEquals(1L, results.get(0).getHabitId()),
            () -> assertEquals(2L, results.get(1).getHabitId())
        );
    }

    @Test
    void IShouldFindRecordsByHabitButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            recordService.findRecordsByHabit(1L, email, 0, 10);
        });

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(habitRepository, never()).findById(any());
    }

    
    @Test
    void IShouldFindRecordsByHabitButHabitNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            recordService.findRecordsByHabit(1L, email, 0, 10);
        });

        assertEquals("Habit with id: '1' not found.", ex.getMessage());
        verify(recordRepository, never()).findByHabitAndUser(habit, user, PageRequest.of(0, 10));
    }

    @Test
    void IShouldFindRecordsByDate(){
        LocalDate date = LocalDate.now();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(recordRepository.findByDateAndUser(date, user)).thenReturn(List.of(record, record2));
        when(modelMapper.map(any(Record.class), eq(RecordDTO.class)))
        .thenAnswer(invocation -> {
            Record source = invocation.getArgument(0);
            if (source.getId().equals(1L)) {
                return recordDTO;
            } else if (source.getId().equals(2L)) {
                return recordDTO2;
            }
            return null;
        });

        List<RecordDTO> results = recordService.findRecordsByDate(email, date);

        assertNotNull(results);
        assertAll("results",
            () -> assertEquals(1L, results.get(0).getHabitId()),
            () -> assertEquals(2L, results.get(1).getHabitId())
        );
    }

    @Test
    void IShouldFindRecordsByDateButUserNotFound(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            recordService.findRecordsByDate(email, LocalDate.now());
        });

        assertEquals("User with id: 'example@gmail.com' not found.", ex.getMessage());
        verify(recordRepository, never()).findByDateAndUser(any(), any());
    }
}
