package ru.practicum.dto.category;

import ru.practicum.model.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        return categoryDto;
    }

    public static CategoryDto toDto(NewCategoryDto newCategoryDto) {
        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setName(newCategoryDto.getName());

        return categoryDto;
    }

    public static Category toCategory(NewCategoryDto categoryDto) {
        Category category = new Category();

        category.setName(categoryDto.getName());

        return category;
    }

    public static Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();

        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());

        return category;
    }
}