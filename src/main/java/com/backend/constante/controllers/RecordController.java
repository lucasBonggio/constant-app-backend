package com.backend.constante.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.constante.dto.RecordDTO;
import com.backend.constante.services.RecordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @PostMapping("/{id}")
    public ResponseEntity<RecordDTO> saveRecord(@PathVariable Long habitId,
                                                Authentication authentication){
        String email = authentication.getName();
        RecordDTO newRecord = recordService.saveRecord(email, habitId);

        return ResponseEntity.status(HttpStatus.CREATED)
                            .body(newRecord);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagedModel<EntityModel<RecordDTO>>> findByHabitId(@PathVariable Long habitId,
                                                    @RequestParam(defaultValue="0") int page,
                                                    @RequestParam(defaultValue="10") int size,
                                                    PagedResourcesAssembler<RecordDTO> assembler,
                                                    Authentication authentication){
        String email = authentication.getName();
        Page<RecordDTO> records = recordService.findRecordsByHabit(habitId, email, page, size);

        PagedModel<EntityModel<RecordDTO>> pagedModel = assembler.toModel(records);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping
    public ResponseEntity<List<RecordDTO>> findByDate(@RequestParam LocalDate date,
                                                    Authentication authentication){
        String email = authentication.getName();
        List<RecordDTO> records = recordService.findRecordsByDate(email, date);

        return ResponseEntity.ok(records);
    }
}
