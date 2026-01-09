package com.myfinance.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myfinance.api.model.CategoryStat;
import com.myfinance.api.model.MonthlyStat;
import com.myfinance.api.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal calculateTotalByTypeAndDateRange(
        @Param("userId") Long userId, 
        @Param("type") String type, 
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.type = 'GASTO' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalByCategoryAndDateRange(
        @Param("userId") Long userId, 
        @Param("categoryId") Long categoryId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
   @Query("SELECT COALESCE(c.name, 'Otros') as name, SUM(t.amount) as total " +
           "FROM Transaction t LEFT JOIN t.category c " +
           "WHERE t.user.id = :userId AND t.type = 'GASTO' AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY c.name")
    List<CategoryStat> getExpensesByCategory(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT new com.myfinance.api.model.MonthlyStat(" +
           "YEAR(t.date), MONTH(t.date), " +
           "SUM(CASE WHEN t.type = 'INGRESO' THEN t.amount ELSE 0 END), " +
           "SUM(CASE WHEN t.type = 'GASTO' THEN t.amount ELSE 0 END)) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.date >= :startDate " +
           "GROUP BY YEAR(t.date), MONTH(t.date) " +
           "ORDER BY YEAR(t.date) ASC, MONTH(t.date) ASC")
    List<MonthlyStat> getMonthlyStats(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}