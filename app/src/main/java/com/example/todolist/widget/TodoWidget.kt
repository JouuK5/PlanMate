package com.example.todolist.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.todolist.R
import com.example.todolist.data.repository.TodoRepo
import com.example.todolist.ui.theme.CardBackground
import com.example.todolist.ui.theme.CyanAccent
import com.example.todolist.ui.theme.DividerColor
import com.example.todolist.ui.theme.NavyText
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTodoRepo(): TodoRepo
}

class TodoWidget : GlanceAppWidget() {

    // Đây là nơi cung cấp dữ liệu cho Widget
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val repository = entryPoint.getTodoRepo()

        // 3. Lấy dữ liệu thật từ Database (Dùng first() để lấy ảnh chụp dữ liệu mới nhất)
        val allTodos = repository.getTodo().first()
        val todayTasks = allTodos.filter { !it.isCompleted && isToday(it.dueDate) }
        // Lọc ra các công việc chưa hoàn thành, lấy tối đa 5 cái để hiện trên Widget
        val pendingTasks = todayTasks.take(5)
        val totalTasksLeft = todayTasks.size

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(CardBackground)
                    .padding(20.dp)
                    .cornerRadius(20.dp) // Border radius: 20dp
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Tasks",
                        style = TextStyle(
                            color = widgetColor(NavyText),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                }

                // Danh sách công việc
                LazyColumn(modifier = GlanceModifier.defaultWeight()) {
                    if (pendingTasks.isEmpty()) {
                        item {
                            Text(
                                text = "Hôm nay không có gì làm hết!",
                                style = TextStyle(color = widgetColor(Color.Gray), fontSize = 14.sp),
                                modifier = GlanceModifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(pendingTasks) { task ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_circle_widget),
                                    contentDescription = "Uncheck",
                                    modifier = GlanceModifier.size(24.dp)
                                )

                                Text(
                                    text = task.title,
                                    style = TextStyle(color = widgetColor(NavyText), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                    modifier = GlanceModifier.defaultWeight().padding(start = 12.dp)
                                )

                                // Nếu task này có đặt giờ (dueDate != null), hiện icon trái tim/cảnh báo
                                if (task.dueDate != null) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_heart_widget),
                                        contentDescription = "Has Due Date",
                                        modifier = GlanceModifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer: Dòng kẻ mờ và số lượng task
                Spacer(modifier = GlanceModifier.height(8.dp))
                Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(DividerColor)) {}
                Spacer(modifier = GlanceModifier.height(12.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$totalTasksLeft tasks left",
                        style = TextStyle(color = widgetColor(Color(0xFF9CA3AF)), fontSize = 13.sp)
                    )
                }
            }
        }
    }
}

// Data class ảo phục vụ vẽ UI
data class WidgetTask(val id: Long, val title: String, val isFavorite: Boolean)

// Receiver để hệ thống Android nhận diện Widget này
class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}

fun widgetColor(color: Color): ColorProvider {
    return ColorProvider(
        day = color,
        night = color
    )
}

fun isToday(timestamp: Long?): Boolean{
    if (timestamp == null) return false // Nếu task không có ngày hạn, bỏ qua

    val today = java.util.Calendar.getInstance()
    val taskDate = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

    return today.get(java.util.Calendar.YEAR) == taskDate.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == taskDate.get(java.util.Calendar.DAY_OF_YEAR)
}