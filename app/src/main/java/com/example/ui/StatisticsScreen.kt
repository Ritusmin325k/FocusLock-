package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground
import com.example.viewmodel.FocusViewModel
import com.example.viewmodel.StatisticsSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(
    viewModel: FocusViewModel,
    stats: StatisticsSummary?,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor
    val secondaryText = themeColors.secondaryTextColor

    LiquidGlassBackground(
        isDark = isDark,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        item {
            Text(
                text = "Detailed Statistics",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "Track your focus performance over weeks and months.",
                style = MaterialTheme.typography.bodyMedium.copy(color = secondaryText)
            )
        }

        // Calendar Contribution Heatmap
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar Heatmap", tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "35-Day Focus Heatmap",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                        )
                    }

                    // Render heatmap cells
                    FocusCalendarHeatmap(heatmap = stats?.calendarHeatmap ?: emptyMap(), isDark = isDark)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Legends
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Less", fontSize = 11.sp, color = secondaryText)
                        Spacer(modifier = Modifier.width(4.dp))
                        HeatmapLegendSquare(Color(0xFF334155), isDark)
                        Spacer(modifier = Modifier.width(2.dp))
                        HeatmapLegendSquare(Color(0xFF0F533E), isDark)
                        Spacer(modifier = Modifier.width(2.dp))
                        HeatmapLegendSquare(Color(0xFF047857), isDark)
                        Spacer(modifier = Modifier.width(2.dp))
                        HeatmapLegendSquare(Color(0xFF059669), isDark)
                        Spacer(modifier = Modifier.width(2.dp))
                        HeatmapLegendSquare(Color(0xFF10B981), isDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "More", fontSize = 11.sp, color = secondaryText)
                    }
                }
            }
        }

        // Weekly Performance Bar Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.InsertChart, contentDescription = "Bar Chart", tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weekly Performance (Minutes)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                        )
                    }

                    WeeklyBarChart(heatmap = stats?.calendarHeatmap ?: emptyMap(), textColor = textColor, secondaryText = secondaryText, accentColor = accentColor, isDark = isDark)
                }
            }
        }

        // Expanded Stats grid cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Performance Records",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        icon = Icons.Default.QueryBuilder,
                        title = "Avg. Session",
                        value = "${stats?.averageSessionMinutes ?: 0} min",
                        isDark = isDark,
                        textColor = textColor,
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.Schedule,
                        title = "Longest Focus",
                        value = "${stats?.longestSessionMinutes ?: 0} min",
                        isDark = isDark,
                        textColor = textColor,
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "Current Streak",
                        value = "${stats?.currentStreak ?: 0} days",
                        isDark = isDark,
                        textColor = Color(0xFFF97316),
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.Event,
                        title = "Best Streak",
                        value = "${stats?.bestStreak ?: 0} days",
                        isDark = isDark,
                        textColor = Color(0xFFF59E0B),
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Historic List of sessions
        item {
            Text(
                text = "Historic Sessions (${stats?.recentSessions?.size ?: 0})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val list = stats?.recentSessions ?: emptyList()
        if (list.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No recorded logs available yet.", color = secondaryText, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(list) { session ->
                SessionHistoryItem(session = session, isDark = isDark, textColor = textColor, cardBg = cardBg)
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    }
}

@Composable
fun FocusCalendarHeatmap(
    heatmap: Map<String, Int>,
    isDark: Boolean
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getDefault()

    // Generate last 35 days (5 weeks of 7 days)
    val totalCells = 35
    val daysList = mutableListOf<String>()
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.add(Calendar.DAY_OF_YEAR, -totalCells + 1)

    for (i in 0 until totalCells) {
        daysList.add(sdf.format(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Wrap into a grid of 5 columns (weeks), each containing 7 rows (days of week)
    // Wait, the standard Github representation is: columns are weeks (left to right), rows are days of week (Sunday to Saturday).
    // Let's render a clean, wrapping Row/Column layout!
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 5 columns (weeks)
        for (w in 0 until 5) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 7 days of the week in each column
                for (d in 0 until 7) {
                    val index = w * 7 + d
                    val dateStr = daysList.getOrNull(index) ?: ""
                    val minutes = heatmap[dateStr] ?: 0

                    val cellColor = when {
                        minutes == 0 -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        minutes <= 15 -> Color(0xFF0F533E)
                        minutes <= 30 -> Color(0xFF047857)
                        minutes <= 60 -> Color(0xFF059669)
                        else -> Color(0xFF10B981) // Brightest emerald for heavy focus days!
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(cellColor)
                    )
                }
            }
        }
    }
}

@Composable
fun HeatmapLegendSquare(color: Color, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
fun WeeklyBarChart(
    heatmap: Map<String, Int>,
    textColor: Color,
    secondaryText: Color,
    accentColor: Color,
    isDark: Boolean
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    sdf.timeZone = TimeZone.getDefault()
    dayFormat.timeZone = TimeZone.getDefault()

    // Get last 7 days from today backwards
    val days = mutableListOf<Pair<String, String>>() // DateKey, Display Label
    val cal = Calendar.getInstance(TimeZone.getDefault())
    
    // Fill backwards
    for (i in 0 until 7) {
        val dateKey = sdf.format(cal.time)
        val label = if (i == 0) "Today" else dayFormat.format(cal.time)
        days.add(Pair(dateKey, label))
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    
    // Reverse to show chronologically (oldest to newest/today)
    val weekDays = days.reversed()

    val minutesList = weekDays.map { heatmap[it.first] ?: 0 }
    val maxMinutes = (minutesList.maxOrNull() ?: 60).coerceAtLeast(60).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        weekDays.forEachIndexed { idx, pair ->
            val mins = minutesList[idx]
            // Calculate proportional height
            val ratio = if (maxMinutes > 0) mins / maxMinutes else 0f
            val barHeightDp = (ratio * 120f).coerceAtLeast(8f).dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Value text
                Text(
                    text = "${mins}m",
                    fontSize = 11.sp,
                    color = if (mins > 0) accentColor else secondaryText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // The bar
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(barHeightDp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (mins > 0) {
                                Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f)))
                            } else {
                                val c = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                Brush.verticalGradient(listOf(c, c))
                            }
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Day Label
                Text(
                    text = pair.second,
                    fontSize = 11.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    val themeColors = GlassThemeColors(isDark)
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = themeColors.accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = textColor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }
    }
}
