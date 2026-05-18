package com.example.voyage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.voyage.database.entities.BudgetExpense;
import com.example.voyage.database.pojo.TripSpentSummary;
import java.util.List;

@Dao
public interface BudgetExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetExpense expense);

    @Update
    void update(BudgetExpense expense);

    @Delete
    void delete(BudgetExpense expense);

    @Query("SELECT * FROM budget_expenses WHERE tripId = :tripId ORDER BY date DESC")
    LiveData<List<BudgetExpense>> getExpensesForTrip(int tripId);

    @Query("SELECT SUM(amount) FROM budget_expenses WHERE tripId = :tripId")
    LiveData<Double> getTotalSpentForTrip(int tripId);

    @Query("SELECT SUM(amount) FROM budget_expenses WHERE tripId = :tripId AND category = :category")
    LiveData<Double> getSpentByCategory(int tripId, String category);

    @Query("DELETE FROM budget_expenses WHERE expenseId = :expenseId")
    void deleteById(int expenseId);

    @Query("SELECT tripId, SUM(amount) as totalSpent FROM budget_expenses GROUP BY tripId")
    LiveData<List<TripSpentSummary>> getAllTripSpentTotals();
}
