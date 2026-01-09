package com.myfinance.api.model;

import java.math.BigDecimal;

public class MonthlyStat {
    private int year;
    private int month;
    private BigDecimal income;
    private BigDecimal expense;

    public MonthlyStat(int year, int month, BigDecimal income, BigDecimal expense) {
        this.year = year;
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getIncome() { return income; }
    public BigDecimal getExpense() { return expense; }
}