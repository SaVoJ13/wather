package com.weather.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.LocalReducedMotion
import com.weather.app.ui.theme.LocalWeatherPalette
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SunriseSunsetCalculator {
    fun calculateTimes(
        todayForecast: DailyForecastItem?,
        currentTime: LocalDateTime
    ): Pair<LocalDateTime, LocalDateTime> {
        val today = currentTime.toLocalDate()
        val sunrise = todayForecast?.sunrise ?: today.atTime(7, 20)
        val sunset = todayForecast?.sunset ?: today.atTime(16, 15)
        return sunrise to sunset
    }

    fun calculateSunProgress(
        currentTime: LocalDateTime,
        sunrise: LocalDateTime,
        sunset: LocalDateTime
    ): Float {
        if (currentTime.isBefore(sunrise)) return 0f
        if (currentTime.isAfter(sunset)) return 1f
        val totalSecs = Duration.between(sunrise, sunset).seconds
        if (totalSecs <= 0) return 0f
        val elapsedSecs = Duration.between(sunrise, currentTime).seconds
        return (elapsedSecs.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)
    }

    fun formatDuration(sunrise: LocalDateTime, sunset: LocalDateTime): String {
        val duration = Duration.between(sunrise, sunset)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return "$hours год $minutes хв"
    }

    fun calculateStatus(
        currentTime: LocalDateTime,
        sunrise: LocalDateTime,
        sunset: LocalDateTime
    ): String {
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
        return when {
            currentTime.isBefore(sunrise) -> {
                val diff = Duration.between(currentTime, sunrise)
                val h = diff.toHours()
                val m = diff.toMinutes() % 60
                if (h > 0) "Схід через $h год $m хв" else "Схід через $m хв"
            }
            currentTime.isAfter(sunset) -> {
                "Ніч • Схід о ${sunrise.format(timeFmt)}"
            }
            else -> {
                val diff = Duration.between(currentTime, sunset)
                val h = diff.toHours()
                val m = diff.toMinutes() % 60
                if (h > 0) "Захід через $h год $m хв" else "Захід через $m хв"
            }
        }
    }
}

@Composable
fun SunriseSunsetCard(
    todayForecast: DailyForecastItem?,
    currentTime: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val isReducedMotion = LocalReducedMotion.current
    val (sunrise, sunset) = remember(todayForecast, currentTime) {
        SunriseSunsetCalculator.calculateTimes(todayForecast, currentTime)
    }
    val targetProgress = remember(currentTime, sunrise, sunset) {
        SunriseSunsetCalculator.calculateSunProgress(currentTime, sunrise, sunset)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (isReducedMotion) snap() else tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sunProgress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "sunPulse")
    val pulseScale by if (isReducedMotion) {
        remember { mutableFloatStateOf(1f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    }

    val isDay = currentTime.isAfter(sunrise) && currentTime.isBefore(sunset)
    val dayDurationText = remember(sunrise, sunset) {
        SunriseSunsetCalculator.formatDuration(sunrise, sunset)
    }
    val statusText = remember(currentTime, sunrise, sunset) {
        SunriseSunsetCalculator.calculateStatus(currentTime, sunrise, sunset)
    }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surfaceTint)
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Схід і захід сонця",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textColor
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.textColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas з дугою небосхилу та сонцем
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height

            val padding = 32.dp.toPx()
            val startX = padding
            val endX = width - padding
            val horizonY = height - 16.dp.toPx()
            val apexY = 16.dp.toPx()
            val controlX = width / 2f
            val controlY = 2f * apexY - horizonY

            // 1. Горизонт
            drawLine(
                color = palette.textColor.copy(alpha = 0.22f),
                start = Offset(16.dp.toPx(), horizonY),
                end = Offset(width - 16.dp.toPx(), horizonY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )

            // 2. Фонова дуга небосхилу (траєкторія)
            val trajectoryPath = Path().apply {
                moveTo(startX, horizonY)
                quadraticTo(controlX, controlY, endX, horizonY)
            }
            drawPath(
                path = trajectoryPath,
                color = palette.textColor.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )

            // 3. Активна золотиста дуга пройденого шляху
            if (animatedProgress > 0f) {
                val pathMeasure = android.graphics.PathMeasure()
                val androidTrajectory = android.graphics.Path()
                androidTrajectory.moveTo(startX, horizonY)
                androidTrajectory.quadTo(controlX, controlY, endX, horizonY)
                pathMeasure.setPath(androidTrajectory, false)

                val length = pathMeasure.length
                val activeAndroidPath = android.graphics.Path()
                pathMeasure.getSegment(0f, length * animatedProgress, activeAndroidPath, true)

                drawPath(
                    path = activeAndroidPath.asComposePath(),
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.accentColor.copy(alpha = 0.5f),
                            palette.accentColor
                        ),
                        startX = startX,
                        endX = endX
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Позиція сонця за формулою quadratic bezier
            val t = animatedProgress
            val oneMinusT = 1f - t
            val sunX = oneMinusT * oneMinusT * startX + 2f * oneMinusT * t * controlX + t * t * endX
            val sunY = oneMinusT * oneMinusT * horizonY + 2f * oneMinusT * t * controlY + t * t * horizonY

            // Маркер Сходу (початок)
            drawCircle(
                color = palette.textColor.copy(alpha = 0.5f),
                radius = 3.dp.toPx(),
                center = Offset(startX, horizonY)
            )
            // Маркер Заходу (кінець)
            drawCircle(
                color = palette.textColor.copy(alpha = 0.5f),
                radius = 3.dp.toPx(),
                center = Offset(endX, horizonY)
            )

            // Сонце
            if (isDay) {
                // Пульсуюче сяйво
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.accentColor.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(sunX, sunY),
                        radius = 24.dp.toPx() * pulseScale
                    ),
                    radius = 24.dp.toPx() * pulseScale,
                    center = Offset(sunX, sunY)
                )

                // Ореол сонця
                drawCircle(
                    color = palette.accentColor,
                    radius = 8.dp.toPx(),
                    center = Offset(sunX, sunY)
                )

                // Ядро сонця
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            } else {
                // Нічний стан: сонце на горизонті / місяць
                drawCircle(
                    color = palette.textColor.copy(alpha = 0.2f),
                    radius = 10.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
                drawCircle(
                    color = palette.textColor.copy(alpha = 0.6f),
                    radius = 5.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Нижні підписи: Схід, Тривалість дня, Захід
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = palette.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Схід",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textColor.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = sunrise.format(timeFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Тривалість дня",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = dayDurationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Захід",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = palette.textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = sunset.format(timeFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textColor
                )
            }
        }
    }
}
