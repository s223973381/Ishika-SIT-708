package com.example.voyage.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.BudgetExpenseDao;
import com.example.voyage.database.entities.BudgetExpense;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetViewModel extends AndroidViewModel {
    private final BudgetExpenseDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).budgetExpenseDao();
    }

    public LiveData<List<BudgetExpense>> getExpensesForTrip(int tripId) {
        return dao.getExpensesForTrip(tripId);
    }

    public LiveData<Double> getTotalSpentForTrip(int tripId) {
        return dao.getTotalSpentForTrip(tripId);
    }

    public LiveData<Double> getSpentByCategory(int tripId, String category) {
        return dao.getSpentByCategory(tripId, category);
    }

    public void insertExpense(BudgetExpense expense) {
        executor.execute(() -> dao.insert(expense));
    }

    public void updateExpense(BudgetExpense expense) {
        executor.execute(() -> dao.update(expense));
    }

    public void deleteExpense(BudgetExpense expense) {
        executor.execute(() -> dao.delete(expense));
    }

    public void deleteExpenseById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }
}
