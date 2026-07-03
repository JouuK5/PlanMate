package com.example.todolist.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "todo_collection")
data class TodoCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "user_id") val userId: String = "",

    @get:PropertyName("favCollection")
    @set:PropertyName("favCollection")
    @ColumnInfo(name = "is_favorite_collection")
    var isFavCollection: Boolean = false
)
