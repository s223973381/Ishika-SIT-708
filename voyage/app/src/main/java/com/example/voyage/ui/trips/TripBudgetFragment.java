package com.example.voyage.ui.trips;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.ExpenseAdapter;
import com.example.voyage.database.entities.BudgetExpense;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.viewmodel.BudgetViewModel;
import com.example.voyage.viewmodel.TripViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class TripBudgetFragment extends Fragment {

    private int tripId;
    private BudgetViewModel budgetViewModel;
    private ExpenseAdapter adapter;
    private double tripBudget = 0;
    private String selectedCategory = "food";
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }
        if (tripId == -1) return;

        TextView tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        TextView tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        ProgressBar progressBudget = view.findViewById(R.id.progressBudget);
        TextView tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus);
        RecyclerView rv = view.findViewById(R.id.rvExpenses);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddExpense);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        TripViewModel tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);

        adapter = new ExpenseAdapter(expense -> budgetViewModel.deleteExpense(expense));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        tripViewModel.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            if (trip == null) return;
            tripBudget = trip.budget;
            tvTotalBudget.setText(String.format("$%.0f", trip.budget));
        });

        budgetViewModel.getExpensesForTrip(tripId).observe(getViewLifecycleOwner(), expenses -> {
            adapter.setExpenses(expenses);
            boolean empty = expenses == null || expenses.isEmpty();
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        budgetViewModel.getTotalSpentForTrip(tripId).observe(getViewLifecycleOwner(), spent -> {
            double spentVal = spent != null ? spent : 0;
            tvTotalSpent.setText(String.format("$%.2f", spentVal));
            int progress = tripBudget > 0 ? (int) ((spentVal / tripBudget) * 100) : 0;
            progressBudget.setProgress(Math.min(progress, 100));
            double remaining = tripBudget - spentVal;
            tvBudgetStatus.setText(remaining >= 0
                    ? String.format("$%.2f remaining", remaining)
                    : String.format("$%.2f over budget!", -remaining));
        });

        fab.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_expense, null);

        EditText etTitle = dialogView.findViewById(R.id.etExpenseTitle);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        TextView tvDate = dialogView.findViewById(R.id.tvExpenseDate);
        LinearLayout btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        TextView catFood = dialogView.findViewById(R.id.catFood);
        TextView catTransport = dialogView.findViewById(R.id.catTransport);
        TextView catHotel = dialogView.findViewById(R.id.catHotel);
        TextView catActivity = dialogView.findViewById(R.id.catActivity);
        TextView catOther = dialogView.findViewById(R.id.catOther);

        selectedCategory = "food";
        selectedDate = "";

        catFood.setOnClickListener(v -> setCat(catFood, catTransport, catHotel, catActivity, catOther, "food"));
        catTransport.setOnClickListener(v -> setCat(catTransport, catFood, catHotel, catActivity, catOther, "transport"));
        catHotel.setOnClickListener(v -> setCat(catHotel, catFood, catTransport, catActivity, catOther, "hotel"));
        catActivity.setOnClickListener(v -> setCat(catActivity, catFood, catTransport, catHotel, catOther, "activity"));
        catOther.setOnClickListener(v -> setCat(catOther, catFood, catTransport, catHotel, catActivity, "other"));

        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dv, y, m, d) -> {
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
                tvDate.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Expense")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) return;
                    double amount = 0;
                    try { amount = Double.parseDouble(etAmount.getText().toString().trim()); } catch (Exception ignored) {}

                    BudgetExpense expense = new BudgetExpense();
                    expense.tripId = tripId;
                    expense.title = title;
                    expense.category = selectedCategory;
                    expense.amount = amount;
                    expense.date = selectedDate;
                    expense.note = etNote.getText().toString().trim();
                    budgetViewModel.insertExpense(expense);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setCat(TextView sel, TextView a, TextView b, TextView c, TextView d, String cat) {
        selectedCategory = cat;
        TextView[] all = {sel, a, b, c, d};
        for (TextView t : all) {
            if (t == sel) {
                t.setBackgroundResource(R.drawable.bg_chip_selected);
            } else {
                t.setBackgroundResource(R.drawable.bg_chip);
            }
        }
    }
}
