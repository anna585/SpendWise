package com.example.spendwise.service;

import com.example.spendwise.entity.TransactionType;
import java.util.Locale;

public class TransactionCategoryTypeMismatchException extends RuntimeException {

    public TransactionCategoryTypeMismatchException(TransactionType transactionType) {
        super("The selected category must be an "
                + transactionType.name().toLowerCase(Locale.ROOT)
                + " category.");
    }
}
