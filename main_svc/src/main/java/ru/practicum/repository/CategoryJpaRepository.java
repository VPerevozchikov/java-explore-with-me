package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Category;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
}