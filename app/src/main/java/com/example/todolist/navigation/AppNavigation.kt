package com.example.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute // Thêm import này
import com.example.todolist.ui.screens.AllCollectionsScreen
import com.example.todolist.ui.screens.CollectionDetailScreen
import com.example.todolist.ui.screens.HomeScreen
import com.example.todolist.ui.viewmodels.TodoViewModel
import com.example.todolist.navigation.Screen
import com.example.todolist.ui.screens.CalendarScreen
import com.example.todolist.ui.screens.LoginScreen
import com.example.todolist.ui.viewmodels.AuthViewModel

@Composable
fun AppNavigation(viewModel: TodoViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val startDestination = if(authViewModel.isUserAuthenticated()) Screen.Home else Screen.Login

    // Bắt đầu bằng Object Home
    NavHost(navController = navController, startDestination = Screen.Home) {

        composable<Screen.Login> {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        // 1. MÀN HÌNH HOME
        composable<Screen.Home> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAllCollections = {
                    navController.navigate(Screen.AllCollections)
                },
                onNavigateToCollectionDetail = { id ->
                    // Truyền ID vào thẳng Data Class
                    navController.navigate(Screen.CollectionDetail(collectionId = id))
                },
                onNavigateToFavorite = {
                    navController.navigate(Screen.Favorite)
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login)
                }
            )
        }

        // 2. MÀN HÌNH ALL COLLECTIONS
        composable<Screen.AllCollections> {
            AllCollectionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCollectionDetail = { id ->
                    navController.navigate(Screen.CollectionDetail(collectionId = id))
                }
            )
        }

        // 3. MÀN HÌNH FAVORITE
        composable<Screen.Favorite> {
            AllCollectionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCollectionDetail = { id ->
                    navController.navigate(Screen.CollectionDetail(collectionId = id))
                },
                onlyFavorites = true
            )
        }

        // 3. MÀN HÌNH CHI TIẾT
        composable<Screen.CollectionDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CollectionDetail>()

            CollectionDetailScreen(
                viewModel = viewModel,
                collectionId = args.collectionId, // Trích xuất collectionId có sẵn đúng kiểu Long
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Calendar> {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateToAllCollections = { navController.navigate(Screen.AllCollections) },
                onNavigateToCollectionDetail = { id -> navController.navigate(Screen.CollectionDetail(collectionId = id)) }
            )
        }
    }
}