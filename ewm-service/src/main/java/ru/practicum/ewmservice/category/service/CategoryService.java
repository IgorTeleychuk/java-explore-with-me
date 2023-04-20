package ru.practicum.ewmservice.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.category.dto.NewCategoryDto;
import ru.practicum.ewmservice.category.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long catId);

    CategoryDto patch(Long catId, CategoryDto categoryDto);

    void deleteById(Long catId);

    Category getCategoryById(Long catId);
}
