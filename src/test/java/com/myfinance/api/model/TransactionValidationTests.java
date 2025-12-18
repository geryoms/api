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

public class TransactionValidationTests {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenTransactionHasNoAccount_thenValidationFails() {

        Transaction transaction = new Transaction();
        transaction.setDescription("Compra válida");
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setDate(LocalDate.now());
        transaction.setType("GASTO");
        transaction.setUser(new User());

        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

        assertFalse(violations.isEmpty(), "La transacción debería ser inválida sin una cuenta");
        
        boolean hasAccountError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("account"));
        
        assertTrue(hasAccountError, "Debe haber un error de validación en el campo 'account'");
    }
}