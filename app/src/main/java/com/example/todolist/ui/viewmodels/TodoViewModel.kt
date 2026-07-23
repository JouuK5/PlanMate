package com.example.todolist.ui.viewmodels

import android.content.Context
import android.service.notification.Condition.newId
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.TodoCollection
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.repository.TodoRepo
import com.example.todolist.service.AlarmScheduler
import com.example.todolist.widget.TodoWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepo,
    private val alarmScheduler: AlarmScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {
    init {
        // Bật ăng-ten đồng bộ với Firebase ngay khi ViewModel được tạo
        viewModelScope.launch {
            repository.syncDataFromFirestore()
        }
    }

    //get all todo
    val todos: StateFlow<List<TodoEntity>> = repository.getTodo()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val collections: StateFlow<List<TodoCollection>> = repository.getTodoCollection()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    fun addTodo(collectionId: Long? = null, title: String, description: String = "", exactDueDate: Long? = null) {
        if (title.isNotBlank()) {
            viewModelScope.launch {
                val targetCollectionId = if (collectionId != null && collectionId != -1L) {
                    collectionId
                } else {
                    val collectionLists = repository.getTodoCollection().first()
                    val inbox = collectionLists.find { it.title == "Task" }
                    if (inbox != null) inbox.id else {
                        repository.insertTodoCollection("Task")
                        val updatedLists = repository.getTodoCollection().first()
                        updatedLists.find { it.title == "Task" }?.id
                    }
                }

                if (targetCollectionId != null) {
                    // Lưu Todo vào Database và nhận đối tượng trả về
                    val insertedTodo = repository.insertTodo(targetCollectionId, title, description, exactDueDate)
                    TodoWidget().updateAll(context)

                    // Lấy ID từ đối tượng trả về và đặt báo thức
                    if (insertedTodo != null && exactDueDate != null) {
                        alarmScheduler.scheduleTaskAlarm(insertedTodo.id, title, exactDueDate)
                    }
                }
            }
        }
    }

    fun toggleTodoState(todo: TodoEntity) {
        viewModelScope.launch {
            val updateTodo = todo.copy(isCompleted = !todo.isCompleted)
            repository.updateTodo(updateTodo)
            TodoWidget().updateAll(context)
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

    fun updateTodo(todo: TodoEntity){
        viewModelScope.launch {
            repository.updateTodo(todo)
            TodoWidget().updateAll(context)
        }
    }
    fun updateTodoCollection(collection: TodoCollection){
        viewModelScope.launch {
            repository.updateTodoCollection(collection)
        }
    }

    fun restoreTodo(todo: TodoEntity){
        viewModelScope.launch {
            repository.restoreTodo(todo)
            TodoWidget().updateAll(context)
        }
    }

    fun deleteTodo(todo: TodoEntity){
        viewModelScope.launch {
            repository.deleteTodo(todo)
            TodoWidget().updateAll(context)
        }
    }

}