package com.example.spendwise.service;

public class InvalidBudgetCategoryException extends RuntimeException {

    public InvalidBudgetCategoryException() {
        super("Budgets can only be assigned to expense categories.");
    }
}
