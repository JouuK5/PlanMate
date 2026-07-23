package com.example.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    showSheet: Boolean,
    initialTodo: TodoEntity? = null,
    onDismissRequest: () -> Unit,
    onAddTask: (String, Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var taskTitle by remember { mutableStateOf("") }

    // Quản lý trạng thái Chọn Ngày
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // Quản lý trạng thái Chọn Giờ
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    var isTimeSelected by remember { mutableStateOf(false) }

    LaunchedEffect(initialTodo, showSheet) {
        if (showSheet) {
            taskTitle = initialTodo?.title ?: ""
            selectedDateMillis = initialTodo?.dueDate

            // Nếu task cũ có giờ, bóc tách giờ phút ra để setup cho TimePicker
            if (initialTodo?.dueDate != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = initialTodo.dueDate }
                isTimeSelected = true
                // Lưu ý: TimePickerState chưa hỗ trợ thay đổi trực tiếp qua biến,
                // bạn có thể bỏ qua bước set initialHour/Minute hoặc tạo lại trạng thái nếu cần.
            } else {
                isTimeSelected = false
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                // Reset lại toàn bộ form khi đóng
                taskTitle = ""
                selectedDateMillis = null
                isTimeSelected = false
                onDismissRequest()
            },
            sheetState = sheetState,
            containerColor = Color.White,
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
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
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

                    // Nút chọn Ngày
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Chọn ngày",
                            tint = if (selectedDateMillis != null) CyanPrimary else TextSecondary
                        )
                    }

                    // Nút chọn Giờ (Chỉ hiển thị nút này đẹp nhất khi đã chọn Ngày)
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Chọn giờ",
                            tint = if (isTimeSelected) CyanPrimary else TextSecondary
                        )
                    }

                    // Hiển thị text Ngày Giờ người dùng đã chọn ra màn hình
                    if (selectedDateMillis != null) {
                        // Compose DatePicker trả về UTC, phải ép về UTC để format text hiển thị cho đúng
                        val formatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
                        var dateTimeText = formatter.format(Date(selectedDateMillis!!))

                        if (isTimeSelected) {
                            val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                            val tempCal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                            }
                            dateTimeText += " - ${timeFormatter.format(tempCal.time)}"
                        }

                        Text(
                            text = dateTimeText,
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

                                // === BƯỚC 5: GOM NGÀY VÀ GIỜ HOẶC CHỈ XỬ LÝ GIỜ ===
                                val finalDateMillis = if (selectedDateMillis != null) {
                                    // Trường hợp 1: Có chọn Ngày (Giữ nguyên logic cũ)
                                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = selectedDateMillis!!
                                    }
                                    val localCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))

                                        if (isTimeSelected) {
                                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                            set(Calendar.MINUTE, timePickerState.minute)
                                        } else {
                                            set(Calendar.HOUR_OF_DAY, 8)
                                            set(Calendar.MINUTE, 0)
                                        }
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    localCalendar.timeInMillis
                                }
                                else if (isTimeSelected) {
                                    // Trường hợp 2: KHÔNG chọn Ngày, CHỈ chọn Giờ
                                    val now = Calendar.getInstance()
                                    val targetTime = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                        set(Calendar.MINUTE, timePickerState.minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    // Nếu giờ bạn chọn đã trôi qua so với hiện tại -> Tự động dời sang giờ đó của NGÀY MAI
                                    if (targetTime.timeInMillis <= now.timeInMillis) {
                                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    targetTime.timeInMillis
                                }
                                else {
                                    // Trường hợp 3: Không chọn cả Ngày lẫn Giờ
                                    null
                                }

                                // Gọi ViewModel lưu trữ
                                onAddTask(taskTitle, finalDateMillis)

                                // Reset form và đóng
                                taskTitle = ""
                                selectedDateMillis = null
                                isTimeSelected = false
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier
                            .background(
                                if (taskTitle.isNotBlank()) CyanPrimary else TextSecondary.copy(alpha = 0.5f),
                                CircleShape
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

        // Hộp thoại Chọn Ngày
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("Xác nhận", color = CyanPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Hủy", color = TextSecondary) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Hộp thoại Chọn Giờ
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text(text = "Chọn giờ nhắc nhở") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        isTimeSelected = true
                        showTimePicker = false
                    }) { Text("Xác nhận", color = CyanPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Hủy", color = TextSecondary) }
                }
            )
        }
    }
}