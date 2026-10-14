package com.example.spendwise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_date_type", columnList = "transaction_date, type"),
                @Index(name = "idx_transactions_category_date", columnList = "category_id, transaction_date")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionType type;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_category")
    )
    private Category category;

    protected Transaction() {
    }

    public Transaction(
            TransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            Category category
    ) {
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.category = category;
    }

    @AssertTrue(message = "Transaction type must match its category type")
    public boolean isCategoryTypeCompatible() {
        return type == null || category == null || type == category.getType();
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
