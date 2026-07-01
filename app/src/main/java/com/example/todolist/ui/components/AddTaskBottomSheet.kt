package com.example.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.TextPrimary
import com.example.todolist.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    onAddTask: (String, Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var taskTitle by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                taskTitle = ""
                selectedDateMillis = null
                onDismissRequest()
            },
            sheetState = sheetState,
            containerColor = Color.White,
            // Đảm bảo BottomSheet đẩy lên khi bàn phím xuất hiện

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .imePadding()
            ) {
                // 1. Ô nhập liệu (Input Field)
                TextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    placeholder = {
                        Text("Kế hoạch hôm nay là gì?", color = TextSecondary)
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = TextSecondary)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        ),
                        focusedIndicatorColor = Color.Transparent, // Ẩn gạch chân
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Hàng công cụ (Actions Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Các icon chức năng bổ sung (Ngày, Cờ, Thư mục)
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = CyanPrimary
                        )
                    }
                    IconButton(onClick = { /* TODO: Chọn mức độ ưu tiên */ }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Chọn thời điểm hoàn thành!",
                            tint = if (selectedDateMillis != null) CyanPrimary else TextSecondary
                        )
                    }

                    if (selectedDateMillis != null) {
                        val formatter = remember {
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            )
                        }
                        Text(
                            text = formatter.format(Date(selectedDateMillis!!)),
                            color = CyanPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }


                    Spacer(modifier = Modifier.weight(1f))

                    // 3. Nút Gửi (Send Button)
                    IconButton(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                onAddTask(taskTitle, selectedDateMillis)
                                taskTitle = "" // Reset ô nhập
                                selectedDateMillis = null
                                onDismissRequest() // Đóng sheet
                            }
                        },
                        modifier = Modifier
                            .background(
                                if (taskTitle.isNotBlank()) CyanPrimary else TextSecondary.copy(
                                    alpha = 0.5f
                                ), CircleShape
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Add Task",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        // Lấy thời gian người dùng vừa chọn lưu vào biến
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("Xác nhận", color = CyanPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(
                            "Hủy",
                            color = TextSecondary
                        )
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}