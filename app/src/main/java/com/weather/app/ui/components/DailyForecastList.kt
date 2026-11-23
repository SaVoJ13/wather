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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import androidx.compose.ui.unit.dp
import com.weather.app.R
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
            text = stringResource(R.string.daily_forecast_title),
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
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val dayFormatter = DateTimeFormatter.ofPattern("EE, d MMM", currentLocale)

    val dayTitle = when (index) {
        0 -> stringResource(R.string.today)
        1 -> stringResource(R.string.tomorrow)
        else -> item.date.format(dayFormatter).replaceFirstChar { it.uppercase(currentLocale) }
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
            if (item.precipitationProbabilityPercent != null && item.precipitationProbabilityPercent > 10) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = stringResource(R.string.precipitation),
                        tint = RainAccent,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.precipitationProbabilityPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = RainAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Мін температура
        val minPrefix = if (minInt > 0) "+" else ""
        Text(
            text = "$minPrefix$minInt°",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textColor.copy(alpha = 0.65f),
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Градієнтна температурна смуга (горизонтальний bar)
        TemperatureRangeBar(
            min = item.minTemperatureCelsius,
            max = item.maxTemperatureCelsius,
            globalMin = globalMin,
            globalMax = globalMax,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Макс температура
        val maxPrefix = if (maxInt > 0) "+" else ""
        Text(
            text = "$maxPrefix$maxInt°",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textColor,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun TemperatureRangeBar(
    min: Double,
    max: Double,
    globalMin: Double,
    globalMax: Double,
    modifier: Modifier = Modifier
) {
    val totalRange = (globalMax - globalMin).coerceAtLeast(1.0)
    val startFraction = ((min - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()
    val endFraction = ((max - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()

    val barBrush = Brush.horizontalGradient(
        colors = listOf(
            tempToColor(min),
            tempToColor(max)
        )
    )

    Canvas(modifier = modifier) {
        val barHeight = size.height
        val totalWidth = size.width

        // Сірий фоновий трек
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            size = Size(totalWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
        )

        // Активна ділянка діапазону дня
        val left = totalWidth * startFraction
        val width = (totalWidth * (endFraction - startFraction)).coerceAtLeast(barHeight)

        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(left, 0f),
            size = Size(width, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
        )
    }
}

private fun tempToColor(temp: Double): Color {
    return when {
        temp <= 0 -> TempBarCold
        temp in 0.1..15.0 -> TempBarModerate
        temp in 15.1..25.0 -> TempBarWarm
        else -> TempBarHot
    }
}
