package com.example.todolist.widget
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.action.actionStartActivity
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
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
import com.example.todolist.MainActivity
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
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTodoRepo(): TodoRepo
}

class TodoWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val repository = entryPoint.getTodoRepo()

        val allTodos = repository.getTodo().first()
        val todayTasks = allTodos.filter { !it.isCompleted && isToday(it.dueDate) }
        val pendingTasks = todayTasks.take(5)
        val totalTasksLeft = todayTasks.size

        provideContent {
            // Bao bọc toàn bộ nội dung trong Column và gắn sự kiện click
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(CardBackground)
                    .padding(20.dp)
                    .cornerRadius(20.dp)
                    .clickable(actionStartActivity<MainActivity>())
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
    if (timestamp == null) return false

    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
}