package com.example.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.todolist.navigation.Screen
import com.example.todolist.ui.theme.*
import com.example.todolist.ui.viewmodels.AuthViewModel

@Composable
fun FlowDoDrawer(
    currentRoute: Screen = Screen.Home,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    onNavigateToAll: () -> Unit = {},
    onNavigateToFav: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(300.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser == null) {
                    // 1. TRẠNG THÁI KHÁCH: Icon mặc định
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (currentUser?.photoUrl != null) {
                    // 2. ĐĂNG NHẬP GOOGLE: Tải ảnh đại diện
                    AsyncImage(
                        model = currentUser?.photoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 3. ĐĂNG NHẬP EMAIL: Lấy chữ cái đầu tiên
                    val emailStr = currentUser?.email ?: ""
                    val initial = if (emailStr.isNotEmpty()) emailStr.first().uppercase() else "?"

                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = CyanPrimary // Hoặc màu TextPrimary tùy ý bạn
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = currentUser?.email ?: "Guest User", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text(text = if (currentUser == null) "Sync to save data" else "Cloud Sync Enabled", fontSize = 12.sp, color = TextSecondary)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        DrawerMenuItem(icon = Icons.Outlined.Home, text = "Home", isSelected = currentRoute is Screen.Home) { onNavigateToHome() }
        DrawerMenuItem(icon = Icons.Outlined.Task, text = "Task", isSelected = currentRoute is Screen.CollectionDetail) { (onNavigateToInbox()) }
        DrawerMenuItem(icon = Icons.Outlined.MailOutline, text = "All Collections", isSelected = currentRoute is Screen.AllCollections) { (onNavigateToAll())}
        DrawerMenuItem(icon = Icons.Outlined.CalendarMonth, text = "Calendar", isSelected = currentRoute is Screen.Calendar) { onNavigateToCalendar() }
        DrawerMenuItem(icon = Icons.Outlined.FavoriteBorder, text = "Favorites", isSelected = currentRoute is Screen.Favorite, tint = PinkFavorite) {onNavigateToFav()}
        DrawerMenuItem(icon = Icons.Outlined.Delete, text = "Trash Bin", isSelected = false) {  }
        DrawerMenuItem(icon = Icons.Outlined.Settings, text = "Settings", isSelected = false) {  }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        // Footer
        if (currentUser == null) {
            DrawerMenuItem(
                icon = Icons.Default.Sync,
                text = "Sync (Log In)",
                isSelected = false,
                tint = CyanPrimary,
                onClick = { onNavigateToLogin() }
            )
        } else {
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                text = "Log Out",
                isSelected = false,
                onClick = { authViewModel.logout() }
            )
        }

        DrawerMenuItem(icon = Icons.Outlined.Info, text = "About App", isSelected = false) {}
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, text: String, isSelected: Boolean, tint: Color = TextSecondary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DrawerSelected else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = if (isSelected) CyanPrimary else tint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = if (isSelected) CyanPrimary else TextPrimary, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
    }
}