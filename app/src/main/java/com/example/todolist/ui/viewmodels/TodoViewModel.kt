package com.example.todolist.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.TodoCollection
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.repository.TodoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepo
) : ViewModel() {

    //get all todo
    val todos: StateFlow<List<TodoEntity>> = repository.getTodo()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val collections: StateFlow<List<TodoCollection>> = repository.getTodoCollection()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    fun addTodo(collectionId: Long? = null, title: String, description: String = "", dueDate: Long? = null) {
        if (title.isNotBlank()) {
            viewModelScope.launch {
                // Xác định ID của danh sách cần lưu
                val targetCollectionId = if (collectionId != null && collectionId != -1L) {
                    collectionId // Nếu có truyền ID vào thì dùng luôn
                } else {
                    // Nếu không truyền ID (tức là null), tự động tìm hoặc tạo Hộp thư đến
                    val collectionLists = repository.getTodoCollection().first()
                    val inbox = collectionLists.find { it.title == "Task" }

                    if (inbox != null) {
                        inbox.id
                    } else {
                        repository.insertTodoCollection("Task")
                        // Quét lại một lần nữa sau khi chèn để lấy ID chính xác
                        val updatedLists = repository.getTodoCollection().first()
                        updatedLists.find { it.title == "Task" }?.id
                    }
                }

                // Tiến hành lưu vào Database
                if (targetCollectionId != null) {
                    repository.insertTodo(targetCollectionId, title, description, dueDate)
                }
            }
        }
    }

    fun toggleTodoState(todo: TodoEntity) {
        viewModelScope.launch {
            val updateTodo = todo.copy(isCompleted = !todo.isCompleted)
            repository.updateTodo(updateTodo)

        }
    }

    fun toggleFavState(todo: TodoEntity){
        viewModelScope.launch {
            val updateTodo = todo.copy(isFavorite = !todo.isFavorite)
            repository.updateTodo(updateTodo)
        }
    }

    fun addTodoCollection(title: String){
        if(title.isNotBlank()){
            viewModelScope.launch {
                repository.insertTodoCollection(title = title)
            }
        }
    }

    fun updateTodoCollection(collection: TodoCollection){
        viewModelScope.launch {
            repository.updateTodoCollection(collection)
        }
    }

}