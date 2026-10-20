package com.example.spendwise.service;

import java.math.BigDecimal;

public record BudgetReportRow(
        Long categoryId,
        String categoryName,
        Long budgetId,
        BigDecimal budgetAmount,
        BigDecimal amountSpent,
        BigDecimal remainingAmount
) {
    public boolean isUnbudgeted() {
        return budgetAmount == null;
    }

    public boolean isOverBudget() {
        return remainingAmount != null && remainingAmount.signum() < 0;
    }
}
