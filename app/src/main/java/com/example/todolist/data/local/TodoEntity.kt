package com.example.todolist.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = TodoCollection::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("collection_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "collection_id", index = true)
    val collectionId: Long? = 0L,
    @ColumnInfo
    val title: String = "",
    @ColumnInfo(name = "description")
    val description: String? = null,

    @get:PropertyName("Completed")
    @set:PropertyName("Completed")
    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,


    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "due_at")
    val dueDate: Long? = null,

    @ColumnInfo(name = "user_id")
    val userId: String = ""
)