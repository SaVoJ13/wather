package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.ui.theme.GlassPillActiveFill
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.theme.RainAccent
import java.time.format.DateTimeFormatter

/**
 * Скляна картка 24-годинного прогнозу (Sapphire Atmospheric Glass v2 — Макет 01 & 02).
 *
 * Відповідно до еталонних PNG-макетів v2:
 * - Активна перша година («Зараз») виділена сапфіровою капсулою з блакитною рамкою;
 * - Кожна година рендерить багатошарову 3D-ілюстрацію погоди з урахуванням часу доби (день/ніч);
 * - Під іконками проходить золотиста точка температурної лінії, поточна температура у вибраних одиницях та ймовірність опадів.
 */
@Composable
fun HourlyForecastRow(
    hourlyItems: List<HourlyForecastItem>,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val units = LocalWeatherUnits.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val minTemp = hourlyItems.minOfOrNull { it.temperatureCelsius } ?: 0.0
    val maxTemp = hourlyItems.maxOfOrNull { it.temperatureCelsius } ?: 1.0
    val tempSpan = (maxTemp - minTemp).coerceAtLeast(1.0)

    GlassCard(
        cornerRadius = 28.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hourly_forecast_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF93C5FD)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(hourlyItems) { index, item ->
                    val isNow = index == 0
                    val timeLabel = if (isNow) stringResource(R.string.now) else item.time.format(timeFormatter)
                    val isHourDay = item.time.hour in 6..20
                    val normTemp = ((item.temperatureCelsius - minTemp) / tempSpan).coerceIn(0.0, 1.0).toFloat()
                    val prevNorm = if (index > 0) {
                        ((hourlyItems[index - 1].temperatureCelsius - minTemp) / tempSpan).coerceIn(0.0, 1.0).toFloat()
                    } else normTemp
                    val nextNorm = if (index < hourlyItems.lastIndex) {
                        ((hourlyItems[index + 1].temperatureCelsius - minTemp) / tempSpan).coerceIn(0.0, 1.0).toFloat()
                    } else normTemp

                    val pillShape = RoundedCornerShape(20.dp)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(66.dp)
                            .clip(pillShape)
                            .then(
                                if (isNow) {
                                    Modifier
                                        .background(GlassPillActiveFill)
                                        .border(1.dp, Color(0xC038BDF8), pillShape)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isNow) Color.White else Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WeatherIllustration(
                            condition = item.condition,
                            isDay = if (isNow) palette.isDay else isHourDay,
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Золотиста лінія температурного тренду з вузловою точкою
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                        ) {
                            val cy = size.height - 4f - normTemp * (size.height - 8f)
                            val prevY = size.height - 4f - ((normTemp + prevNorm) / 2f) * (size.height - 8f)
                            val nextY = size.height - 4f - ((normTemp + nextNorm) / 2f) * (size.height - 8f)
                            val cx = size.width / 2f

                            if (index > 0) {
                                drawLine(
                                    color = Color(0xFFFACC15).copy(alpha = 0.75f),
                                    start = Offset(0f, prevY),
                                    end = Offset(cx, cy),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            if (index < hourlyItems.lastIndex) {
                                drawLine(
                                    color = Color(0xFFFACC15).copy(alpha = 0.75f),
                                    start = Offset(cx, cy),
                                    end = Offset(size.width, nextY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            drawCircle(
                                color = Color(0xFFFACC15),
                                radius = 3.2.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = units.formatTemperature(item.temperatureCelsius),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val pop = item.precipitationProbabilityPercent
                        if (pop != null && pop > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = stringResource(R.string.precipitation_probability_desc),
                                    tint = RainAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$pop%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RainAccent
                                )
                            }
                        } else {
                            Box(modifier = Modifier.height(15.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "0%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF94A3B8).copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
