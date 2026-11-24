package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.weather.app.R
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.GlassPillActiveFill
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.theme.RainAccent
import com.weather.app.ui.theme.TempBarTrack
import com.weather.app.ui.theme.TempTrendMaxCoral
import com.weather.app.ui.theme.TempTrendMinCyan
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Об'ємний список щоденного прогнозу на 14–16 днів (Sapphire Atmospheric Glass v2 — Макет 03).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 2, 3, 5, 7, 9):
 * - Підтримує всі фактичні дні прогнозу до 16 днів (без штучного обрізання);
 * - Перший рядок («Сьогодні») виділений сапфіровою капсулою з блакитною рамкою;
 * - Кожен рядок показує назву дня, реальну дату, багатошарову 3D-ілюстрацію погоди, опис стану,
 *   пігулку ймовірності опадів та двоколірну шкалу температурного діапазону (Cyan -> Coral).
 */
@Composable
fun DailyForecastList(
    dailyItems: List<DailyForecastItem>,
    onDayClick: (DailyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dailyItems.isEmpty()) return

    val globalMin = dailyItems.minOfOrNull { it.minTemperatureCelsius } ?: 0.0
    val globalMax = dailyItems.maxOfOrNull { it.maxTemperatureCelsius } ?: 30.0

    GlassCard(
        cornerRadius = 28.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "ЩОДЕННИЙ ПРОГНОЗ (${dailyItems.size} ДНІВ • ТАП ДЛЯ ДЕТАЛЕЙ)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = Color(0xFF93C5FD),
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            dailyItems.forEachIndexed { index, item ->
                DailyForecastRow(
                    index = index,
                    item = item,
                    globalMin = globalMin,
                    globalMax = globalMax,
                    onClick = { onDayClick(item) }
                )

                if (index < dailyItems.lastIndex && index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        thickness = 0.5.dp
                    )
                }
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
    val units = LocalWeatherUnits.current
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE", currentLocale)
    val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", currentLocale)

    val dayTitle = when (index) {
        0 -> stringResource(R.string.today)
        1 -> stringResource(R.string.tomorrow)
        else -> item.date.format(dayNameFormatter).replaceFirstChar { it.uppercase(currentLocale) }
    }
    val dateSubtitle = item.date.format(shortDateFormatter)
    val isToday = index == 0
    val rowShape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(rowShape)
            .then(
                if (isToday) {
                    Modifier
                        .background(GlassPillActiveFill)
                        .border(1.dp, Color(0x9938BDF8), rowShape)
                } else {
                    Modifier
                }
            )
            .sizeIn(minHeight = 68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. День тижня + реальна дата
        Column(modifier = Modifier.width(102.dp)) {
            Text(
                text = dayTitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateSubtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFBAD6F8),
                maxLines = 1
            )
        }

        // 2. Багатошарова 3D-ілюстрація погоди
        WeatherIllustration(
            condition = item.condition,
            isDay = true,
            modifier = Modifier.size(38.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 3. Опис стану погоди + темно-сапфірова пігулка опадів
        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(item.condition.stringRes),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFFF1F5F9),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val pop = item.precipitationProbabilityPercent
            if (pop != null && pop > 10) {
                Spacer(modifier = Modifier.height(3.dp))
                GlassPillBadge(
                    cornerRadius = 8.dp,
                    fillColor = Color(0xE00E224C),
                    borderColor = Color(0x7838BDF8)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = stringResource(R.string.precipitation),
                            tint = RainAccent,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$pop%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF7DD3FC)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 4. Мін температура
        Text(
            text = units.formatTemperature(item.minTemperatureCelsius),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFFBAD6F8),
            modifier = Modifier.width(38.dp)
        )

        // 5. Двоколірна шкала температурного діапазону (Cyan -> Coral)
        TemperatureRangeBar(
            min = item.minTemperatureCelsius,
            max = item.maxTemperatureCelsius,
            globalMin = globalMin,
            globalMax = globalMax,
            modifier = Modifier
                .width(56.dp)
                .height(7.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 6. Макс температура
        Text(
            text = units.formatTemperature(item.maxTemperatureCelsius),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.width(38.dp)
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
        colors = listOf(TempTrendMinCyan, TempTrendMaxCoral)
    )

    Canvas(modifier = modifier) {
        val barHeight = size.height
        val totalWidth = size.width

        drawRoundRect(
            color = TempBarTrack,
            size = Size(totalWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
        )

        val left = totalWidth * startFraction
        val width = (totalWidth * (endFraction - startFraction)).coerceAtLeast(barHeight * 1.4f)

        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(left.coerceAtMost(totalWidth - width), 0f),
            size = Size(width, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
        )
    }
}
