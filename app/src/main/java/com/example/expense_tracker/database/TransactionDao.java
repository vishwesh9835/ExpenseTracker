package com.example.expense_tracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expense_tracker.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("DELETE FROM transactions")
    void deleteAll();

    /** All transactions, newest first */
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<Transaction> getAllTransactions();

    /**
     * Transactions for a specific month.
     * date format stored: "dd-MM-yyyy" — monthKey = "MM-yyyy"
     */
    @Query("SELECT * FROM transactions WHERE (substr(date,4,2) || '-' || substr(date,7,4)) = :monthKey ORDER BY id DESC")
    List<Transaction> getTransactionsByMonth(String monthKey);

    /** Expenses for a specific month */
    @Query("SELECT * FROM transactions WHERE type = 'Expense' AND (substr(date,4,2) || '-' || substr(date,7,4)) = :monthKey ORDER BY id DESC")
    List<Transaction> getExpensesByMonth(String monthKey);

    /** Income for a specific month */
    @Query("SELECT * FROM transactions WHERE type = 'Income' AND (substr(date,4,2) || '-' || substr(date,7,4)) = :monthKey ORDER BY id DESC")
    List<Transaction> getIncomeByMonth(String monthKey);

    /** Full-text search across category + note */
    @Query("SELECT * FROM transactions WHERE category LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY id DESC")
    List<Transaction> search(String query);

    /** Filter by type */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY id DESC")
    List<Transaction> getByType(String type);

    /** Filter by category */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY id DESC")
    List<Transaction> getByCategory(String category);

    /** Recurring transactions (for auto-insert each month) */
    @Query("SELECT * FROM transactions WHERE isRecurring = 1")
    List<Transaction> getRecurringTransactions();

    /** Total expense for a given month */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type='Expense' AND (substr(date,4,2) || '-' || substr(date,7,4)) = :monthKey")
    double getTotalExpenseForMonth(String monthKey);

    /** Distinct months available (for analytics) */
    @Query("SELECT DISTINCT (substr(date,4,2) || '-' || substr(date,7,4)) FROM transactions ORDER BY substr(date,7,4) DESC, substr(date,4,2) DESC")
    List<String> getDistinctMonths();
}
