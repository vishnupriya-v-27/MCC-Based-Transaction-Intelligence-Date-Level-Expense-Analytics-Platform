package com.expensetracker.service;

import com.expensetracker.dto.CategorySummaryDTO;
import com.expensetracker.dto.ReportSummaryDTO;
import com.expensetracker.entity.AppCategory;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ReportSummaryDTO buildOverallReport() {
        List<Transaction> all = transactionRepository.findAll();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        long needsReviewCount = 0;

        Map<AppCategory, BigDecimal> categoryTotals = new EnumMap<>(AppCategory.class);
        Map<AppCategory, Long> categoryCounts = new EnumMap<>(AppCategory.class);

        for (Transaction t : all) {
            if (t.getType() == TransactionType.CREDIT) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
            if (t.getAppCategory() == AppCategory.NEEDS_REVIEW) {
                needsReviewCount++;
            }
            AppCategory cat = t.getAppCategory() != null ? t.getAppCategory() : AppCategory.NEEDS_REVIEW;
            categoryTotals.merge(cat, t.getAmount(), BigDecimal::add);
            categoryCounts.merge(cat, 1L, Long::sum);
        }

        BigDecimal totalAll = totalIncome.add(totalExpenses);
        List<CategorySummaryDTO> breakdown = new ArrayList<>();
        for (Map.Entry<AppCategory, BigDecimal> entry : categoryTotals.entrySet()) {
            double pct = totalAll.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    entry.getValue().divide(totalAll, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            breakdown.add(new CategorySummaryDTO(
                    entry.getKey().getDisplayName(),
                    entry.getValue(),
                    categoryCounts.get(entry.getKey()),
                    Math.round(pct * 100.0) / 100.0
            ));
        }
        breakdown.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        return new ReportSummaryDTO(
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses),
                all.size(),
                needsReviewCount,
                breakdown
        );
    }
}
