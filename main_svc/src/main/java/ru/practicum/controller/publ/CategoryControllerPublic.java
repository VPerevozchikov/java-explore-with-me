package ru.practicum.controller.publ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryControllerPublic {

    private final CategoryService categoryService;

    @Autowired
    public CategoryControllerPublic(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") int from,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        List<CategoryDto> categoryDtos = categoryService.getAllCategories(from, size);
        log.info("Получен список всех категорий через Публичный контроллер");
        return categoryDtos;
    }

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable int categoryId) {
        CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
        log.info("Получена категория с id={} через Публичный контроллер", categoryId);
        return categoryDto;
    }
}