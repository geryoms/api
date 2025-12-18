package com.myfinance.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myfinance.api.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


   @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.user.id = :userId")
List<Transaction> findByUserId(Long userId);

@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal calculateTotalByTypeAndDateRange(Long userId, String type, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.type = 'GASTO' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalByCategoryAndDateRange(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate);
}