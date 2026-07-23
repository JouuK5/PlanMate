package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoCollection
import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class TodoRepoImpl(
    private val todoDAO: TodoDao
) : TodoRepo{
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        val userId = auth.currentUser?.uid ?: ""

        val todoCollection = TodoCollection(
            title = title,
            createdAt = System.currentTimeMillis(),
            userId = userId
        )
        val id = todoDAO.insertTodoCollection(todoCollection)
        return if(id>0){
            val finalCollection = todoCollection.copy(id = id)

            if(userId.isNotEmpty()){
                firestore.collection("todo_collections")
                    .document(finalCollection.id.toString())
                    .set(finalCollection)
                    .addOnFailureListener { it.printStackTrace() }
            }
            finalCollection
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
        val userId = auth.currentUser?.uid ?: ""
        val cleanedTitle = title.trimEnd()

        val todo = TodoEntity(
            collectionId = collectionId,
            title = cleanedTitle,
            description = description,
            dueDate = dueDate,
            userId = userId
        )

        val id = todoDAO.insertTodo(todo)

        return if (id>0){
            val finalTodo = todo.copy(id = id)
            if(userId.isNotEmpty()){
                firestore.collection("todo")
                    .document(finalTodo.id.toString())
                    .set(finalTodo)
                    .addOnFailureListener { it.printStackTrace() }
            }
            finalTodo
        }else{
            null
        }
    }

    override suspend fun updateTodo(todo: TodoEntity): Boolean {
        todoDAO.updateTodo(todo)

        if(todo.userId.isNotEmpty()){
            firestore.collection("todo")
                .document(todo.id.toString())
                .set(todo)
                .addOnFailureListener { it.printStackTrace() }
        }
        return true
    }

    override suspend fun updateTodoCollection(todoCollection: TodoCollection): Boolean {
        todoDAO.updateTodoCollection(todoCollection)

        if(todoCollection.userId.isNotEmpty()){
            firestore.collection("todo_collections")
                .document(todoCollection.id.toString())
                .set(todoCollection)
                .addOnFailureListener { it.printStackTrace() }
        }
        return true
    }

    override suspend fun favoriteCollection(todoCollection: TodoCollection) {
        todoDAO.favTodoCollection(todoCollection)

        if (todoCollection.userId.isNotEmpty()) {
            firestore.collection("todo_collections")
                .document(todoCollection.id.toString())
                .set(todoCollection)
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    override suspend fun deleteTodo(todo: TodoEntity) {
        todoDAO.deleteTodo(todo)

        if (todo.userId.isNotEmpty()) {
            firestore.collection("todos")
                .document(todo.id.toString())
                .delete() // Lệnh xóa của Firebase
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    override suspend fun deleteTodoCollection(todoCollection: TodoCollection) {
        todoDAO.deleteTodoCollection(todoCollection)

        if (todoCollection.userId.isNotEmpty()) {
            firestore.collection("todo_collections")
                .document(todoCollection.id.toString())
                .delete()
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    override suspend fun restoreTodo(todo: TodoEntity) {
        todoDAO.insertTodo(todo)

        if(todo.userId.isNotEmpty()){
            firestore.collection("todo")
                .document(todo.id.toString())
        }
    }

    override suspend fun clearLocalDatabase() {
        todoDAO.clearAllTodo()
        todoDAO.clearAllToDoCollection()
    }

    override suspend fun syncDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        try{
            val collectionSnapshot = firestore.collection("todo_collections")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val collections = collectionSnapshot.toObjects(TodoCollection::class.java)
            collections.forEach { collection ->
                todoDAO.insertTodoCollection(collection)
            }

            val todosSnapshot = firestore.collection("todo")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val todos = todosSnapshot.toObjects(TodoEntity::class.java)
            todos.forEach { todo ->
                todoDAO.insertTodo(todo)
            }
        } catch (e: Exception){
            android.util.Log.e("LOI_SYNC_DATA", "Lý do tải xịt: ${e.message}", e)
            e.printStackTrace()
        }
    }
}