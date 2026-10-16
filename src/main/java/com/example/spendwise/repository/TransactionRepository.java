package com.example.spendwise.repository;

import com.example.spendwise.entity.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Transaction> findAllByOrderByTransactionDateDescIdDesc();
}
