package com.myfinance.api.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BudgetProgress {
    private BigDecimal limitAmount;     
    private BigDecimal spentAmount;      
    private BigDecimal remainingAmount;  
    private String categoryName;         
}