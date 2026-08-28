package com.expensetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mcc_category_mapping")
public class MccCategoryMapping {

    @Id
    @Column(length = 4)
    private String mccCode;

    @Column(length = 255)
    private String isoCategoryName;

    @Enumerated(EnumType.STRING)
    private AppCategory appCategory;

    public MccCategoryMapping() {}

    public MccCategoryMapping(String mccCode, String isoCategoryName, AppCategory appCategory) {
        this.mccCode = mccCode;
        this.isoCategoryName = isoCategoryName;
        this.appCategory = appCategory;
    }

    public String getMccCode() { return mccCode; }
    public void setMccCode(String mccCode) { this.mccCode = mccCode; }

    public String getIsoCategoryName() { return isoCategoryName; }
    public void setIsoCategoryName(String isoCategoryName) { this.isoCategoryName = isoCategoryName; }

    public AppCategory getAppCategory() { return appCategory; }
    public void setAppCategory(AppCategory appCategory) { this.appCategory = appCategory; }
}
