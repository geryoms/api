package com.myfinance.api.model;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class AccountValidationTests {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAccountNameIsBlank_thenValidationFails() {
        Account account = new Account();
        account.setName("");
        account.setInitialBalance(BigDecimal.TEN);
        account.setUser(new User()); 

        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        assertFalse(violations.isEmpty(), "Debería fallar la validación por nombre en blanco");
    }
}