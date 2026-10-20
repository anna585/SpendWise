package com.example.spendwise.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.MonthlyBudget;
import com.example.spendwise.entity.Transaction;
import com.example.spendwise.entity.TransactionType;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
class FinanceSchemaTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsTransactionWithItsCategoryAndMoneyPrecision() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Salary", TransactionType.INCOME)
        );
        Transaction transaction = transactionRepository.saveAndFlush(
                new Transaction(
                        TransactionType.INCOME,
                        new BigDecimal("1250.75"),
                        LocalDate.of(2026, 10, 1),
                        "Monthly salary",
                        category
                )
        );

        Transaction reloaded = transactionRepository.findById(transaction.getId()).orElseThrow();
        assertThat(reloaded.getAmount()).isEqualByComparingTo("1250.75");
        assertThat(reloaded.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    void sumsTransactionsWithinInclusiveStartAndExclusiveEndDates() {
        Category incomeCategory = categoryRepository.saveAndFlush(
                new Category("Salary", TransactionType.INCOME)
        );
        Category expenseCategory = categoryRepository.saveAndFlush(
                new Category("Dining", TransactionType.EXPENSE)
        );
        saveTransaction(incomeCategory, TransactionType.INCOME, "100.00", LocalDate.of(2026, 9, 30));
        saveTransaction(incomeCategory, TransactionType.INCOME, "200.00", LocalDate.of(2026, 10, 1));
        saveTransaction(incomeCategory, TransactionType.INCOME, "300.00", LocalDate.of(2026, 10, 31));
        saveTransaction(incomeCategory, TransactionType.INCOME, "400.00", LocalDate.of(2026, 11, 1));
        saveTransaction(expenseCategory, TransactionType.EXPENSE, "50.00", LocalDate.of(2026, 9, 30));
        saveTransaction(expenseCategory, TransactionType.EXPENSE, "25.25", LocalDate.of(2026, 10, 1));
        saveTransaction(expenseCategory, TransactionType.EXPENSE, "75.75", LocalDate.of(2026, 10, 31));
        saveTransaction(expenseCategory, TransactionType.EXPENSE, "100.00", LocalDate.of(2026, 11, 1));

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 1);

        assertThat(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.INCOME, startDate, endDate
        )).isEqualByComparingTo("500.00");
        assertThat(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.EXPENSE, startDate, endDate
        )).isEqualByComparingTo("101.00");
        assertThat(transactionRepository.sumExpensesByCategoryAndDateRange(startDate, endDate))
                .singleElement()
                .satisfies(total -> {
                    assertThat(total.getCategoryId()).isEqualTo(expenseCategory.getId());
                    assertThat(total.getCategoryName()).isEqualTo("Dining");
                    assertThat(total.getAmount()).isEqualByComparingTo("101.00");
                });
    }

    private void saveTransaction(
            Category category,
            TransactionType type,
            String amount,
            LocalDate date
    ) {
        transactionRepository.saveAndFlush(
                new Transaction(type, new BigDecimal(amount), date, "Test transaction", category)
        );
    }

    @Test
    void persistsUpdatesAndDeletesTransactionsAndOrdersNewestFirst() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Dining", TransactionType.EXPENSE)
        );
        Transaction older = transactionRepository.saveAndFlush(
                new Transaction(
                        TransactionType.EXPENSE,
                        new BigDecimal("12.50"),
                        LocalDate.of(2026, 10, 14),
                        "Older meal",
                        category
                )
        );
        Transaction newer = transactionRepository.saveAndFlush(
                new Transaction(
                        TransactionType.EXPENSE,
                        new BigDecimal("20.00"),
                        LocalDate.of(2026, 10, 16),
                        "Newer meal",
                        category
                )
        );

        List<Transaction> ordered = transactionRepository.findAllByOrderByTransactionDateDescIdDesc();
        assertThat(ordered).extracting(Transaction::getId)
                .containsExactly(newer.getId(), older.getId());

        newer.setAmount(new BigDecimal("21.75"));
        newer.setDescription("Updated meal");
        Transaction updated = transactionRepository.saveAndFlush(newer);
        assertThat(updated.getAmount()).isEqualByComparingTo("21.75");
        assertThat(updated.getDescription()).isEqualTo("Updated meal");

        transactionRepository.delete(updated);
        transactionRepository.flush();
        assertThat(transactionRepository.findById(newer.getId())).isEmpty();
        assertThat(transactionRepository.findById(older.getId())).isPresent();
    }

    @Test
    void enforcesUniqueCategoryNames() {
        categoryRepository.saveAndFlush(new Category("Groceries", TransactionType.EXPENSE));
        assertThatThrownBy(() -> categoryRepository.saveAndFlush(
                new Category("Groceries", TransactionType.EXPENSE)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesOneBudgetPerCategoryAndMonth() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Groceries", TransactionType.EXPENSE)
        );
        LocalDate month = LocalDate.of(2026, 10, 1);
        monthlyBudgetRepository.saveAndFlush(
                new MonthlyBudget(month, new BigDecimal("500.00"), category)
        );
        assertThatThrownBy(() -> monthlyBudgetRepository.saveAndFlush(
                new MonthlyBudget(month, new BigDecimal("600.00"), category)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsNonFirstDayBudgetMonths() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Utilities", TransactionType.EXPENSE)
        );
        MonthlyBudget budget = new MonthlyBudget(
                LocalDate.of(2026, 10, 15),
                new BigDecimal("200.00"),
                category
        );

        assertThatThrownBy(() -> monthlyBudgetRepository.saveAndFlush(budget))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void rejectsTransactionWhoseTypeDoesNotMatchItsCategory() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Salary", TransactionType.INCOME)
        );
        Transaction transaction = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("10.00"),
                LocalDate.of(2026, 10, 1),
                "Mismatched type",
                category
        );

        assertThatThrownBy(() -> transactionRepository.saveAndFlush(transaction))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void rejectsBudgetForIncomeCategory() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Salary", TransactionType.INCOME)
        );
        MonthlyBudget budget = new MonthlyBudget(
                LocalDate.of(2026, 10, 1),
                new BigDecimal("100.00"),
                category
        );

        assertThatThrownBy(() -> monthlyBudgetRepository.saveAndFlush(budget))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void doesNotCascadeDeleteReferencedCategories() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Dining", TransactionType.EXPENSE)
        );
        transactionRepository.saveAndFlush(
                new Transaction(
                        TransactionType.EXPENSE,
                        new BigDecimal("25.00"),
                        LocalDate.of(2026, 10, 1),
                        "Dinner",
                        category
                )
        );

        assertThatThrownBy(() -> jdbcTemplate.update(
                "DELETE FROM categories WHERE id = ?",
                category.getId()
        ))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
