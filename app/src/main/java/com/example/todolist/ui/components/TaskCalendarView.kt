package com.example.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.ui.theme.CyanPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun TaskCalendarView(
    allTodos: List<TodoEntity>,
    onDateSelected: (LocalDate) -> Unit
) {
    // Biến lưu trữ tháng hiện tại đang xem
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val taskCountsByDate = remember(allTodos) {
        allTodos.filter { it.dueDate != null && !it.isCompleted }
            .groupingBy {
                Instant.ofEpochMilli(it.dueDate!!)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
            }
            .eachCount()
    }

    // TỐI ƯU HÓA: Tự động tính toán lại danh sách ô mỗi khi người dùng đổi tháng
    val days = remember(currentMonth) {
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value

        val list = mutableListOf<LocalDate?>()
        for (i in 1 until firstDayOfWeek) list.add(null) // Ô trống
        for (i in 1..daysInMonth) list.add(currentMonth.atDay(i))
        list
    }

    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // 1. THANH ĐIỀU HƯỚNG THÁNG (CÓ NÚT BẤM)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Đẩy 2 nút ra 2 góc
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
            }

            Text(
                text = "Tháng ${currentMonth.monthValue} / ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = if (day == "CN") Color.Red.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(320.dp)
        ) {
            items(days) { date ->
                if (date != null) {
                    val taskCount = taskCountsByDate[date] ?: 0
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (taskCount > 0) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) CyanPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedDate = date
                                onDateSelected(date)
                            }
                    ) {
                        // Ngày hiển thị TRUNG TÂM ô vuông
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday && !isSelected) CyanPrimary else Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        // ĐÃ SỬA: Ép sát góc và tinh chỉnh kích thước
                        if (taskCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    // Giảm padding để nó bám sát viền phải và viền trên
                                    .padding(top = 2.dp, end = 2.dp)
                                    // Thu nhỏ vòng tròn xuống 14dp để chừa không gian cho ngày
                                    .size(12.dp)
                                    .background(Color.Red.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (taskCount > 9) "9+" else "$taskCount",
                                    // Thu nhỏ font chữ để nằm lọt lòng trong vòng tròn 14dp
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}