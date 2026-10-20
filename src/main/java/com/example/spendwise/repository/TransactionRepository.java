package com.example.spendwise.repository;

import com.example.spendwise.entity.Transaction;
import com.example.spendwise.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Transaction> findAllByOrderByTransactionDateDescIdDesc();

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.type = :type
              AND t.transactionDate >= :startDate
              AND t.transactionDate < :endDate
            """)
    BigDecimal sumAmountByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT t.category.id AS categoryId,
                   t.category.name AS categoryName,
                   SUM(t.amount) AS amount
            FROM Transaction t
            WHERE t.type = com.example.spendwise.entity.TransactionType.EXPENSE
              AND t.transactionDate >= :startDate
              AND t.transactionDate < :endDate
            GROUP BY t.category.id, t.category.name
            """)
    List<CategoryExpenseTotal> sumExpensesByCategoryAndDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
