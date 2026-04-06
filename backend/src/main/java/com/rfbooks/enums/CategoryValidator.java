package com.rfbooks.enums;

import java.util.Set;

public final class CategoryValidator {

    public static final Set<String> EXPENSE_CATEGORIES = Set.of(
        "utilities", "maintenance", "supplies", "food_beverage",
        "marketing", "insurance", "payroll", "taxes",
        "rent", "professional_services", "other"
    );

    public static final Set<String> INCOME_CATEGORIES = Set.of(
        "room_revenue", "food_beverage", "activities", "merchandise",
        "rentals", "parking", "late_fees", "cancellation_fees",
        "deposits", "other_revenue"
    );

    public static final Set<String> PAYMENT_METHODS = Set.of(
        "cash", "check", "card", "credit_card", "debit_card", "ach", "other"
    );

    private CategoryValidator() {}

    public static boolean isValidExpenseCategory(String category) {
        return category == null || EXPENSE_CATEGORIES.contains(category.toLowerCase().replace(' ', '_'));
    }

    public static boolean isValidIncomeCategory(String category) {
        return category == null || INCOME_CATEGORIES.contains(category.toLowerCase().replace(' ', '_'));
    }

    public static boolean isValidPaymentMethod(String method) {
        return method == null || method.isEmpty() || PAYMENT_METHODS.contains(method.toLowerCase().replace(' ', '_'));
    }
}
