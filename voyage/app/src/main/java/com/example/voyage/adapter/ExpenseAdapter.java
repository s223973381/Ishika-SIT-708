package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.BudgetExpense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(BudgetExpense expense);
    }

    private List<BudgetExpense> expenses = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public ExpenseAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setExpenses(List<BudgetExpense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        BudgetExpense expense = expenses.get(position);

        h.tvCategoryEmoji.setText(categoryEmoji(expense.category));
        h.tvTitle.setText(expense.title != null ? expense.title : "");
        h.tvCategory.setText(expense.category != null ? capitalize(expense.category) : "");
        h.tvDate.setText(expense.date != null ? expense.date : "");
        h.tvAmount.setText(String.format("$%.2f", expense.amount));

        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(expense);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    private String categoryEmoji(String cat) {
        if (cat == null) return "📦";
        switch (cat.toLowerCase()) {
            case "food": return "🍔";
            case "transport": return "🚌";
            case "hotel": return "🏨";
            case "activity": return "🎭";
            default: return "📦";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryEmoji, tvTitle, tvCategory, tvDate, tvAmount, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvCategoryEmoji = v.findViewById(R.id.tvCategoryEmoji);
            tvTitle = v.findViewById(R.id.tvExpenseTitle);
            tvCategory = v.findViewById(R.id.tvExpenseCategory);
            tvDate = v.findViewById(R.id.tvExpenseDate);
            tvAmount = v.findViewById(R.id.tvExpenseAmount);
            btnDelete = v.findViewById(R.id.btnDeleteExpense);
        }
    }
}
