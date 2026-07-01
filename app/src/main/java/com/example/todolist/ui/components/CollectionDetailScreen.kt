package com.example.todolist.ui.screens

import android.util.Log.d
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.todolist.ui.components.AddTaskBottomSheet
import com.example.todolist.ui.components.FlowDoBackTopBar
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.viewmodels.TodoViewModel

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

    var showAddTaskSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FlowDoBackTopBar(
                title = currentCollection?.title ?: "Danh sách công việc",
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskSheet = true },
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
                // Hiển thị danh sách các task
                items(collectionTodos) { todo ->
                    Card(
                        // Làm mờ nền thẻ một chút nếu đã hoàn thành
                        colors = CardDefaults.cardColors(
                            containerColor = if (todo.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithContent {
                                // 1. Vẽ nội dung của thẻ bình thường trước
                                drawContent()

                                // 2. Nếu đã tick, vẽ thêm một đường kẻ ngang đè lên trên cùng
                                if (todo.isCompleted) {
                                    val paddingLeft = 56.dp.toPx()  // Tầm 56dp là vừa vặn để né cái nút tròn check bên trái
                                    val paddingRight = 16.dp.toPx()
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.5f), // Màu xám nhạt
                                        start = Offset(x = paddingLeft, y = size.height / 2), // Bắt đầu từ mép trái, giữa chiều cao
                                        end = Offset(x = size.width - paddingRight, y = size.height / 2), // Kéo tới mép phải
                                        strokeWidth = 2.dp.toPx() // Độ dày của nét gạch
                                    )
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // Nút Tick Hình Tròn
                            IconButton(onClick = { viewModel.toggleTodoState(todo) }) {
                                Icon(
                                    imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = "Toggle",
                                    tint = if (todo.isCompleted) CyanPrimary else Color.Gray
                                )
                            }

                            // Text không cần textDecoration gạch ngang nữa vì đã có vạch kẻ của Card lo
                            Text(
                                text = todo.title,
                                color = if (todo.isCompleted) Color.Gray else Color.Black,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Đệm một khoảng ở cuối để list không bị chìm dưới nút FAB
        }

        // Tái sử dụng Bottom Sheet để thêm Task
        AddTaskBottomSheet(
            showSheet = showAddTaskSheet,
            onDismissRequest = { showAddTaskSheet = false },
            onAddTask = { taskTitle, dueDate ->
                viewModel.addTodo(collectionId = collectionId, title = taskTitle, description = "", dueDate = dueDate)
            }
        )
    }
}