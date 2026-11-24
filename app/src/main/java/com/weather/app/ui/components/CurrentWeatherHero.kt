package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.theme.StaleBadgeBackground
import com.weather.app.ui.theme.StaleBadgeText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Головний скульптурний блок поточної погоди (Sapphire Atmospheric Glass v2 — Макет 01 & 02).
 *
 * Відповідно до еталонних PNG-макетів v2:
 * - Відкрита сапфірова скляна композиція (радіус 32dp) із радіальним свіченням позаду великої 3D-ілюстрації;
 * - Верхня ліва темно-сапфірова пігулка стану погоди з кольоровим маркером;
 * - Велика скульптурна температура (86sp Bold) з оптично вирівняним знаком градуса;
 * - Рядок «Відчувається як ... • ↑ Макс ↓ Мін»;
 * - 3 симетричні скляні міні-чіпи в нижньому доці Hero (Вологість, Вітер, Опади / УФ).
 */
@Composable
fun CurrentWeatherHero(
    current: CurrentWeather,
    todayForecast: DailyForecastItem?,
    updatedAt: LocalDateTime,
    isStale: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val units = LocalWeatherUnits.current
    val noDataStr = stringResource(R.string.no_data)

    val tempVal = units.convertTemperature(current.temperatureCelsius)
    val tempPrefix = if (tempVal > 0) "+" else ""
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val updateTimeFormatted = updatedAt.format(timeFormatter)

    GlassCard(
        cornerRadius = 32.dp,
        topColor = if (palette.isDay) Color(0x963068C0) else Color(0x9E1A3066),
        bottomColor = if (palette.isDay) Color(0xB5122C62) else Color(0xBF0C183A),
        borderTopColor = Color(0x90FFFFFF),
        borderBottomColor = Color(0x5538BDF8),
        glowColor = palette.heroGlowColor,
        glowAlignmentFactorX = 0.76f,
        glowAlignmentFactorY = 0.36f,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Верхня зона Hero: зліва бейдж стану + гігантська температура, справа велика 3D-ілюстрація
            Box(modifier = Modifier.fillMaxWidth()) {
                // Велика багатошарова 3D-ілюстрація погоди праворуч
                WeatherIllustration(
                    condition = current.condition,
                    isDay = palette.isDay,
                    modifier = Modifier
                        .size(134.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .align(Alignment.TopStart)
                ) {
                    // Темно-сапфірова пігулка стану погоди
                    GlassPillBadge(
                        cornerRadius = 16.dp,
                        fillColor = Color(0xD90C1E46),
                        borderColor = Color(0x997DD3FC)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (palette.isDay) Color(0xFFFACC15) else Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = stringResource(current.condition.stringRes),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Гігантська скульптурна температура з оптичним знаком градуса
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "$tempPrefix$tempVal",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 78.sp,
                                lineHeight = 80.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-2).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "°",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Рядок «Відчувається як ... • ↑ Макс ↓ Мін»
            val feelsAndRangeText = buildString {
                if (current.apparentTemperatureCelsius != null) {
                    val feelsFormatted = units.formatTemperature(current.apparentTemperatureCelsius)
                    append(stringResource(R.string.feels_like, feelsFormatted))
                }
                if (todayForecast != null) {
                    if (isNotEmpty()) append("   •   ")
                    val maxStr = units.formatTemperature(todayForecast.maxTemperatureCelsius)
                    val minStr = units.formatTemperature(todayForecast.minTemperatureCelsius)
                    append("↑ $maxStr   ↓ $minStr")
                }
            }

            if (feelsAndRangeText.isNotBlank()) {
                Text(
                    text = feelsAndRangeText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFE0F2FE)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 симетричні скляні міні-чіпи в нижньому доці Hero
            val humidityText = current.relativeHumidityPercent?.let { "$it%" } ?: noDataStr
            val windText = current.windSpeedKmh?.let {
                units.formatWindSpeed(
                    kmh = it,
                    unitLabelKmh = stringResource(R.string.wind_unit_kmh),
                    unitLabelMs = stringResource(R.string.wind_unit_ms)
                )
            } ?: noDataStr
            val popAndUvText = buildString {
                val pop = todayForecast?.precipitationProbabilityPercent
                if (pop != null) {
                    append("$pop%")
                }
                if (current.uvIndex != null) {
                    if (isNotEmpty()) append(" • ")
                    append("УФ ${current.uvIndex.roundToInt()}")
                }
                if (isEmpty()) append("—")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HeroQuickMetricChip(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF38BDF8),
                    label = stringResource(R.string.metric_humidity),
                    value = humidityText,
                    modifier = Modifier.weight(0.95f)
                )
                HeroQuickMetricChip(
                    icon = Icons.Default.Air,
                    iconTint = Color(0xFF7DD3FC),
                    label = stringResource(R.string.metric_wind),
                    value = windText,
                    modifier = Modifier.weight(1.15f)
                )
                HeroQuickMetricChip(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFACC15),
                    label = "${stringResource(R.string.precipitation)} / УФ",
                    value = popAndUvText,
                    modifier = Modifier.weight(1.05f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Індикатор свіжості даних або попередження про застарілість
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isStale) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StaleBadgeBackground)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = stringResource(R.string.stale_data_warning),
                            tint = StaleBadgeText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stringResource(R.string.updated_at, updateTimeFormatted)} (${stringResource(R.string.stale_data_warning)})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = StaleBadgeText
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.updated_at, updateTimeFormatted),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFFBAD6F8).copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroQuickMetricChip(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassPillBadge(
        cornerRadius = 15.dp,
        fillColor = Color(0xD90C1C40),
        borderColor = Color(0x667DD3FC),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFFBAD6F8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
