package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalTransactions;
    private long needsReviewCount;
    private List<CategorySummaryDTO> categoryBreakdown;

    public ReportSummaryDTO(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal netBalance,
                             long totalTransactions, long needsReviewCount, List<CategorySummaryDTO> categoryBreakdown) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = netBalance;
        this.totalTransactions = totalTransactions;
        this.needsReviewCount = needsReviewCount;
        this.categoryBreakdown = categoryBreakdown;
    }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getNetBalance() { return netBalance; }
    public long getTotalTransactions() { return totalTransactions; }
    public long getNeedsReviewCount() { return needsReviewCount; }
    public List<CategorySummaryDTO> getCategoryBreakdown() { return categoryBreakdown; }
}
