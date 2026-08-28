package com.expensetracker.entity;

public enum AppCategory {
    FOOD_AND_DINING("Food & Dining"),
    GROCERIES("Groceries"),
    TRAVEL_AND_TRANSPORT("Travel & Transport"),
    SHOPPING("Shopping"),
    BILLS_AND_UTILITIES("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    INVESTMENTS("Investments"),
    TRANSFERS_P2P("Transfers (P2P)"),
    INCOME("Income"),
    NEEDS_REVIEW("Needs Review");

    private final String displayName;

    AppCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
