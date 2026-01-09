package com.myfinance.api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myfinance.api.model.CategoryStat;
import com.myfinance.api.model.DashboardSummary;
import com.myfinance.api.model.MonthlyStat;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.TransactionRepository;
import com.myfinance.api.repository.UserRepository; // <--- 1. Importar esto

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/summary")
    public DashboardSummary getMonthlySummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        // Consultas a la base de datos
        BigDecimal income = transactionRepository.calculateTotalByTypeAndDateRange(currentUser.getId(), "INGRESO", startOfMonth, endOfMonth);
        BigDecimal expense = transactionRepository.calculateTotalByTypeAndDateRange(currentUser.getId(), "GASTO", startOfMonth, endOfMonth);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        BigDecimal balance = income.subtract(expense);

        return new DashboardSummary(income, expense, balance);
    }

    @GetMapping("/charts")
    public ResponseEntity<List<CategoryStat>> getChartData() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // O usa userRepository.findByEmail si prefieres el método seguro que pusimos antes
        
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        List<CategoryStat> stats = transactionRepository.getExpensesByCategory(
            currentUser.getId(), 
            startOfMonth, 
            endOfMonth
        );
        
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/history")
    public ResponseEntity<List<MonthlyStat>> getHistory() {
        User currentUser = getCurrentUser();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        
        List<MonthlyStat> stats = transactionRepository.getMonthlyStats(currentUser.getId(), sixMonthsAgo);
        return ResponseEntity.ok(stats);
    }
}