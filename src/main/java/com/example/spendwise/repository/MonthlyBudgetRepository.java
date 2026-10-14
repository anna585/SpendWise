package com.example.spendwise.repository;

import com.example.spendwise.entity.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
}
