package com.expensetracker.dto;

import com.expensetracker.entity.AppCategory;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDTO {

    private Long id;
    private LocalDate transactionDate;
    private String payee;
    private BigDecimal amount;
    private TransactionType type;
    private String mccCode;
    private String isoCategoryName;
    private AppCategory appCategory;
    private String appCategoryLabel;
    private String categorizationSource;
    private boolean manuallyConfirmed;
    private String referenceId;

    public static TransactionDTO from(Transaction t) {
        TransactionDTO dto = new TransactionDTO();
        dto.id = t.getId();
        dto.transactionDate = t.getTransactionDate();
        dto.payee = t.getPayeeRaw();
        dto.amount = t.getAmount();
        dto.type = t.getType();
        dto.mccCode = t.getMccCode();
        dto.isoCategoryName = t.getIsoCategoryName();
        dto.appCategory = t.getAppCategory();
        dto.appCategoryLabel = t.getAppCategory() != null ? t.getAppCategory().getDisplayName() : null;
        dto.categorizationSource = t.getCategorizationSource();
        dto.manuallyConfirmed = t.isManuallyConfirmed();
        dto.referenceId = t.getReferenceId();
        return dto;
    }

    // Getters (needed for Jackson serialization)
    public Long getId() { return id; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getPayee() { return payee; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getMccCode() { return mccCode; }
    public String getIsoCategoryName() { return isoCategoryName; }
    public AppCategory getAppCategory() { return appCategory; }
    public String getAppCategoryLabel() { return appCategoryLabel; }
    public String getCategorizationSource() { return categorizationSource; }
    public boolean isManuallyConfirmed() { return manuallyConfirmed; }
    public String getReferenceId() { return referenceId; }
}
