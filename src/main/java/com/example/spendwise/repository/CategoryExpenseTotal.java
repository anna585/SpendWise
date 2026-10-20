package com.example.spendwise.repository;

import java.math.BigDecimal;

public interface CategoryExpenseTotal {

    Long getCategoryId();

    String getCategoryName();

    BigDecimal getAmount();
}
