package com.example.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.data.local.TodoCollection
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.TextPrimary
import com.example.todolist.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionSheet(
    showSheet: Boolean,
    collections: List<TodoCollection>,
    onDismissRequest: () -> Unit,
    onAddTask: (String, Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Biến quản lý trạng thái đang ở Bước 1 (Chọn thư mục) hay Bước 2 (Gõ chữ)
    var step by remember(showSheet) { mutableStateOf(1) }
    var selectedCollection by remember { mutableStateOf<TodoCollection?>(null) }
    var taskTitle by remember { mutableStateOf("") }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                // Reset lại khi đóng
                step = 1
                taskTitle = ""
                selectedCollection = null
                onDismissRequest()
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().imePadding().padding(bottom = 24.dp)
            ) {
                if (step == 1) {
                    // ================= BƯỚC 1: HIỂN THỊ DANH SÁCH THƯ MỤC =================
                    Text(
                        text = "Thêm vào danh sách nào?",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(collections.filter { it.title != "Hộp thư đến" }) { collection ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCollection = collection
                                        step = 2 // Chuyển sang bước 2
                                    }
                                    .padding(16.dp)
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null, tint = CyanPrimary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = collection.title, color = TextPrimary)
                            }
                        }
                    }
                } else {
                    // ================= BƯỚC 2: Ô NHẬP TEXT ĐÃ KHÓA THƯ MỤC =================
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // Hiển thị thẻ tag tên thư mục ở trên để người dùng biết họ đang gõ vào đâu
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Lưu vào: ${selectedCollection?.title}",
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        TextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            placeholder = { Text("Nhập công việc...", color = TextSecondary) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(
                                onClick = {
                                    if (taskTitle.isNotBlank() && selectedCollection != null) {
                                        onAddTask(taskTitle, selectedCollection!!.id)
                                        taskTitle = ""
                                        step = 1
                                        onDismissRequest()
                                    }
                                },
                                modifier = Modifier.background(if (taskTitle.isNotBlank()) CyanPrimary else TextSecondary.copy(alpha = 0.5f), CircleShape).size(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Add Task", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}