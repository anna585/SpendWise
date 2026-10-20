package com.example.spendwise.service;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.MonthlyBudget;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.repository.CategoryExpenseTotal;
import com.example.spendwise.repository.CategoryRepository;
import com.example.spendwise.repository.MonthlyBudgetRepository;
import com.example.spendwise.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

@Service
@Transactional(readOnly = true)
public class MonthlyBudgetService {

    private final MonthlyBudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public MonthlyBudgetService(
            MonthlyBudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<BudgetReportRow> getReport(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.plusMonths(1).atDay(1);
        Map<Long, MonthlyBudget> budgetsByCategory = new HashMap<>();
        for (MonthlyBudget budget : budgetRepository.findAllByMonthOrderByCategory_NameAsc(startDate)) {
            budgetsByCategory.put(budget.getCategory().getId(), budget);
        }

        Map<Long, CategoryExpenseTotal> spendingByCategory = new HashMap<>();
        for (CategoryExpenseTotal total : transactionRepository
                .sumExpensesByCategoryAndDateRange(startDate, endDate)) {
            spendingByCategory.put(total.getCategoryId(), total);
        }

        Map<Long, Category> expenseCategories = new HashMap<>();
        for (Category category : categoryRepository.findAllByTypeOrderByNameAsc(TransactionType.EXPENSE)) {
            expenseCategories.put(category.getId(), category);
        }

        List<BudgetReportRow> rows = new ArrayList<>();
        for (Category category : expenseCategories.values()) {
            MonthlyBudget budget = budgetsByCategory.get(category.getId());
            CategoryExpenseTotal total = spendingByCategory.get(category.getId());
            BigDecimal spent = total == null ? BigDecimal.ZERO.setScale(2) : total.getAmount();
            BigDecimal budgetAmount = budget == null ? null : budget.getAmount();
            BigDecimal remaining = budgetAmount == null ? null : budgetAmount.subtract(spent);
            rows.add(new BudgetReportRow(
                    category.getId(),
                    category.getName(),
                    budget == null ? null : budget.getId(),
                    budgetAmount,
                    spent.setScale(2, RoundingMode.UNNECESSARY),
                    remaining == null ? null : remaining.setScale(2, RoundingMode.UNNECESSARY)
            ));
        }
        rows.sort((left, right) -> left.categoryName().compareToIgnoreCase(right.categoryName()));
        return rows;
    }

    @NonNull
    public MonthlyBudget findById(@NonNull Long id) {
        return Objects.requireNonNull(budgetRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new MonthlyBudgetNotFoundException(id)));
    }

    @Transactional
    public MonthlyBudget create(YearMonth month, BigDecimal amount, @NonNull Long categoryId) {
        Category category = findExpenseCategory(categoryId);
        if (budgetRepository.existsByCategory_IdAndMonth(categoryId, month.atDay(1))) {
            throw new DuplicateMonthlyBudgetException(category.getName(), month);
        }
        return budgetRepository.save(new MonthlyBudget(month.atDay(1), amount, category));
    }

    @Transactional
    public MonthlyBudget update(
            @NonNull Long id,
            YearMonth month,
            BigDecimal amount,
            @NonNull Long categoryId
    ) {
        MonthlyBudget budget = findById(id);
        Category category = findExpenseCategory(categoryId);
        if (budgetRepository.existsByCategory_IdAndMonthAndIdNot(categoryId, month.atDay(1), id)) {
            throw new DuplicateMonthlyBudgetException(category.getName(), month);
        }
        budget.setMonth(month.atDay(1));
        budget.setAmount(amount);
        budget.setCategory(category);
        return budgetRepository.save(budget);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        budgetRepository.delete(findById(id));
    }

    private Category findExpenseCategory(@NonNull Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        if (category.getType() != TransactionType.EXPENSE) {
            throw new InvalidBudgetCategoryException();
        }
        return category;
    }
}
