package com.example.expense_tracker.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores a monthly budget for a specific category (or "Overall" for total).
 * Key = category name, e.g. "Food", "Travel", "Overall"
 */
@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;    // "Overall", "Food", "Travel", etc.
    private double limitAmount; // monthly limit in ₹
    private String monthKey;    // "MM-yyyy" e.g. "04-2026"

    public Budget(String category, double limitAmount, String monthKey) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.monthKey = monthKey;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
}
