package com.backend.constante.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.constante.model.Habit;
import com.backend.constante.model.User;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUser(User user);
    Optional<Habit> findByIdAndUser(Long habitId, User user);
}
