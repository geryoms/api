package com.myfinance.api.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class BudgetValidationTests {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenBudgetHasNoCategory_thenValidationFails() {
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("500.00"));
        budget.setStartDate(LocalDate.now());
        budget.setUser(new User());

        Set<ConstraintViolation<Budget>> violations = validator.validate(budget);

        assertFalse(violations.isEmpty(), "El presupuesto no debe ser válido sin categoría");
        boolean hasCategoryError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("category"));
        assertTrue(hasCategoryError, "Debe fallar por falta de categoría");
    }

    @Test
    void whenBudgetAmountIsNegative_thenValidationFails() {
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("-10.00"));
        budget.setStartDate(LocalDate.now());
        budget.setCategory(new Category());
        budget.setUser(new User());

        Set<ConstraintViolation<Budget>> violations = validator.validate(budget);

        boolean hasAmountError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        assertTrue(hasAmountError, "El monto debe ser positivo");
    }
}