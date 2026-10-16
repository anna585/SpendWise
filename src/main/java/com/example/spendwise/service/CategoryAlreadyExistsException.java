package com.example.spendwise.service;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String name) {
        super("A category named '" + name + "' already exists.");
    }
}
