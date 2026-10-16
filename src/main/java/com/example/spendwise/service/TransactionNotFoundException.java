package com.example.spendwise.service;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("Transaction " + id + " was not found.");
    }
}
