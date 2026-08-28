package com.expensetracker.dto;

import com.expensetracker.entity.AppCategory;
import jakarta.validation.constraints.NotNull;

public class CategoryUpdateRequest {

    @NotNull
    private AppCategory appCategory;

    private boolean applyToAllFromSamePayee = true;

    public AppCategory getAppCategory() { return appCategory; }
    public void setAppCategory(AppCategory appCategory) { this.appCategory = appCategory; }

    public boolean isApplyToAllFromSamePayee() { return applyToAllFromSamePayee; }
    public void setApplyToAllFromSamePayee(boolean applyToAllFromSamePayee) { this.applyToAllFromSamePayee = applyToAllFromSamePayee; }
}
