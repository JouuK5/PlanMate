package com.example.todolist.ui.screens

import android.util.Log.d
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.ui.components.AddTaskBottomSheet
import com.example.todolist.ui.components.FlowDoBackTopBar
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.viewmodels.TodoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    viewModel: TodoViewModel,
    collectionId: Long?,
    onNavigateBack: () -> Unit
) {
    // 1. Lấy dữ liệu từ ViewModel
    val allTodos by viewModel.todos.collectAsState()
    val allCollections by viewModel.collections.collectAsState()

    // 2. Lọc dữ liệu cho riêng thư mục này
    val currentCollection = allCollections.find { it.id == collectionId }

    val collectionTodos = allTodos
        .filter { it.collectionId == collectionId }
        .sortedBy { it.isCompleted }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var selectedTodoToEdit by remember { mutableStateOf<TodoEntity?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            FlowDoBackTopBar(
                title = currentCollection?.title ?: "Danh sách công việc",
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddTaskSheet = true
                    selectedTodoToEdit = null
                },
                shape = CircleShape,
                containerColor = CyanPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .offset(y=(-40).dp)
                    .size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm Task")
            }
        },
        containerColor = LightBlueBackground
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (collectionTodos.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có công việc nào ở đây cả!\nHãy bấm nút + để thêm nhé.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            } else {
                // Hiển thị danh sách các task - QUAN TRỌNG: Thêm key = { it.id }
                items(items = collectionTodos, key = { it.id }) { todo ->

                    // 1. Khai báo trạng thái vuốt (Dùng SwipeToDismissBoxValue chuẩn của Material 3)
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            // EndToStart là vuốt từ Phải sang Trái, StartToEnd là từ Trái sang Phải
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                // Xóa task
                                viewModel.deleteTodo(todo)

                                // Hiện thanh thông báo hoàn tác
                                coroutineScope.launch {
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = "Đã xóa công việc",
                                        actionLabel = "Hoàn tác",
                                        duration = SnackbarDuration.Short
                                    )

                                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreTodo(todo)
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    // 2. Bọc Card bằng SwipeToDismissBox
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            // Settled nghĩa là trạng thái đứng yên bình thường (không bị vuốt)
                            val color = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                Color.Red.copy(alpha = 0.6f)
                            } else {
                                Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, shape = CardDefaults.shape)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        content = {
                            // Đây là giao diện Card gốc của bạn
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (todo.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawWithContent {
                                        drawContent()
                                        if (todo.isCompleted) {
                                            val paddingLeft = 56.dp.toPx()
                                            val paddingRight = 16.dp.toPx()
                                            drawLine(
                                                color = CyanPrimary,
                                                start = Offset(x = paddingLeft, y = size.height / 2),
                                                end = Offset(x = size.width - paddingRight, y = size.height / 2),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    },
                                onClick = {
                                    selectedTodoToEdit = todo
                                    showAddTaskSheet = true
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    IconButton(onClick = { viewModel.toggleTodoState(todo) }) {
                                        Icon(
                                            imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Toggle",
                                            tint = if (todo.isCompleted) CyanPrimary else Color.Gray
                                        )
                                    }

                                    Text(
                                        text = todo.title,
                                        color = if (todo.isCompleted) Color.Gray else Color.Black,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        AddTaskBottomSheet(
            showSheet = showAddTaskSheet,
            initialTodo = selectedTodoToEdit,
            onDismissRequest = {
                showAddTaskSheet = false
                selectedTodoToEdit = null
            },
            onAddTask = { taskTitle, dueDate ->
                if (selectedTodoToEdit != null){
                    val updated = selectedTodoToEdit!!.copy(
                        title = taskTitle,
                        dueDate = dueDate
                    )
                    viewModel.updateTodo(updated)
                }
                else{
                    viewModel.addTodo(collectionId = collectionId, title = taskTitle, description = "", exactDueDate = dueDate)
                }
            }
        )
    }
}