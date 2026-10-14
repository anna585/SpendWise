package com.example.spendwise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "monthly_budgets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_budgets_category_month",
                columnNames = {"category_id", "budget_month"}
        )
)
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "budget_month", nullable = false)
    private LocalDate month;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_monthly_budgets_category")
    )
    private Category category;

    protected MonthlyBudget() {
    }

    public MonthlyBudget(LocalDate month, BigDecimal amount, Category category) {
        this.month = month;
        this.amount = amount;
        this.category = category;
    }

    @AssertTrue(message = "Budget month must be the first day of the month")
    public boolean isFirstDayOfMonth() {
        return month == null || month.getDayOfMonth() == 1;
    }

    @AssertTrue(message = "Budgets can only be assigned to expense categories")
    public boolean isExpenseCategory() {
        return category == null || category.getType() == TransactionType.EXPENSE;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
