package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "budget_expenses",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "tripId",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("tripId")
)
public class BudgetExpense {
    @PrimaryKey(autoGenerate = true)
    public int expenseId;
    public int tripId;
    public String category;   // food, transport, hotel, activity, other
    public String title;
    public double amount;
    public String date;
    public String note;
}
