package com.example.spendwise.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.Transaction;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.repository.CategoryRepository;
import com.example.spendwise.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        expenseCategory = new Category("Dining", TransactionType.EXPENSE);
    }

    @Test
    void listsTransactionsInRepositoryOrder() {
        List<Transaction> transactions = List.of();
        when(transactionRepository.findAllByOrderByTransactionDateDescIdDesc())
                .thenReturn(transactions);

        assertThat(transactionService.findAll()).isSameAs(transactions);
    }

    @Test
    void calculatesMonthlyIncomeExpensesAndBalanceUsingTheMonthDateRange() {
        when(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1)
        )).thenReturn(new BigDecimal("2500.75"));
        when(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1)
        )).thenReturn(new BigDecimal("825.50"));

        MonthlySummary summary = transactionService.getMonthlySummary(YearMonth.of(2026, 10));

        assertThat(summary.income()).isEqualByComparingTo("2500.75");
        assertThat(summary.expenses()).isEqualByComparingTo("825.50");
        assertThat(summary.balance()).isEqualByComparingTo("1675.25");
        assertThat(summary.income().scale()).isEqualTo(2);
        assertThat(summary.expenses().scale()).isEqualTo(2);
        assertThat(summary.balance().scale()).isEqualTo(2);
    }

    @Test
    void returnsZeroWithTwoDecimalPlacesForAnEmptyMonth() {
        when(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1)
        )).thenReturn(null);
        when(transactionRepository.sumAmountByTypeAndDateRange(
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1)
        )).thenReturn(null);

        MonthlySummary summary = transactionService.getMonthlySummary(YearMonth.of(2026, 10));

        assertThat(summary.income()).isEqualTo(new BigDecimal("0.00"));
        assertThat(summary.expenses()).isEqualTo(new BigDecimal("0.00"));
        assertThat(summary.balance()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void createsTransactionWithResolvedCategory() {
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction created = transactionService.create(
                TransactionType.EXPENSE,
                new BigDecimal("12.50"),
                LocalDate.of(2026, 10, 16),
                "  Lunch  ",
                4L
        );

        assertThat(created.getAmount()).isEqualByComparingTo("12.50");
        assertThat(created.getDescription()).isEqualTo("Lunch");
        assertThat(created.getCategory()).isSameAs(expenseCategory);
    }

    @Test
    void rejectsMissingCategory() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(
                TransactionType.EXPENSE,
                new BigDecimal("12.50"),
                LocalDate.of(2026, 10, 16),
                "Lunch",
                404L
        )).isInstanceOf(CategoryNotFoundException.class);

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void rejectsCategoryOfTheWrongTransactionType() {
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> transactionService.create(
                TransactionType.INCOME,
                new BigDecimal("12.50"),
                LocalDate.of(2026, 10, 16),
                "Lunch",
                4L
        )).isInstanceOf(TransactionCategoryTypeMismatchException.class);

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updatesExistingTransactionAndCategory() {
        Transaction existing = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("5.00"),
                LocalDate.of(2026, 10, 15),
                "Old description",
                expenseCategory
        );
        when(transactionRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(existing)).thenReturn(existing);

        Transaction updated = transactionService.update(
                8L,
                TransactionType.EXPENSE,
                new BigDecimal("15.25"),
                LocalDate.of(2026, 10, 16),
                " Updated ",
                4L
        );

        assertThat(updated.getAmount()).isEqualByComparingTo("15.25");
        assertThat(updated.getTransactionDate()).isEqualTo(LocalDate.of(2026, 10, 16));
        assertThat(updated.getDescription()).isEqualTo("Updated");
    }

    @Test
    void failsWhenUpdatingMissingTransaction() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(
                99L,
                TransactionType.EXPENSE,
                new BigDecimal("15.25"),
                LocalDate.of(2026, 10, 16),
                "Updated",
                4L
        )).isInstanceOf(TransactionNotFoundException.class);

        verify(categoryRepository, never()).findById(4L);
    }

    @Test
    void deletesExistingTransactionAndRejectsMissingId() {
        Transaction existing = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("5.00"),
                LocalDate.of(2026, 10, 15),
                "Dinner",
                expenseCategory
        );
        when(transactionRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(transactionRepository.findById(404L)).thenReturn(Optional.empty());

        transactionService.delete(8L);

        verify(transactionRepository).delete(existing);
        assertThatThrownBy(() -> transactionService.delete(404L))
                .isInstanceOf(TransactionNotFoundException.class);
    }
}
