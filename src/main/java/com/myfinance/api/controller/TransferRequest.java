package com.myfinance.api.controller;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull
    private Long originAccountId;

    @NotNull
    private Long destinationAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "El monto debe ser positivo")
    private BigDecimal amount;
}