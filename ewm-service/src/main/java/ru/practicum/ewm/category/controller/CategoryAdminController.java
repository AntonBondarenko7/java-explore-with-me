package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.category.dto.NewCategoryRequestDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public CategoryDto saveCategory(@Valid @RequestBody NewCategoryRequestDto newCategoryDto) {
        CategoryDto categoryDto = categoryService.saveCategory(newCategoryDto);
        log.info("Добавлена новая категория: {}", categoryDto);
        return categoryDto;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Boolean deleteCategoryById(@PathVariable Long catId) {
        log.info("Удалена категория с id = {}", catId);
        return categoryService.deleteCategoryById(catId);
    }

    @PatchMapping("/{catId}")
    @Validated
    public CategoryDto updateCategory(
            @PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Обновлена категория с id = {}: {}.", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

}
