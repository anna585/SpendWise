package com.example.spendwise.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class MonthlyBudgetForm {

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Select a valid month.")
    private String month;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;

    @NotNull
    @Positive
    private Long categoryId;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
