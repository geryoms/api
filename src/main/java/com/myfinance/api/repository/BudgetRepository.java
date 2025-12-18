package com.myfinance.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myfinance.api.model.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndStartDate(Long userId, Long categoryId, LocalDate startDate);
}