package com.weather.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.weather.app.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.weather.app.ui.theme.LocalReducedMotion
import com.weather.app.ui.theme.LocalWeatherPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

private data class RadarEchoBlob(
    val relX: Float,
    val relY: Float,
    val radiusRatio: Float,
    val intensity: Int // 1: Мряка, 2: Дощ, 3: Злива, 4: Гроза
)

@Composable
fun WeatherRadarCard(
    cityName: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val isReducedMotion = LocalReducedMotion.current

    var selectedStepIndex by remember { mutableIntStateOf(1) } // 0: -30 хв, 1: Зараз, 2: +30 хв, 3: +60 хв
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                delay(1500)
                selectedStepIndex = (selectedStepIndex + 1) % 4
            }
        }
    }

    val animatedDrift by animateFloatAsState(
        targetValue = (selectedStepIndex - 1) * 0.16f,
        animationSpec = if (isReducedMotion) androidx.compose.animation.core.snap() else tween(600, easing = FastOutSlowInEasing),
        label = "radarDrift"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "radarTransition")

    val sweepAngle by if (isReducedMotion) {
        remember { mutableFloatStateOf(45f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweepAngle"
        )
    }

    val pulseRipple by if (isReducedMotion) {
        remember { mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseRipple"
        )
    }

    // Симульовані радарні ехо-зони (хмари та опади)
    val echoBlobs = remember {
        listOf(
            RadarEchoBlob(-0.35f, -0.25f, 0.40f, 3), // Основний фронт на північному заході
            RadarEchoBlob(-0.15f, -0.10f, 0.30f, 4), // Епіцентр з грозою ближче до центру
            RadarEchoBlob(0.30f, 0.20f, 0.35f, 2),  // Дощова зона на південному сході
            RadarEchoBlob(0.10f, 0.40f, 0.25f, 1),  // Мряка на півдні
            RadarEchoBlob(-0.45f, 0.25f, 0.22f, 2)  // Невелика хмара на південному заході
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surfaceTint)
            .padding(16.dp)
    ) {
        // Шапка: Заголовок + Live індикатор
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.radar_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textColor
                    )
                    Text(
                        text = stringResource(R.string.radar_subtitle, cityName),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Індикатор LIVE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00E676).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.radar_live),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00E676)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Радарний Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(230.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radarRadius = size.width / 2f - 6.dp.toPx()

                val radarClipPath = Path().apply {
                    addOval(
                        Rect(
                            center = center,
                            radius = radarRadius
                        )
                    )
                }

                clipPath(radarClipPath) {
                    // 1. Темний радарний екран із глибоким радіальним фоном
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF142436), Color(0xFF0A111C)),
                            center = center,
                            radius = radarRadius
                        ),
                        radius = radarRadius,
                        center = center
                    )

                    // 2. Кластери хмар та опадів (зміщуються за часом)
                    echoBlobs.forEach { blob ->
                        val blobCenterX = center.x + (blob.relX + animatedDrift) * radarRadius
                        val blobCenterY = center.y + (blob.relY - animatedDrift * 0.45f) * radarRadius
                        val blobCenter = Offset(blobCenterX, blobCenterY)
                        val blobR = blob.radiusRatio * radarRadius

                        // Рівень 1: Мряка (зелений)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x804CAF50), Color(0x304CAF50), Color.Transparent),
                                center = blobCenter,
                                radius = blobR
                            ),
                            radius = blobR,
                            center = blobCenter
                        )

                        // Рівень 2: Дощ (жовтий)
                        if (blob.intensity >= 2) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xA0FFEB3B), Color(0x40FFEB3B), Color.Transparent),
                                    center = blobCenter,
                                    radius = blobR * 0.65f
                                ),
                                radius = blobR * 0.65f,
                                center = blobCenter
                            )
                        }

                        // Рівень 3: Злива (помаранчевий)
                        if (blob.intensity >= 3) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xC0FF9800), Color(0x50FF9800), Color.Transparent),
                                    center = blobCenter,
                                    radius = blobR * 0.42f
                                ),
                                radius = blobR * 0.42f,
                                center = blobCenter
                            )
                        }

                        // Рівень 4: Гроза (червоний)
                        if (blob.intensity >= 4) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xE0F44336), Color(0x60F44336), Color.Transparent),
                                    center = blobCenter,
                                    radius = blobR * 0.25f
                                ),
                                radius = blobR * 0.25f,
                                center = blobCenter
                            )
                        }
                    }

                    // 3. Сканувальний шлейф променя (Gradient Sweep Trail)
                    val trailAngle = 50f
                    val steps = 8
                    for (i in 1..steps) {
                        val deg = sweepAngle - (i * (trailAngle / steps))
                        val alpha = (0.24f * (1f - (i.toFloat() / (steps + 1)))).coerceAtLeast(0f)
                        drawArc(
                            color = Color(0xFF00E676).copy(alpha = alpha),
                            startAngle = deg,
                            sweepAngle = trailAngle / steps,
                            useCenter = true,
                            size = Size(2 * radarRadius, 2 * radarRadius),
                            topLeft = Offset(center.x - radarRadius, center.y - radarRadius)
                        )
                    }

                    // Сканувальна лінія променя
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())
                    val sweepEnd = Offset(
                        x = center.x + radarRadius * cos(sweepRad).toFloat(),
                        y = center.y + radarRadius * sin(sweepRad).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF69F0AE),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 4. Сітка дальності та перехрестя (15 км, 30 км, 50 км)
                    val ringColor = Color(0x3569F0AE)
                    val ringStroke = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    drawCircle(color = ringColor, radius = radarRadius * 0.30f, center = center, style = ringStroke)
                    drawCircle(color = ringColor, radius = radarRadius * 0.60f, center = center, style = ringStroke)
                    drawCircle(color = ringColor, radius = radarRadius * 0.98f, center = center, style = Stroke(width = 1.5.dp.toPx()))

                    // Перехрестя (осі)
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x - radarRadius, center.y),
                        end = Offset(center.x + radarRadius, center.y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
                    )
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x, center.y - radarRadius),
                        end = Offset(center.x, center.y + radarRadius),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
                    )

                    // 5. Текстові підписи сторін світу та кілометражу
                    drawContext.canvas.nativeCanvas.apply {
                        val compassPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(190, 200, 240, 215)
                            textSize = 11.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val distPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(150, 160, 210, 180)
                            textSize = 8.5f.dp.toPx()
                            textAlign = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }

                        drawText("Пн", center.x, center.y - radarRadius + 14.dp.toPx(), compassPaint)
                        drawText("Пд", center.x, center.y + radarRadius - 6.dp.toPx(), compassPaint)
                        drawText("Зх", center.x - radarRadius + 12.dp.toPx(), center.y + 4.dp.toPx(), compassPaint)
                        drawText("Сх", center.x + radarRadius - 12.dp.toPx(), center.y + 4.dp.toPx(), compassPaint)

                        drawText("15 км", center.x + 4.dp.toPx(), center.y - radarRadius * 0.30f - 2.dp.toPx(), distPaint)
                        drawText("30 км", center.x + 4.dp.toPx(), center.y - radarRadius * 0.60f - 2.dp.toPx(), distPaint)
                        drawText("50 км", center.x + 4.dp.toPx(), center.y - radarRadius * 0.98f + 11.dp.toPx(), distPaint)
                    }

                    // 6. Центральна мітка користувача з пульсуючим колом
                    if (pulseRipple > 0f) {
                        drawCircle(
                            color = Color(0xFF29B6F6).copy(alpha = 0.5f * (1f - pulseRipple)),
                            radius = 22.dp.toPx() * pulseRipple,
                            center = center
                        )
                    }
                    drawCircle(color = Color(0xFF0288D1), radius = 5.dp.toPx(), center = center)
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = center)
                }

                // Зовнішнє кільце облямівки радара
                drawCircle(
                    color = Color(0x5569F0AE),
                    radius = radarRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Інтерактивна панель керування: Play/Pause та кроки часу
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) palette.accentColor else palette.textColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Призупинити радар" else "Відтворити радар",
                    tint = if (isPlaying) Color.Black else palette.textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            val minShort = stringResource(R.string.minutes_short)
            val steps = listOf("-30 $minShort", stringResource(R.string.now), "+30 $minShort", "+60 $minShort")
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                steps.forEachIndexed { index, title ->
                    val isSelected = selectedStepIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) palette.accentColor.copy(alpha = 0.9f)
                                else palette.textColor.copy(alpha = 0.1f)
                            )
                            .clickable {
                                selectedStepIndex = index
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.Black else palette.textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Легенда інтенсивності опадів
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadarLegendItem(color = Color(0xFF4CAF50), label = stringResource(R.string.intensity_drizzle), textColor = palette.textColor)
            RadarLegendItem(color = Color(0xFFFFEB3B), label = stringResource(R.string.intensity_rain), textColor = palette.textColor)
            RadarLegendItem(color = Color(0xFFFF9800), label = stringResource(R.string.intensity_heavy_rain), textColor = palette.textColor)
            RadarLegendItem(color = Color(0xFFF44336), label = stringResource(R.string.intensity_thunderstorm), textColor = palette.textColor)
        }
    }
}

@Composable
private fun RadarLegendItem(
    color: Color,
    label: String,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.75f)
        )
    }
}
