package com.expensetracker.dto;

import java.math.BigDecimal;

public class CategorySummaryDTO {

    private String category;
    private BigDecimal totalAmount;
    private long transactionCount;
    private double percentageOfTotal;

    public CategorySummaryDTO(String category, BigDecimal totalAmount, long transactionCount, double percentageOfTotal) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getCategory() { return category; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public long getTransactionCount() { return transactionCount; }
    public double getPercentageOfTotal() { return percentageOfTotal; }
}
