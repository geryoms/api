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

public class SubscriptionValidationTests {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenSubscriptionHasNoName_thenValidationFails() {
        Subscription subscription = new Subscription();
        subscription.setAmount(new BigDecimal("10.00"));
        subscription.setFrequency(Subscription.Frequency.MONTHLY);
        subscription.setNextPaymentDate(LocalDate.now());
        subscription.setUser(new User());
        Set<ConstraintViolation<Subscription>> violations = validator.validate(subscription);

        boolean hasNameError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameError, "El nombre es obligatorio");
    }

    @Test
    void whenAmountIsNegative_thenValidationFails() {
        Subscription subscription = new Subscription();
        subscription.setName("Spotify");
        subscription.setAmount(new BigDecimal("-5.00"));
        subscription.setFrequency(Subscription.Frequency.MONTHLY);
        subscription.setNextPaymentDate(LocalDate.now());
        subscription.setUser(new User());

        Set<ConstraintViolation<Subscription>> violations = validator.validate(subscription);

        boolean hasAmountError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        assertTrue(hasAmountError, "El monto debe ser positivo");
    }
    
    @Test
    void whenSubscriptionHasNoAccount_thenValidationFails() {
        Subscription subscription = new Subscription();
        subscription.setName("Disney+");
        subscription.setAmount(new BigDecimal("8.99"));
        subscription.setFrequency(Subscription.Frequency.MONTHLY);
        subscription.setNextPaymentDate(LocalDate.now());
        subscription.setUser(new User());

        Set<ConstraintViolation<Subscription>> violations = validator.validate(subscription);

        boolean hasAccountError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("account"));
        assertTrue(hasAccountError, "La suscripción debe tener una cuenta asociada");
    }
}