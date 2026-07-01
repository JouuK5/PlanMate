package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoCollection
import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class TodoRepoImpl(private val todoDAO: TodoDao
) : TodoRepo{
    override fun getTodo(): Flow<List<TodoEntity>> {
        return todoDAO.getAllTodos()
    }

    override fun getTodoCollection(): Flow<List<TodoCollection>> {
        return todoDAO.getAllTodoCollection()
    }

    override fun getTodoCollectionById(id: Long): Flow<List<TodoEntity>> {
        return todoDAO.getTodoCollectionById(id)
    }

    override suspend fun insertTodoCollection(title: String): TodoCollection? {
        val todoCollection = TodoCollection(title = title, createdAt = System.currentTimeMillis())
        val id = todoDAO.insertTodoCollection(todoCollection)
        return if(id>0){
            todoCollection.copy(
                id = id
            )
        }else{
            null
        }
    }

    override suspend fun insertTodo(
        collectionId : Long,
        title: String,
        description: String,
        dueDate: Long?
    ): TodoEntity? {
        val cleanedTitle = title.trimEnd()

        val todo = TodoEntity(
            collectionId = collectionId,
            title = cleanedTitle,
            description = description,
            dueDate = dueDate
        )

        val id = todoDAO.insertTodo(todo)
        return if (id>0){
            todo.copy(id = id)
        }else{
            null
        }
    }

    override suspend fun updateTodo(todo: TodoEntity): Boolean {
        todoDAO.updateTodo(todo)
        return true
    }

    override suspend fun updateTodoCollection(todoCollection: TodoCollection): Boolean {
        todoDAO.updateTodoCollection(todoCollection)
        return true
    }

    override suspend fun favoriteCollection(todoCollection: TodoCollection) {
        todoDAO.favTodoCollection(todoCollection)
    }

    override suspend fun deleteTodo(todo: TodoEntity) {
        todoDAO.deleteTodo(todo)
    }

    override suspend fun deleteTodoCollection(todoCollection: TodoCollection) {
        todoDAO.deleteTodoCollection(todoCollection)
    }
}