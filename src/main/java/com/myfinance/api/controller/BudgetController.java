package com.myfinance.api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Budget;
import com.myfinance.api.model.BudgetProgress;
import com.myfinance.api.model.Category;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.BudgetRepository;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController extends BaseController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) {
        User currentUser = getCurrentUser();
        budget.setUser(currentUser);

        if (budget.getCategory() != null && budget.getCategory().getId() != null) {
            Category category = categoryRepository.findById(budget.getCategory().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

            if (!category.getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to the current user");
            }
            budget.setCategory(category);
        }

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndCategoryIdAndStartDate(
                currentUser.getId(), 
                budget.getCategory().getId(), 
                budget.getStartDate()
        );

        if (existingBudget.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A budget for this category and month already exists");
        }

        Budget savedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(savedBudget);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<BudgetProgress> getBudgetProgress(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        LocalDate startDate = budget.getStartDate();
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal spentAmount = transactionRepository.calculateTotalByCategoryAndDateRange(
                currentUser.getId(),
                budget.getCategory().getId(),
                startDate,
                endDate
        );

        BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);

        BudgetProgress progress = new BudgetProgress(
                budget.getAmount(),
                spentAmount,
                remainingAmount,
                budget.getCategory().getName()
        );

        return ResponseEntity.ok(progress);
    }
}