package com.backend.constante.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.constante.model.Habit;
import com.backend.constante.model.Record;
import com.backend.constante.model.User;

@Repository
public interface RecordRepository extends  JpaRepository<Record, Long>{
    Page<Record> findByHabitAndUser(Habit habit, User user, Pageable pageable);
    List<Record> findByDateAndUser(LocalDate date, User user);
}
