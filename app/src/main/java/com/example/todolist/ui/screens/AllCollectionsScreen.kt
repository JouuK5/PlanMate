package com.example.todolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.components.CollectionListItem
import com.example.todolist.ui.components.FlowDoBackTopBar
import com.example.todolist.ui.components.GhostAddCard
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.viewmodels.AuthViewModel
import com.example.todolist.ui.viewmodels.TodoViewModel

@Composable
fun AllCollectionsScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCollectionDetail: (Long) -> Unit,
    onlyFavorites: Boolean = false
) {
    val collections by viewModel.collections.collectAsState()
    val allTodos by viewModel.todos.collectAsState()

    val filteredCollections = if (onlyFavorites) {
        collections.filter { it.isFavCollection }
    } else {
        collections
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newCollectionTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            FlowDoBackTopBar(
                title = if (onlyFavorites) "Favorites" else "All My Lists",
                onBack = onNavigateBack
            )
        },
        containerColor = LightBlueBackground
    ) { paddingValues ->

        // Dùng LazyColumn để chứa danh sách
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách giữa các thẻ
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. In ra toàn bộ Collection có trong Database
            items(filteredCollections) { collection ->
                val itemCount = allTodos.count { it.collectionId == collection.id }
                CollectionListItem(
                    collection = collection,
                    itemCount = itemCount,
                    onClick = { onNavigateToCollectionDetail(collection.id) },
                    onToggleFavorite = {
                        viewModel.updateTodoCollection(collection.copy(isFavCollection = !collection.isFavCollection))
                    }
                )
            }

            // 2. Ghost Card luôn luôn chốt sổ ở cuối cùng
            if (!onlyFavorites) {
                item {
                    GhostAddCard(onClick = { showAddDialog = true })
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Cửa sổ Pop-up để gõ tên Collection mới
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Danh sách mới") },
                text = {
                    OutlinedTextField(
                        value = newCollectionTitle,
                        onValueChange = { newCollectionTitle = it },
                        label = { Text("Tên danh sách") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newCollectionTitle.isNotBlank()) {
                            viewModel.addTodoCollection(newCollectionTitle)
                            newCollectionTitle = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("Tạo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
