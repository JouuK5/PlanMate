package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoCollection
import com.example.todolist.data.local.TodoEntity
import kotlinx.coroutines.flow.Flow

interface TodoRepo {
    fun getTodo(): Flow<List<TodoEntity>>
    fun getTodoCollection(): Flow<List<TodoCollection>>
    fun getTodoCollectionById(id: Long): Flow<List<TodoEntity>>

    suspend fun insertTodoCollection(title: String): TodoCollection?
    suspend fun insertTodo(collectionId: Long, title: String, description: String, dueDate: Long? = null): TodoEntity?

    suspend fun updateTodo(todo: TodoEntity): Boolean
    suspend fun updateTodoCollection(todoCollection: TodoCollection): Boolean

    suspend fun deleteTodo(todo: TodoEntity)
    suspend fun deleteTodoCollection(todoCollection: TodoCollection)

    suspend fun clearLocalDatabase()

    suspend fun favoriteCollection(todoCollection: TodoCollection)

    suspend fun syncDataFromFirestore()
}