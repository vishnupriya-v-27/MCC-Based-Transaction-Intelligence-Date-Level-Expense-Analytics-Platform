package com.expensetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payee_category_cache")
public class PayeeCategoryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String payeeNormalized;

    @Enumerated(EnumType.STRING)
    private AppCategory appCategory;

    private String mccCode;

    private boolean fromManualCorrection;

    public PayeeCategoryCache() {}

    public PayeeCategoryCache(String payeeNormalized, AppCategory appCategory, String mccCode, boolean fromManualCorrection) {
        this.payeeNormalized = payeeNormalized;
        this.appCategory = appCategory;
        this.mccCode = mccCode;
        this.fromManualCorrection = fromManualCorrection;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPayeeNormalized() { return payeeNormalized; }
    public void setPayeeNormalized(String payeeNormalized) { this.payeeNormalized = payeeNormalized; }

    public AppCategory getAppCategory() { return appCategory; }
    public void setAppCategory(AppCategory appCategory) { this.appCategory = appCategory; }

    public String getMccCode() { return mccCode; }
    public void setMccCode(String mccCode) { this.mccCode = mccCode; }

    public boolean isFromManualCorrection() { return fromManualCorrection; }
    public void setFromManualCorrection(boolean fromManualCorrection) { this.fromManualCorrection = fromManualCorrection; }
}
