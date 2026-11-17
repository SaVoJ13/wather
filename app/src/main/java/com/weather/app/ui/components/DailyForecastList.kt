package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.RainAccent
import com.weather.app.ui.theme.TempBarCold
import com.weather.app.ui.theme.TempBarHot
import com.weather.app.ui.theme.TempBarModerate
import com.weather.app.ui.theme.TempBarWarm
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DailyForecastList(
    dailyItems: List<DailyForecastItem>,
    onDayClick: (DailyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    if (dailyItems.isEmpty()) return

    val globalMin = dailyItems.minOfOrNull { it.minTemperatureCelsius } ?: 0.0
    val globalMax = dailyItems.maxOfOrNull { it.maxTemperatureCelsius } ?: 30.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surfaceTint)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Прогноз на 14 днів",
            style = MaterialTheme.typography.titleMedium,
            color = palette.textColor,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        dailyItems.forEachIndexed { index, item ->
            DailyForecastRow(
                index = index,
                item = item,
                globalMin = globalMin,
                globalMax = globalMax,
                onClick = { onDayClick(item) }
            )

            if (index < dailyItems.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = palette.textColor.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    index: Int,
    item: DailyForecastItem,
    globalMin: Double,
    globalMax: Double,
    onClick: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val ukLocale = Locale("uk", "UA")
    val dayFormatter = DateTimeFormatter.ofPattern("EE, d MMM", ukLocale)

    val dayTitle = when (index) {
        0 -> "Сьогодні"
        1 -> "Завтра"
        else -> item.date.format(dayFormatter).replaceFirstChar { it.uppercase(ukLocale) }
    }

    val minInt = item.minTemperatureCelsius.roundToInt()
    val maxInt = item.maxTemperatureCelsius.roundToInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Назва дня
        Text(
            text = dayTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textColor,
            modifier = Modifier.width(96.dp)
        )

        // Іконка погоди
        WeatherConditionIcon(
            condition = item.condition,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Ймовірність опадів
        Box(
            modifier = Modifier.width(42.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (item.precipitationProbabilityPercent > 10) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Опади",
                        tint = RainAccent,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = RainAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Мінімальна температура
        Text(
            text = (if (minInt > 0) "+" else "") + "°",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textColor.copy(alpha = 0.75f),
            modifier = Modifier.width(34.dp)
        )

        // Графічна смужка діапазону дня (Range Bar)
        TemperatureRangeBar(
            dayMin = item.minTemperatureCelsius,
            dayMax = item.maxTemperatureCelsius,
            globalMin = globalMin,
            globalMax = globalMax,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 6.dp)
        )

        // Максимальна температура
        Text(
            text = (if (maxInt > 0) "+" else "") + "°",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textColor,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
fun TemperatureRangeBar(
    dayMin: Double,
    dayMax: Double,
    globalMin: Double,
    globalMax: Double,
    modifier: Modifier = Modifier
) {
    val totalRange = (globalMax - globalMin).coerceAtLeast(1.0)
    val startFraction = ((dayMin - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()
    val endFraction = ((dayMax - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()

    val startColor = when {
        dayMin < 0 -> TempBarCold
        dayMin < 18 -> TempBarModerate
        else -> TempBarWarm
    }
    val endColor = when {
        dayMax < 5 -> TempBarCold
        dayMax < 22 -> TempBarModerate
        dayMax < 28 -> TempBarWarm
        else -> TempBarHot
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cornerRadius = CornerRadius(height / 2f, height / 2f)

        // Базовий фоновий трек
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(0f, 0f),
            size = Size(width, height),
            cornerRadius = cornerRadius
        )

        // Активний діапазон дня
        val activeStart = width * startFraction
        val activeWidth = (width * (endFraction - startFraction)).coerceAtLeast(height)

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(startColor, endColor),
                startX = activeStart,
                endX = activeStart + activeWidth
            ),
            topLeft = Offset(activeStart, 0f),
            size = Size(activeWidth, height),
            cornerRadius = cornerRadius
        )
    }
}
