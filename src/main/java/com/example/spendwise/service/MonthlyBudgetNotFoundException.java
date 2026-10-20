package com.example.spendwise.service;

import org.springframework.lang.NonNull;

public class MonthlyBudgetNotFoundException extends RuntimeException {

    private final String errorMessage;

    public MonthlyBudgetNotFoundException(Long id) {
        super("Monthly budget " + id + " was not found.");
        this.errorMessage = "Monthly budget " + id + " was not found.";
    }

    @NonNull
    public String message() {
        return errorMessage;
    }
}
