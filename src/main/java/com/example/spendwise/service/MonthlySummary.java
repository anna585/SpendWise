package com.example.spendwise.service;

import java.math.BigDecimal;

public record MonthlySummary(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
