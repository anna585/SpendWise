package com.example.spendwise.service;

import java.time.YearMonth;
import org.springframework.lang.NonNull;

public class DuplicateMonthlyBudgetException extends RuntimeException {

    private final @NonNull String errorMessage;

    public DuplicateMonthlyBudgetException(String categoryName, YearMonth month) {
        super("A budget already exists for " + categoryName + " in " + month + ".");
        this.errorMessage = "A budget already exists for " + categoryName + " in " + month + ".";
    }

    @NonNull
    public String message() {
        return errorMessage;
    }
}
