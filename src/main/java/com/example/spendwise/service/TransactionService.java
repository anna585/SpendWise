package com.example.spendwise.service;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.Transaction;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.repository.CategoryRepository;
import com.example.spendwise.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAllByOrderByTransactionDateDescIdDesc();
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional
    public Transaction create(
            TransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            Long categoryId
    ) {
        Category category = findCategory(categoryId, type);
        Transaction transaction = new Transaction(
                type,
                amount,
                transactionDate,
                description.trim(),
                category
        );
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction update(
            Long id,
            TransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            Long categoryId
    ) {
        Transaction transaction = findById(id);
        Category category = findCategory(categoryId, type);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);
        transaction.setDescription(description.trim());
        transaction.setCategory(category);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.delete(findById(id));
    }

    private Category findCategory(Long categoryId, TransactionType type) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        if (category.getType() != type) {
            throw new TransactionCategoryTypeMismatchException(type);
        }
        return category;
    }
}
