package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.*;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;

    @Autowired
    public CategoryService(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    public CategoryDto create(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (name == null || name.isBlank()) {
            throw new BadParameterException("Имя категории не должно быть пустым");
        }
        if (categoryJpaRepository.findByName(name) != null) {
            throw new AlreadyExistException("Категория с именем " + name + " уже существует");
        }

        Category category = categoryJpaRepository.save(CategoryMapper.toCategory(newCategoryDto));
        return CategoryMapper.toDto(category);
    }

    public void deleteById(int catId) {
        Optional<Category> categoryOptional = categoryJpaRepository.findById(catId);
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("Категория с id= " + catId + " не найдена");
        }
        try {
            categoryJpaRepository.deleteById(catId);
        } catch (RuntimeException ex) {
            throw new DataConflictException("Невозможно удалить категорию. Возможно, существуют связанные события");
        }
    }

    public CategoryDto updateCategory(int catId, CategoryDto categoryDto) {
        String name = categoryDto.getName();
        if (name.isBlank()) {
            throw new BadParameterException("Имя категории должно быть не пустым");
        }
        Optional<Category> categoryOptional = categoryJpaRepository.findById(catId);
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("категории с id=" + catId + " не существует");
        }
        Category category = categoryOptional.get();
        if (category.getName().equals(name)) {
            return CategoryMapper.toDto(category);
        }
        if (categoryJpaRepository.findByName(name) != null) {
            throw new AlreadyExistException("категория с таким именем уже существует");
        }
        category.setName(categoryDto.getName());
        categoryJpaRepository.save(category);
        return CategoryMapper.toDto(category);
    }

    public List<CategoryDto> getAllCategories(int from, int size) {
        if (from < 0 || size < 1) {
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Category> categories = categoryJpaRepository.findAll(page).getContent();
        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(int categoryId) {
        if (categoryId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }
        Optional<Category> categoryOptional = categoryJpaRepository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("Элемент с id=" + categoryId + " не найден");
        }
        return CategoryMapper.toDto(categoryOptional.get());
    }
}