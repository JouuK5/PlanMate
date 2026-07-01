package com.example.todolist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    background = BackgroundWhite,
    surface = GrayLight,
    tertiary = AccentPink,
    onPrimary = BackgroundWhite,
    onBackground = TextDark
)

@Composable
fun TodoListTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}