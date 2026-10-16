package com.example.spendwise.service;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public List<Category> findAllByType(TransactionType type) {
        return categoryRepository.findAllByTypeOrderByNameAsc(type);
    }

    @Transactional
    public Category create(String name, TransactionType type, String description) {
        String normalizedName = name.trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new CategoryAlreadyExistsException(normalizedName);
        }

        Category category = new Category(normalizedName, type);
        category.setDescription(normalizeDescription(description));
        return categoryRepository.save(category);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
