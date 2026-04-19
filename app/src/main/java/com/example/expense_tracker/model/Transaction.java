package com.example.expense_tracker.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type;      // "Income" or "Expense"
    private double amount;
    private String category;
    private String date;      // "dd-MM-yyyy"
    private String note;
    private boolean isRecurring; // monthly recurring flag

    public Transaction(String type, double amount, String category, String date, String note, boolean isRecurring) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.isRecurring = isRecurring;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    /** Display label: "Income" or the expense category */
    public String getDisplayLabel() {
        if ("Income".equalsIgnoreCase(type)) return "Income";
        return (category != null && !category.isEmpty()) ? category : "Other";
    }

    /** Returns month-year key "MM-yyyy" from date "dd-MM-yyyy" */
    public String getMonthKey() {
        if (date == null || date.length() < 7) return "";
        // date format: dd-MM-yyyy → parts[1]-parts[2]
        String[] parts = date.split("-");
        if (parts.length == 3) return parts[1] + "-" + parts[2];
        return "";
    }
}
