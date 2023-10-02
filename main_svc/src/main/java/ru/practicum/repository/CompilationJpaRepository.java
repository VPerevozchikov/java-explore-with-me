package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationJpaRepository extends JpaRepository<Compilation, Integer> {

    List<Compilation> findByPinned(boolean pinned, Pageable page);
}