package com.example.spendwise.service;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Category " + id + " was not found.");
    }
}
