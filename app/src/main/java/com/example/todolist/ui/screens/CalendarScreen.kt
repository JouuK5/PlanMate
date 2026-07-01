package com.example.todolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.navigation.Screen
import com.example.todolist.ui.components.*
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.theme.TextPrimary
import com.example.todolist.ui.theme.TextSecondary
import com.example.todolist.ui.viewmodels.TodoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CalendarScreen(
    viewModel: TodoViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAllCollections: () -> Unit,
    onNavigateToCollectionDetail: (Long) -> Unit
) {
    val collections by viewModel.collections.collectAsState()
    val allTodos by viewModel.todos.collectAsState()

    // Lưu trữ ngày đang được chọn trên lịch (Mặc định là hôm nay)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Lọc task theo ngày được chọn
    val tasksForSelectedDate = remember(allTodos, selectedDate) {
        allTodos.filter { todo ->
            if (todo.dueDate == null) return@filter false
            val todoDate = java.time.Instant.ofEpochMilli(todo.dueDate)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
            todoDate == selectedDate
        }.sortedBy { it.isCompleted }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }

    var showIndependentTaskSheet by remember { mutableStateOf(false) }
    var showCollectionTaskSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FlowDoDrawer(
                currentRoute = Screen.Calendar,
                onNavigateToHome = {
                    scope.launch { drawerState.close() }
                    onNavigateToHome()
                },
                onNavigateToCalendar = {
                    scope.launch { drawerState.close() }
                    // Đang ở lịch rồi nên không cần làm gì
                },
                onNavigateToAll = {
                    scope.launch { drawerState.close() }
                    onNavigateToAllCollections()
                },
                onNavigateToInbox = {
                    scope.launch { drawerState.close() }
                    val inboxId = collections.find { it.title == "Task" }?.id ?: -1L
                    onNavigateToCollectionDetail(inboxId)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                FlowDoTopBar(onOpenDrawer = { scope.launch { drawerState.open() } })
            },
            floatingActionButton = {
                ExpandableFab(
                    expanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    onCreateIndependent = {
                        isFabExpanded = false
                        showIndependentTaskSheet = true
                    },
                    onAddCollection = {
                        isFabExpanded = false
                        showCollectionTaskSheet = true
                    }
                )
            },
            containerColor = LightBlueBackground
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                // NỘI DUNG CHÍNH: CUỘN ĐƯỢC TOÀN BỘ
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Text(text = "Lịch trình", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    // 1. CHÈN BỘ LỊCH VÀO ĐÂY
                    item {
                        TaskCalendarView(
                            allTodos = allTodos,
                            onDateSelected = { newDate ->
                                selectedDate = newDate // Cập nhật ngày được chọn để tự động load lại danh sách bên dưới
                            }
                        )
                    }

                    item {
                        Text(
                            text = "Công việc ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}",
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 2. DANH SÁCH TASK CỦA NGÀY ĐÓ
                    if (tasksForSelectedDate.isEmpty()) {
                        item {
                            Text(
                                text = "Không có công việc nào trong ngày này.",
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(tasksForSelectedDate) { todo ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (todo.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawWithContent {
                                        drawContent()
                                        if (todo.isCompleted) {
                                            drawLine(
                                                color = Color.Gray.copy(alpha = 0.5f),
                                                start = Offset(x = 56.dp.toPx(), y = size.height / 2),
                                                end = Offset(x = size.width - 16.dp.toPx(), y = size.height / 2),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    IconButton(onClick = { viewModel.toggleTodoState(todo) }) {
                                        Icon(
                                            imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Toggle",
                                            tint = if (todo.isCompleted) Color.Cyan else Color.Gray
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
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

                // Lớp mờ khi mở FAB
                AnimatedVisibility(visible = isFabExpanded, enter = fadeIn(), exit = fadeOut()) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.8f))
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { isFabExpanded = false })
                }
            }
        }

        // Đảm bảo khi tạo task từ màn hình này, nó được gán đúng vào ngày đang chọn trên lịch
        AddTaskBottomSheet(
            showSheet = showIndependentTaskSheet,
            onDismissRequest = { showIndependentTaskSheet = false },
            onAddTask = { taskTitle, dueDate ->
                viewModel.addTodo(title = taskTitle, dueDate = dueDate)
            }
        )
        AddToCollectionSheet(
            showSheet = showCollectionTaskSheet,
            collections = collections,
            onDismissRequest = { showCollectionTaskSheet = false },
            onAddTask = { taskTitle, collectionId ->
                viewModel.addTodo(collectionId = collectionId, title = taskTitle)
            }
        )
    }
}