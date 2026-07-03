package com.example.todolist.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Login : Screen()
    @Serializable
    data object Home : Screen()

    @Serializable
    data object AllCollections : Screen()

    @Serializable
    data object Favorite : Screen()
    // Dùng data class cho các màn hình cần nhận dữ liệu (ở đây là nhận id)
    @Serializable
    data class CollectionDetail(val collectionId: Long) : Screen()

    @Serializable
    data object Calendar: Screen()
}
