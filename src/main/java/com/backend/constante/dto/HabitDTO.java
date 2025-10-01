package com.backend.constante.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.backend.constante.utils.Frequency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabitDTO {
    private String name;
    private String description;
    private LocalDate madeSince;
    private Frequency frequency;
    private LocalTime reminderTime; 
}
