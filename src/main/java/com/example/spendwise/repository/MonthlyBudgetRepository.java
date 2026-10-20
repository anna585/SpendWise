package com.example.spendwise.repository;

import com.example.spendwise.entity.MonthlyBudget;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

    @EntityGraph(attributePaths = "category")
    @NonNull
    @Query("SELECT b FROM MonthlyBudget b WHERE b.id = :id")
    Optional<MonthlyBudget> findByIdWithCategory(@Param("id") @NonNull Long id);

    @EntityGraph(attributePaths = "category")
    List<MonthlyBudget> findAllByMonthOrderByCategory_NameAsc(LocalDate month);

    boolean existsByCategory_IdAndMonth(Long categoryId, LocalDate month);

    boolean existsByCategory_IdAndMonthAndIdNot(Long categoryId, LocalDate month, Long id);
}
