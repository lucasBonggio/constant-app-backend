package com.backend.constante.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.constante.dto.RecordDTO;
import com.backend.constante.exception.ResourceNotFoundException;
import com.backend.constante.model.Habit;
import com.backend.constante.model.Record;
import com.backend.constante.model.User;
import com.backend.constante.repositories.HabitRepository;
import com.backend.constante.repositories.RecordRepository;
import com.backend.constante.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final HabitRepository habitRepository;

    /**
    * Saves information about the completed habit.
    * First, it searches for the user and the habit in question.
    * If it doesn't find either of the searched objects, it returns a {@link ResourceNotFoundException}.
    * Finally, it maps the user and the habit to the record and saves it.
    * 
    * @param email retrieved from the token. Used to search for the user's account.
    * @param idHabit used to search for the habit.
    * @return Record saved and mapped as a DTO.
    * @throws ResourceNotFoundException if the user or the habit were not found.
    */
    public RecordDTO saveRecord(String email, Long idHabit){
        User user =  userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        Habit habit = habitRepository.findById(idHabit)
            .orElseThrow(() -> new ResourceNotFoundException("Habit", idHabit));
        
        Record newRecord = new Record();
        newRecord.setUser(user);
        newRecord.setHabit(habit);
        newRecord.setCompleted(true);

        Record savedRecord = recordRepository.save(newRecord);

        return modelMapper.map(savedRecord, RecordDTO.class);
    }

    /**
     * Returns a paginated list of records for a specific habit.
     * The method first checks that the user exists; if not, throws {@link ResourceNotFoundException}.
     * Then, it verifies the habit exists.
     * Finally, it retrieves the records with pagination and maps them to DTOs.
     *
     * @param idHabit identifier of the habit to search for.
     * @param email retrieved from the authenticated user's token.
     * @param page zero-based index for pagination (0 = first page).
     * @param size number of items per page.
     * @return Page of RecordDTOs containing the user's records for the habit.
     */
    public Page<RecordDTO> findRecordsByHabit(Long idHabit, String email, int page, int size){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        Habit habit = habitRepository.findById(idHabit)
            .orElseThrow(() -> new ResourceNotFoundException("Habit", idHabit));
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Record> records = recordRepository.findByHabitAndUser(habit, user, pageable);
        
        return records.map(record -> modelMapper.map(record, RecordDTO.class));
    }

    /**
     * Returns a list of records for a specific user on a given date.
     * The method first checks that the user exists; if not, throws {@link ResourceNotFoundException}.
     * Then, it retrieves all records associated with that user and date.
     * Finally, it maps the entities to DTOs for safe return.
     *
     * @param email retrieved from the authenticated user's token.
     * @param date the target date to search records for.
     * @return List of RecordDTOs with the user's records on the specified date.
     */
    public List<RecordDTO> findRecordsByDate(String email, LocalDate date){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        List<Record> records = recordRepository.findByDateAndUser(date, user);

        return records.stream()
                        .map(record -> modelMapper.map(record, RecordDTO.class))
                        .collect(Collectors.toList());
    }
    
}
