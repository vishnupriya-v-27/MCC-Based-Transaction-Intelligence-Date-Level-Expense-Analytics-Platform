package com.expensetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate transactionDate;

    @Column(length = 500)
    private String payeeRaw;        // raw description from the statement

    @Column(length = 255)
    private String payeeNormalized;  // cleaned/normalized payee name used for cache lookups

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;    // DEBIT or CREDIT, as reported by PhonePe

    private String mccCode;          // 4-digit MCC if available from the enriched dataset

    private String isoCategoryName;  // human-readable MCC category name (e.g. "Eating Places")

    @Enumerated(EnumType.STRING)
    private AppCategory appCategory; // the bucket this app groups the transaction into

    private String categorizationSource; // CACHE, MCC, P2P, KEYWORD, HEURISTIC, MANUAL, NEEDS_REVIEW

    private boolean p2p;

    private boolean manuallyConfirmed;

    private String referenceId; // UTR / transaction ID from statement, if present

    public Transaction() {}

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getPayeeRaw() { return payeeRaw; }
    public void setPayeeRaw(String payeeRaw) { this.payeeRaw = payeeRaw; }

    public String getPayeeNormalized() { return payeeNormalized; }
    public void setPayeeNormalized(String payeeNormalized) { this.payeeNormalized = payeeNormalized; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getMccCode() { return mccCode; }
    public void setMccCode(String mccCode) { this.mccCode = mccCode; }

    public String getIsoCategoryName() { return isoCategoryName; }
    public void setIsoCategoryName(String isoCategoryName) { this.isoCategoryName = isoCategoryName; }

    public AppCategory getAppCategory() { return appCategory; }
    public void setAppCategory(AppCategory appCategory) { this.appCategory = appCategory; }

    public String getCategorizationSource() { return categorizationSource; }
    public void setCategorizationSource(String categorizationSource) { this.categorizationSource = categorizationSource; }

    public boolean isP2p() { return p2p; }
    public void setP2p(boolean p2p) { this.p2p = p2p; }

    public boolean isManuallyConfirmed() { return manuallyConfirmed; }
    public void setManuallyConfirmed(boolean manuallyConfirmed) { this.manuallyConfirmed = manuallyConfirmed; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}
