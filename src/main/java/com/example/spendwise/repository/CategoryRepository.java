package com.example.spendwise.repository;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.TransactionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findAllByTypeOrderByNameAsc(TransactionType type);
}
