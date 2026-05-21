package com.example.voyage.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "packing_list_items",
    foreignKeys = @ForeignKey(
        entity = PackingList.class,
        parentColumns = "listId",
        childColumns = "listId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("listId")
)
public class PackingListItem {
    @PrimaryKey(autoGenerate = true)
    public int itemId;
    public int listId;
    public String itemName;
    public String category;
    public boolean isPacked;
}
