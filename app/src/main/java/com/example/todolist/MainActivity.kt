package com.example.todolist
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.navigation.AppNavigation
import com.example.todolist.ui.screens.AllCollectionsScreen
import com.example.todolist.ui.screens.CollectionDetailScreen
import com.example.todolist.ui.screens.HomeScreen
import com.example.todolist.ui.theme.LightBlueBackground
import com.example.todolist.ui.theme.TodoListTheme
import com.example.todolist.ui.viewmodels.AuthViewModel
import com.example.todolist.ui.viewmodels.TodoViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TodoViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()

            TodoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel, authViewModel = authViewModel)
                }
            }
        }
    }
}

