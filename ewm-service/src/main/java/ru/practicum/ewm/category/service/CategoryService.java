package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryRequestDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotSavedException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDto saveCategory(NewCategoryRequestDto newCategoryDto) {
        try {
            Category category = categoryRepository.save(
                    CategoryMapper.INSTANCE.toCategoryFromNewDto(newCategoryDto));
            return CategoryMapper.INSTANCE.toCategoryDto(category);
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Категория не была создана: " + newCategoryDto);
        }
    }

    public Boolean deleteCategoryById(Long catId) {
        findCategoryById(catId);

        try {
            return categoryRepository.deleteByIdWithReturnedLines(catId) >= 0;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Категория с ид = " + catId + " не может быть удалена, " +
                    "существуют события, связанные с категорией.");
        }
    }

    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category cat = findCategoryById(catId);
        cat.setName(categoryDto.getName());

        try {
            return CategoryMapper.INSTANCE.toCategoryDto(categoryRepository.saveAndFlush(cat));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Категория с id = " + catId + " не была обновлена: " + categoryDto);
        }
    }

    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return CategoryMapper.INSTANCE.convertCategoryListToCategoryDTOList(
                categoryRepository.findAll(page).getContent());
    }

    public CategoryDto getCategoryById(Long catId) {
        return CategoryMapper.INSTANCE.toCategoryDto(findCategoryById(catId));
    }

    public Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найден."));
    }

}
