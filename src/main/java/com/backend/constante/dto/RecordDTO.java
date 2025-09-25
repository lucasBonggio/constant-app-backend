package com.backend.constante.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDTO {
    private Long habitId;
    private LocalDate date;
    private Boolean completed;
}
