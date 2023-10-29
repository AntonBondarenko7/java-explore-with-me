package ru.practicum.ewm.category;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryRequestDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.exception.NotSavedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("сохранена категория, когда категория валидна, тогда она сохраняется")
    void saveCategory_whenCategoryValid_thenSavedCategory() {
        NewCategoryRequestDto categoryToSave = new NewCategoryRequestDto();
        categoryToSave.setName("name");
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(CategoryMapper.INSTANCE.toCategoryFromNewDto(categoryToSave));

        CategoryDto actualCategory = categoryService.saveCategory(categoryToSave);

        assertThat(categoryToSave.getName(), equalTo(actualCategory.getName()));
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("сохранена категория, когда категория не валидна, тогда выбрасывается исключение")
    void saveCategory_whenCategoryNotValid_thenExceptionThrown() {
        NewCategoryRequestDto categoryToSave = new NewCategoryRequestDto();
        when(categoryRepository.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("Категория не была создана."));

        final NotSavedException exception = assertThrows(NotSavedException.class,
                () -> categoryService.saveCategory(categoryToSave));

        assertThat("Категория не была создана: " + categoryToSave,
                equalTo(exception.getMessage()));
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
