package com.myfinance.api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myfinance.api.model.DashboardSummary;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.TransactionRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/summary")
    public DashboardSummary getMonthlySummary() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal income = transactionRepository.calculateTotalByTypeAndDateRange(currentUser.getId(), "INGRESO", startOfMonth, endOfMonth);
        BigDecimal expense = transactionRepository.calculateTotalByTypeAndDateRange(currentUser.getId(), "GASTO", startOfMonth, endOfMonth);
        BigDecimal balance = income.subtract(expense);

        return new DashboardSummary(income, expense, balance);
    }
}