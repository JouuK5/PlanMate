package com.example.todolist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    fun getTodoById(id: Long): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("SELECT * FROM todo_collection")
    fun getAllTodoCollection(): Flow<List<TodoCollection>>

    @Query("SELECT * FROM todo_collection WHERE id = :id")
    fun getCollectionById(id: Long): Flow<TodoCollection>

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertTodoCollection(todoCollection: TodoCollection): Long

    @Update
    suspend fun updateTodoCollection(todoCollection: TodoCollection)

    @Update
    suspend fun favTodoCollection(todoCollection: TodoCollection)

    @Delete
    suspend fun deleteTodoCollection(todoCollection: TodoCollection)

    @Query("SELECT * FROM todos WHERE collection_id = :id")
    fun getTodoCollectionById(id: Long): Flow<List<TodoEntity>>

}