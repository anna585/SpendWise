package com.example.spendwise.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void listsCategoriesInRepositoryOrder() {
        List<Category> categories = List.of(
                new Category("Dining", TransactionType.EXPENSE),
                new Category("Salary", TransactionType.INCOME)
        );
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

        assertThat(categoryService.findAll()).containsExactlyElementsOf(categories);
    }

    @Test
    void listsCategoriesFilteredByType() {
        List<Category> categories = List.of(
                new Category("Dining", TransactionType.EXPENSE)
        );
        when(categoryRepository.findAllByTypeOrderByNameAsc(TransactionType.EXPENSE))
                .thenReturn(categories);

        assertThat(categoryService.findAllByType(TransactionType.EXPENSE))
                .containsExactlyElementsOf(categories);
    }

    @Test
    void trimsCategoryTextAndSavesIt() {
        when(categoryRepository.existsByNameIgnoreCase("Dining")).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category saved = categoryService.create(
                " Dining ",
                TransactionType.EXPENSE,
                "  Restaurants  "
        );

        assertThat(saved.getName()).isEqualTo("Dining");
        assertThat(saved.getDescription()).isEqualTo("Restaurants");
        verify(categoryRepository).save(saved);
    }

    @Test
    void normalizesBlankDescriptionToNull() {
        when(categoryRepository.existsByNameIgnoreCase("Salary")).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category saved = categoryService.create("Salary", TransactionType.INCOME, "  ");

        assertThat(saved.getDescription()).isNull();
    }

    @Test
    void rejectsCaseInsensitiveDuplicateNamesWithoutSaving() {
        when(categoryRepository.existsByNameIgnoreCase("dining")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(
                " dining ",
                TransactionType.EXPENSE,
                null
        )).isInstanceOf(CategoryAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }
}
