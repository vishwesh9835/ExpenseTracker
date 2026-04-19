package com.example.expense_tracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expense_tracker.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey")
    List<Budget> getBudgetsForMonth(String monthKey);

    @Query("SELECT * FROM budgets WHERE category = :category AND monthKey = :monthKey LIMIT 1")
    Budget getBudgetForCategory(String category, String monthKey);

    @Query("DELETE FROM budgets WHERE monthKey = :monthKey")
    void deleteAllForMonth(String monthKey);
}
