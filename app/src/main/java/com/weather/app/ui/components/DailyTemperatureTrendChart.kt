package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.theme.TempTrendMaxCoral
import com.weather.app.ui.theme.TempTrendMinCyan
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Інтерактивний дволінійний графік температурного тренду Макс/Мін (Sapphire Atmospheric Glass v2 — Макет 03).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 2 & 5):
 * - Підтримує повний фактичний горизонт прогнозу до 16 днів (без необґрунтованого обрізання до 14);
 * - Верхня крива Макс (теплий кораловий #FB923C) та нижня крива Мін (блакитний #38BDF8);
 * - Напівпрозора градієнтна заливка коридору між кривими Макс і Мін;
 * - Інтерактивний вибір дня (тап або горизонтальний свайп) з вертикальним візиром та інформаційною капсулою.
 */
@Composable
fun DailyTemperatureTrendChart(
    dailyItems: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    if (dailyItems.isEmpty()) return

    val units = LocalWeatherUnits.current
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val shortDayFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("EE", currentLocale) }
    val fullDateFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("EEE, d MMM", currentLocale) }

    var selectedIndex by remember(dailyItems.size) {
        mutableIntStateOf((2).coerceAtMost(dailyItems.lastIndex))
    }
    val safeIndex = selectedIndex.coerceIn(0, dailyItems.lastIndex)
    val selectedDay = dailyItems[safeIndex]

    val minVal = dailyItems.minOfOrNull { it.minTemperatureCelsius } ?: 0.0
    val maxVal = dailyItems.maxOfOrNull { it.maxTemperatureCelsius } ?: 30.0
    val tempSpan = (maxVal - minVal).coerceAtLeast(4.0)
    val paddedMin = minVal - tempSpan * 0.14
    val paddedMax = maxVal + tempSpan * 0.16
    val paddedSpan = (paddedMax - paddedMin).coerceAtLeast(1.0)

    val selectedDateStr = selectedDay.date.format(fullDateFormatter).replaceFirstChar { it.uppercase(currentLocale) }
    val selectedMaxStr = units.formatTemperature(selectedDay.maxTemperatureCelsius)
    val selectedMinStr = units.formatTemperature(selectedDay.minTemperatureCelsius)
    val popSuffix = selectedDay.precipitationProbabilityPercent?.takeIf { it > 0 }?.let { " • $it%" } ?: ""

    GlassCard(
        cornerRadius = 28.dp,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Графік температурного тренду на ${dailyItems.size} днів. Вибрано $selectedDateStr: максимум $selectedMaxStr, мінімум $selectedMinStr"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Заголовок та легенда Макс / Мін
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ТЕМПЕРАТУРНИЙ ТРЕНД (${dailyItems.size} ДНІВ)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = Color(0xFF93C5FD)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(TempTrendMaxCoral)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Макс ${units.temperatureUnit.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFFED7AA)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(TempTrendMinCyan)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Мін ${units.temperatureUnit.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFBAE6FD)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Інформаційна капсула обраного дня
            GlassPillBadge(
                cornerRadius = 10.dp,
                fillColor = Color(0xEE0F172A),
                borderColor = Color(0xB338BDF8)
            ) {
                Text(
                    text = "$selectedDateStr: $selectedMaxStr / $selectedMinStr$popSuffix",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Полотно дволінійного графіка Макс/Мін
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .pointerInput(dailyItems.size) {
                        detectTapGestures { offset ->
                            if (dailyItems.size > 1 && size.width > 0) {
                                val step = size.width.toFloat() / (dailyItems.size - 1)
                                val idx = (offset.x / step).roundToInt().coerceIn(0, dailyItems.lastIndex)
                                selectedIndex = idx
                            }
                        }
                    }
                    .pointerInput(dailyItems.size) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (dailyItems.size > 1 && size.width > 0) {
                                val step = size.width.toFloat() / (dailyItems.size - 1)
                                val idx = (change.position.x / step).roundToInt().coerceIn(0, dailyItems.lastIndex)
                                selectedIndex = idx
                            }
                        }
                    }
            ) {
                val padX = 12.dp.toPx()
                val padTop = 12.dp.toPx()
                val padBot = 12.dp.toPx()
                val chartW = (size.width - padX * 2f).coerceAtLeast(1f)
                val chartH = (size.height - padTop - padBot).coerceAtLeast(1f)

                // 3 горизонтальні направляючі лінії сітки
                for (g in 0..2) {
                    val gy = padTop + g * (chartH / 2f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.14f),
                        start = Offset(padX, gy),
                        end = Offset(size.width - padX, gy),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val count = dailyItems.size
                val stepX = if (count > 1) chartW / (count - 1).toFloat() else 0f

                val maxPoints = ArrayList<Offset>(count)
                val minPoints = ArrayList<Offset>(count)

                for (i in 0 until count) {
                    val item = dailyItems[i]
                    val px = padX + i * stepX
                    val normMax = ((item.maxTemperatureCelsius - paddedMin) / paddedSpan).coerceIn(0.0, 1.0).toFloat()
                    val normMin = ((item.minTemperatureCelsius - paddedMin) / paddedSpan).coerceIn(0.0, 1.0).toFloat()
                    val pyMax = padTop + chartH * (1f - normMax)
                    val pyMin = padTop + chartH * (1f - normMin)
                    maxPoints.add(Offset(px, pyMax))
                    minPoints.add(Offset(px, pyMin))
                }

                // 1. Заливка коридору між кривою Макс і кривою Мін
                if (count > 1) {
                    val bandPath = Path().apply {
                        moveTo(maxPoints.first().x, maxPoints.first().y)
                        for (i in 1 until count) {
                            lineTo(maxPoints[i].x, maxPoints[i].y)
                        }
                        for (i in count - 1 downTo 0) {
                            lineTo(minPoints[i].x, minPoints[i].y)
                        }
                        close()
                    }
                    drawPath(
                        path = bandPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TempTrendMaxCoral.copy(alpha = 0.24f),
                                TempTrendMinCyan.copy(alpha = 0.16f)
                            ),
                            startY = padTop,
                            endY = padTop + chartH
                        )
                    )
                }

                // 2. Вертикальна лінія-візир обраного дня
                val selX = maxPoints[safeIndex].x
                drawLine(
                    color = Color.White.copy(alpha = 0.62f),
                    start = Offset(selX, 0f),
                    end = Offset(selX, size.height),
                    strokeWidth = 1.8.dp.toPx()
                )

                // 3. Лінії тренду Макс та Мін
                if (count > 1) {
                    val maxPath = Path().apply {
                        moveTo(maxPoints.first().x, maxPoints.first().y)
                        for (i in 1 until count) {
                            lineTo(maxPoints[i].x, maxPoints[i].y)
                        }
                    }
                    val minPath = Path().apply {
                        moveTo(minPoints.first().x, minPoints.first().y)
                        for (i in 1 until count) {
                            lineTo(minPoints[i].x, minPoints[i].y)
                        }
                    }

                    drawPath(
                        path = maxPath,
                        color = TempTrendMaxCoral,
                        style = Stroke(
                            width = 3.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    drawPath(
                        path = minPath,
                        color = TempTrendMinCyan,
                        style = Stroke(
                            width = 3.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // 4. Вузлові точки Макс та Мін
                for (i in 0 until count) {
                    val isSel = i == safeIndex
                    val rOuter = if (isSel) 5.8.dp.toPx() else 3.6.dp.toPx()
                    val rInner = if (isSel) 4.0.dp.toPx() else 2.2.dp.toPx()

                    drawCircle(
                        color = Color.White,
                        radius = rOuter,
                        center = maxPoints[i]
                    )
                    drawCircle(
                        color = TempTrendMaxCoral,
                        radius = rInner,
                        center = maxPoints[i]
                    )

                    drawCircle(
                        color = Color.White,
                        radius = rOuter,
                        center = minPoints[i]
                    )
                    drawCircle(
                        color = TempTrendMinCyan,
                        radius = rInner,
                        center = minPoints[i]
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Підписи днів тижня вздовж осі X
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyItems.forEachIndexed { index, item ->
                    val isSel = index == safeIndex
                    val shortDay = item.date.format(shortDayFormatter)
                        .take(2)
                        .replaceFirstChar { it.uppercase(currentLocale) }
                    Text(
                        text = shortDay,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (dailyItems.size > 14) 10.sp else 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSel) Color.White else Color(0xFFBAD6F8).copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
