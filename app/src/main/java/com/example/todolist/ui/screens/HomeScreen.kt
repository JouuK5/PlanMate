package com.example.todolist.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.todolist.ui.components.AddTaskBottomSheet
import com.example.todolist.ui.components.CollectionCard
import com.example.todolist.ui.components.ExpandableFab
import com.example.todolist.ui.components.FlowDoDrawer
import com.example.todolist.ui.components.FlowDoTopBar
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.theme.TextPrimary
import com.example.todolist.ui.viewmodels.TodoViewModel
import kotlinx.coroutines.launch
import com.example.todolist.R
import com.example.todolist.navigation.Screen
import com.example.todolist.ui.components.AddToCollectionSheet
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: TodoViewModel,
    onNavigateToAllCollections: () -> Unit,
    onNavigateToCollectionDetail: (Long) -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val collections by viewModel.collections.collectAsState()
    val allTodos by viewModel.todos.collectAsState()

    val favoriteCollections = collections.filter { it.isFavCollection }
    val todayTodos = remember(allTodos) {
        // Lấy ngày hiện tại theo format "yyyyMMdd" để dễ so sánh
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayString = dateFormat.format(Date())

        allTodos.filter { todo ->
            // Kiểm tra xem task có dueDate không và dueDate đó có phải hôm nay không
            todo.dueDate != null && dateFormat.format(Date(todo.dueDate)) == todayString
        }.sortedBy { it.isCompleted } // Đẩy task đã xong xuống cuối
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Đã cấp quyền thành công
        } else {
            // Người dùng từ chối
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_lottie_cat))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    var showIndependentTaskSheet by remember { mutableStateOf(false) }
    var showCollectionTaskSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FlowDoDrawer(
                currentRoute = Screen.Home,
                onNavigateToHome = {
                    scope.launch { drawerState.close() }
                },
                onNavigateToAll = {
                    scope.launch { drawerState.close() }
                    onNavigateToAllCollections()
                },
                onNavigateToInbox = {
                    scope.launch { drawerState.close() }
                    val inboxId = collections.find { it.title == "Task" }?.id ?: -1L
                    onNavigateToCollectionDetail(inboxId)
                },
                onNavigateToCalendar = {
                    scope.launch { drawerState.close() }
                    onNavigateToCalendar()
                },
                onNavigateToFav = {
                    scope.launch { drawerState.close() }
                    onNavigateToFavorite()
                },
                onNavigateToLogin = {
                    scope.launch { drawerState.close() }
                    onNavigateToLogin()
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Today Tasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if(todayTodos.isEmpty()){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.size(300.dp).offset(y=(-40).dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ZzzZzzzzZZ",
                                style= MaterialTheme.typography.headlineMedium,
                                color = TextSecondary,
                                fontSize = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Kiếm cái gì làm đii OwO!",
                                style= MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(todayTodos) { todo ->
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
                                                    color = Color.Gray.copy(alpha = 0.5f),
                                                    start = Offset(x = paddingLeft, y = size.height / 2),
                                                    end = Offset(x = size.width - paddingRight, y = size.height / 2),
                                                    strokeWidth = 2.dp.toPx()
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
                                        IconButton(onClick = { viewModel.toggleTodoState(todo) }) {
                                            Icon(
                                                imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                                contentDescription = "Toggle",
                                                // Nếu bạn có màu CyanPrimary ở theme, có thể thay Color.Cyan bằng CyanPrimary
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

                            // Đệm cuối trang để không bị che bởi FAB
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }

                }

                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { isFabExpanded = false }
                    )
                }
            }
        }
        AddTaskBottomSheet(
            showSheet = showIndependentTaskSheet,
            onDismissRequest = { showIndependentTaskSheet = false },
            onAddTask = { taskTitle, dueDate ->
                viewModel.addTodo(title = taskTitle, exactDueDate = dueDate)
            }
        )
        AddToCollectionSheet(
            showSheet = showCollectionTaskSheet,
            collections = collections,
            onDismissRequest = { showCollectionTaskSheet = false },
            onAddTask = { taskTitle, collectionId, dueDate ->
                viewModel.addTodo(collectionId = collectionId, title = taskTitle, description = "", exactDueDate = dueDate)
            }
        )
    }
}